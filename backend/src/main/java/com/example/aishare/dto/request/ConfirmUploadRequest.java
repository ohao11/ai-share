package com.example.aishare.dto.request;

import lombok.Data;

/**
 * 确认上传请求
 */
@Data
public class ConfirmUploadRequest {

    private String fileName;
    private Long fileSize;
    private String mimeType;
    private String objectName;
}