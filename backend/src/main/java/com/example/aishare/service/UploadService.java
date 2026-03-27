package com.example.aishare.service;

import com.example.aishare.dto.request.ConfirmUploadRequest;
import com.example.aishare.dto.request.PresignedUrlRequest;
import com.example.aishare.dto.response.PresignedUrlResponse;
import com.example.aishare.dto.response.UploadResponse;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件上传服务接口
 */
public interface UploadService {

    /**
     * 直接上传文件
     */
    UploadResponse uploadFile(MultipartFile file, String folder);

    /**
     * 获取预签名上传 URL
     */
    PresignedUrlResponse getPresignedUrlResponse(PresignedUrlRequest request);

    /**
     * 确认上传完成
     */
    UploadResponse confirmUpload(ConfirmUploadRequest request);

    /**
     * 删除文件
     */
    void deleteFile(Long fileId);

    /**
     * 获取文件列表
     */
    Object getFiles(String folder, Integer page, Integer size);
}
