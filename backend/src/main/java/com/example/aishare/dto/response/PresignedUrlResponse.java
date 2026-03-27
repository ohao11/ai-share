package com.example.aishare.dto.response;

import lombok.Data;

/**
 * 预签名 URL 响应
 */
@Data
public class PresignedUrlResponse {

    private String presignedUrl;
    private String objectName;

    public static PresignedUrlResponse of(String presignedUrl, String objectName) {
        PresignedUrlResponse response = new PresignedUrlResponse();
        response.setPresignedUrl(presignedUrl);
        response.setObjectName(objectName);
        return response;
    }
}