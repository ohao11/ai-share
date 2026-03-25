package com.example.aishare.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.aishare.entity.Article;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
 * 文章 Mapper
 */
@Mapper
public interface ArticleMapper extends BaseMapper<Article> {

    @Select("SELECT COALESCE(SUM(view_count), 0) FROM articles")
    Long selectSumViewCount();

    @Select("SELECT COALESCE(SUM(like_count), 0) FROM articles")
    Long selectSumLikeCount();
}
