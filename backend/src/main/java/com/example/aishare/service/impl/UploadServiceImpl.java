package com.example.aishare.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.aishare.common.exception.BusinessException;
import com.example.aishare.dto.request.PresignedUrlRequest;
import com.example.aishare.dto.response.UploadResponse;
import com.example.aishare.entity.File;
import com.example.aishare.entity.User;
import com.example.aishare.mapper.FileMapper;
import com.example.aishare.mapper.UserMapper;
import com.example.aishare.service.UploadService;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.RemoveObjectArgs;
import io.minio.http.Method;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 文件上传服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UploadServiceImpl implements UploadService {

    private final MinioClient minioClient;
    private final FileMapper fileMapper;
    private final UserMapper userMapper;

    @Value("${minio.bucket:ai-share}")
    private String bucketName;

    @Override
    public String getPresignedUrl(PresignedUrlRequest request) {
        try {
            String objectName = generateObjectName(request.getFileName());
            String url = minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.PUT)
                            .bucket(bucketName)
                            .object(objectName)
                            .expiry(15, TimeUnit.MINUTES)
                            .build());
            log.info("生成预签名 URL: {}", objectName);
            return url;
        } catch (Exception e) {
            log.error("生成预签名 URL 失败：{}", e.getMessage());
            throw new BusinessException("生成上传 URL 失败");
        }
    }

    @Override
    public UploadResponse confirmUpload(PresignedUrlRequest request, String objectName) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", username);
        User user = userMapper.selectOne(queryWrapper);

        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        File file = new File();
        file.setUuid(UUID.randomUUID());
        file.setFileName(request.getFileName());
        file.setFilePath(objectName);
        file.setBucket(bucketName);
        file.setFileSize(request.getFileSize());
        file.setMimeType(request.getMimeType());
        file.setUploaderId(user.getId());

        fileMapper.insert(file);

        String fileUrl = getFileUrl(objectName);
        return UploadResponse.of(file.getId(), request.getFileName(), fileUrl, request.getFileSize(), request.getMimeType());
    }

    @Override
    public void deleteFile(Long fileId) {
        File file = fileMapper.selectById(fileId);
        if (file == null) {
            throw new BusinessException("文件不存在");
        }

        // 从 MinIO 删除对象
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(file.getBucket() != null ? file.getBucket() : bucketName)
                            .object(file.getFilePath())
                            .build());
            log.info("从 MinIO 删除文件: {}", file.getFilePath());
        } catch (Exception e) {
            log.error("从 MinIO 删除文件失败：{}", e.getMessage());
            // 继续删除数据库记录，即使 MinIO 删除失败
        }

        // 删除数据库记录
        fileMapper.deleteById(fileId);
    }

    @Override
    public Object getFiles(String folder, Integer page, Integer size) {
        // TODO: 实现文件列表查询
        return List.of();
    }

    private String generateObjectName(String fileName) {
        String extension = fileName.contains(".") ? fileName.substring(fileName.lastIndexOf(".")) : "";
        return "uploads/" + System.currentTimeMillis() + "-" + UUID.randomUUID() + extension;
    }

    private String getFileUrl(String objectName) {
        try {
            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(bucketName)
                            .object(objectName)
                            .expiry(7, TimeUnit.DAYS)
                            .build());
        } catch (Exception e) {
            log.error("生成文件 URL 失败：{}", e.getMessage());
            return "";
        }
    }
}
