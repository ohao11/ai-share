package com.example.aishare.dto.request;

import lombok.Data;

/**
 * MinIO 预签名 URL 请求
 */
@Data
public class PresignedUrlRequest {

    private String fileName;
    private Long fileSize;
    private String mimeType;
    private String folder;
}
