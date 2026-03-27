package com.example.aishare.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.OffsetDateTime;

/**
 * 分类实体
 */
@Data
@TableName("categories")
public class Category implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 分类 ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 分类名
     */
    private String name;

    /**
     * Slug
     */
    private String slug;

    /**
     * 描述
     */
    private String description;

    /**
     * 父分类 ID
     */
    private Long parentId;

    /**
     * 排序
     */
    private Integer sortOrder;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private OffsetDateTime createdAt;

    /**
     * 文章数量（非数据库字段，用于展示）
     */
    @TableField(exist = false)
    private Integer articleCount;
}
