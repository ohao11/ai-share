package com.example.aishare.dto.response;

import lombok.Data;

/**
 * MinIO 上传响应
 */
@Data
public class UploadResponse {

    private String fileId;
    private String fileName;
    private String fileUrl;
    private Long fileSize;
    private String mimeType;

    public static UploadResponse of(Long id, String fileName, String fileUrl, Long fileSize, String mimeType) {
        UploadResponse response = new UploadResponse();
        response.setFileId(String.valueOf(id));
        response.setFileName(fileName);
        response.setFileUrl(fileUrl);
        response.setFileSize(fileSize);
        response.setMimeType(mimeType);
        return response;
    }
}
