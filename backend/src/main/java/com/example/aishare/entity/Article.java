package com.example.aishare.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.OffsetDateTime;

/**
 * 文章实体
 */
@Data
@TableName("articles")
public class Article implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 文章 ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 标题
     */
    private String title;

    /**
     * Slug
     */
    private String slug;

    /**
     * 摘要
     */
    private String summary;

    /**
     * 内容 (Markdown)
     */
    private String content;

    /**
     * 封面图 URL
     */
    private String coverImage;

    /**
     * 作者 ID
     */
    private Long authorId;

    /**
     * 分类 ID
     */
    private Long categoryId;

    /**
     * 状态：0-草稿 1-已发布 2-下架
     */
    private Integer status;

    /**
     * 阅读量
     */
    private Integer viewCount;

    /**
     * 点赞数
     */
    private Integer likeCount;

    /**
     * 评论数
     */
    private Integer commentCount;

    /**
     * 发布时间
     */
    private OffsetDateTime publishedAt;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private OffsetDateTime createdAt;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private OffsetDateTime updatedAt;
}
