package com.example.aishare.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.OffsetDateTime;

/**
 * 标签实体
 */
@Data
@TableName("tags")
public class Tag implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 标签 ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 标签名
     */
    private String name;

    /**
     * Slug
     */
    private String slug;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private OffsetDateTime createdAt;
}
