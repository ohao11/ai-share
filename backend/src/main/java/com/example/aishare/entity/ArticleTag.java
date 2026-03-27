package com.example.aishare.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 文章标签关联实体
 */
@Data
@TableName("article_tags")
public class ArticleTag {

    private Long articleId;

    private Long tagId;
}