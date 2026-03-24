package com.example.aishare.dto.response;

import lombok.Data;

import java.time.ZonedDateTime;

/**
 * 文章信息响应
 */
@Data
public class ArticleResponse {

    private Long id;
    private String title;
    private String slug;
    private String summary;
    private String content;
    private String coverImage;
    private Long authorId;
    private String authorName;
    private Long categoryId;
    private String categoryName;
    private Integer status;
    private Integer viewCount;
    private Integer likeCount;
    private Integer commentCount;
    private ZonedDateTime publishedAt;
    private ZonedDateTime createdAt;
    private ZonedDateTime updatedAt;
}
