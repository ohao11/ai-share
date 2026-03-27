package com.example.aishare.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.aishare.common.exception.BusinessException;
import com.example.aishare.dto.request.ConfirmUploadRequest;
import com.example.aishare.dto.request.PresignedUrlRequest;
import com.example.aishare.dto.response.PresignedUrlResponse;
import com.example.aishare.dto.response.UploadResponse;
import com.example.aishare.entity.File;
import com.example.aishare.entity.User;
import com.example.aishare.mapper.FileMapper;
import com.example.aishare.mapper.UserMapper;
import com.example.aishare.service.UploadService;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.http.Method;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

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

    @Value("${minio.endpoint:http://minio:9000}")
    private String endpoint;

    @Value("${minio.public-endpoint:/minio-upload}")
    private String publicEndpoint;

    @Override
    public UploadResponse uploadFile(MultipartFile file, String folder) {
        try {
            // 获取当前用户
            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            QueryWrapper<User> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("username", username);
            User user = userMapper.selectOne(queryWrapper);

            if (user == null) {
                throw new BusinessException("用户不存在");
            }

            // 生成对象名称
            String objectName = generateObjectName(file.getOriginalFilename());

            // 上传到 MinIO
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .stream(file.getInputStream(), file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build());

            log.info("文件上传成功: {}", objectName);

            // 保存文件记录 (uuid 由数据库自动生成)
            File fileEntity = new File();
            fileEntity.setFileName(file.getOriginalFilename());
            fileEntity.setFilePath(objectName);
            fileEntity.setBucket(bucketName);
            fileEntity.setFileSize(file.getSize());
            fileEntity.setMimeType(file.getContentType());
            fileEntity.setUploaderId(user.getId());

            fileMapper.insert(fileEntity);

            String fileUrl = getFileUrl(objectName);
            return UploadResponse.of(fileEntity.getId(), file.getOriginalFilename(), fileUrl, file.getSize(), file.getContentType());
        } catch (Exception e) {
            log.error("文件上传失败: {}", e.getMessage(), e);
            throw new BusinessException("文件上传失败: " + e.getMessage());
        }
    }

    @Override
    public PresignedUrlResponse getPresignedUrlResponse(PresignedUrlRequest request) {
        try {
            String objectName = generateObjectName(request.getFileName());
            // 生成 MinIO presigned URL
            String presignedUrl = minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.PUT)
                            .bucket(bucketName)
                            .object(objectName)
                            .expiry(15, TimeUnit.MINUTES)
                            .build());
            log.info("MinIO 原始 URL: {}", presignedUrl);
            log.info("endpoint: {}", endpoint);
            log.info("publicEndpoint: {}", publicEndpoint);
            // 替换为 nginx 代理地址，供前端访问
            String publicUrl = presignedUrl.replace(endpoint, publicEndpoint);
            log.info("生成上传 URL: {} -> {}", objectName, publicUrl);
            return PresignedUrlResponse.of(publicUrl, objectName);
        } catch (Exception e) {
            log.error("生成预签名 URL 失败：{}", e.getMessage());
            throw new BusinessException("生成上传 URL 失败");
        }
    }

    @Override
    public UploadResponse confirmUpload(ConfirmUploadRequest request) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", username);
        User user = userMapper.selectOne(queryWrapper);

        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        File file = new File();
        file.setFileName(request.getFileName());
        file.setFilePath(request.getObjectName());
        file.setBucket(bucketName);
        file.setFileSize(request.getFileSize());
        file.setMimeType(request.getMimeType());
        file.setUploaderId(user.getId());

        fileMapper.insert(file);

        String fileUrl = getFileUrl(request.getObjectName());
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
        // 返回通过 nginx 代理的 URL，格式为 /files/{bucket}/{objectName}
        return "/files/" + bucketName + "/" + objectName;
    }
}
