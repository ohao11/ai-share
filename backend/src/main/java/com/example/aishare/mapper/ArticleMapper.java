package com.example.aishare.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.aishare.entity.Article;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 文章 Mapper
 */
@Mapper
public interface ArticleMapper extends BaseMapper<Article> {

    @Select("SELECT COALESCE(SUM(view_count), 0) FROM articles")
    Long selectSumViewCount();

    @Select("SELECT COALESCE(SUM(like_count), 0) FROM articles")
    Long selectSumLikeCount();

    /**
     * 全文搜索文章
     */
    @Select("""
        SELECT a.id, a.title, a.slug, a.summary, a.content, a.cover_image, a.author_id,
               a.category_id, a.status, a.view_count, a.like_count, a.comment_count,
               a.published_at, a.created_at, a.updated_at
        FROM articles a
        WHERE a.status = 1
        AND (#{categoryId} = 0 OR a.category_id = #{categoryId})
        AND (#{keyword} = '' OR a.search_vector @@ plainto_tsquery('simple', #{keyword}))
        ORDER BY
            CASE WHEN #{keyword} != ''
                 THEN ts_rank(a.search_vector, plainto_tsquery('simple', #{keyword}))
                 ELSE 0
            END DESC,
            a.published_at DESC
        LIMIT #{limit} OFFSET #{offset}
    """)
    List<Article> searchArticles(@Param("categoryId") Long categoryId, @Param("keyword") String keyword,
                                  @Param("limit") int limit, @Param("offset") int offset);

    /**
     * 全文搜索文章总数
     */
    @Select("""
        SELECT COUNT(*)
        FROM articles a
        WHERE a.status = 1
        AND (#{categoryId} = 0 OR a.category_id = #{categoryId})
        AND (#{keyword} = '' OR a.search_vector @@ plainto_tsquery('simple', #{keyword}))
    """)
    Long countSearchArticles(@Param("categoryId") Long categoryId, @Param("keyword") String keyword);
}
