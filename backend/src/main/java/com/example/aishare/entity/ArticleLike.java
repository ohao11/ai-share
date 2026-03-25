package com.example.aishare.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.OffsetDateTime;

/**
 * 文章点赞实体
 */
@Data
@TableName("article_likes")
public class ArticleLike {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long articleId;

    private Long userId;

    private OffsetDateTime createdAt;
}