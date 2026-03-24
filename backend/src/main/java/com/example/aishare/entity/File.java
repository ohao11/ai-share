package com.example.aishare.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.OffsetDateTime;

/**
 * 文件实体
 */
@Data
@TableName("files")
public class File implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 文件 ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * UUID
     */
    private java.util.UUID uuid;

    /**
     * 文件名
     */
    private String fileName;

    /**
     * 文件路径 (MinIO object key)
     */
    private String filePath;

    /**
     * Bucket 名称
     */
    private String bucket;

    /**
     * 文件大小
     */
    private Long fileSize;

    /**
     * MIME 类型
     */
    private String mimeType;

    /**
     * 上传者 ID
     */
    private Long uploaderId;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private OffsetDateTime createdAt;
}
