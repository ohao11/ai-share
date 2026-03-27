package com.example.aishare.dto.response;

import lombok.Data;

import java.time.OffsetDateTime;
import java.util.List;

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
    private OffsetDateTime publishedAt;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    /**
     * 标签ID列表（用于编辑时回显）
     */
    private List<Long> tagIds;

    /**
     * 文章标签列表
     */
    private List<TagResponse> tags;
}
