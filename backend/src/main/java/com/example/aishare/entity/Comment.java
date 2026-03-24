package com.example.aishare.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.ZonedDateTime;

/**
 * 评论实体
 */
@Data
@TableName("comments")
public class Comment implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 评论 ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 评论内容
     */
    private String content;

    /**
     * 文章 ID
     */
    private Long articleId;

    /**
     * 用户 ID
     */
    private Long userId;

    /**
     * 父评论 ID
     */
    private Long parentId;

    /**
     * 状态：0-待审核 1-通过 2-拒绝
     */
    private Integer status;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private ZonedDateTime createdAt;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private ZonedDateTime updatedAt;
}
