package com.example.aishare.service;

import com.example.aishare.dto.request.PresignedUrlRequest;
import com.example.aishare.dto.response.UploadResponse;

/**
 * 文件上传服务接口
 */
public interface UploadService {

    /**
     * 获取预签名上传 URL
     */
    String getPresignedUrl(PresignedUrlRequest request);

    /**
     * 确认上传完成
     */
    UploadResponse confirmUpload(PresignedUrlRequest request, String objectName);

    /**
     * 删除文件
     */
    void deleteFile(Long fileId);

    /**
     * 获取文件列表
     */
    Object getFiles(String folder, Integer page, Integer size);
}
