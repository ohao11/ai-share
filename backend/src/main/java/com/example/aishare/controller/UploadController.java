package com.example.aishare.controller;

import com.example.aishare.common.result.Result;
import com.example.aishare.dto.request.ConfirmUploadRequest;
import com.example.aishare.dto.request.PresignedUrlRequest;
import com.example.aishare.dto.response.PresignedUrlResponse;
import com.example.aishare.dto.response.UploadResponse;
import com.example.aishare.service.UploadService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件上传控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/upload")
@RequiredArgsConstructor
public class UploadController {

    private final UploadService uploadService;

    /**
     * 直接上传文件
     */
    @PostMapping
    public Result<UploadResponse> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "folder", required = false, defaultValue = "uploads") String folder) {
        UploadResponse response = uploadService.uploadFile(file, folder);
        return Result.success(response);
    }

    /**
     * 获取预签名上传 URL
     */
    @PostMapping("/presigned-url")
    public Result<PresignedUrlResponse> getPresignedUrl(@Valid @RequestBody PresignedUrlRequest request) {
        PresignedUrlResponse response = uploadService.getPresignedUrlResponse(request);
        return Result.success("获取上传 URL 成功", response);
    }

    /**
     * 确认上传完成
     */
    @PostMapping("/confirm")
    public Result<UploadResponse> confirmUpload(@RequestBody ConfirmUploadRequest request) {
        UploadResponse response = uploadService.confirmUpload(request);
        return Result.success(response);
    }

    /**
     * 删除文件
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteFile(@PathVariable Long id) {
        uploadService.deleteFile(id);
        return Result.success();
    }

    /**
     * 获取文件列表
     */
    @GetMapping("/files")
    public Result<Object> getFiles(
            @RequestParam(required = false) String folder,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        return Result.success(uploadService.getFiles(folder, page, size));
    }
}
