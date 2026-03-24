package com.example.aishare.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.aishare.entity.Article;
import org.apache.ibatis.annotations.Mapper;

/**
 * 文章 Mapper
 */
@Mapper
public interface ArticleMapper extends BaseMapper<Article> {
}
