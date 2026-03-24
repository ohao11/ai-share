package com.example.aishare.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.aishare.dto.request.ArticleCreateRequest;
import com.example.aishare.dto.response.ArticleResponse;
import com.example.aishare.entity.Article;

/**
 * 文章服务接口
 */
public interface ArticleService extends IService<Article> {

    /**
     * 分页获取文章列表
     */
    Page<ArticleResponse> getArticles(Integer page, Integer size, Long categoryId, String keyword);

    /**
     * 获取文章详情
     */
    ArticleResponse getArticleById(Long id);

    /**
     * 创建文章
     */
    ArticleResponse createArticle(ArticleCreateRequest request);

    /**
     * 更新文章
     */
    ArticleResponse updateArticle(Long id, ArticleCreateRequest request);

    /**
     * 删除文章
     */
    void deleteArticle(Long id);

    /**
     * 发布文章
     */
    void publishArticle(Long id);

    /**
     * 增加阅读量
     */
    void incrementViewCount(Long id);

    /**
     * 点赞文章
     */
    void likeArticle(Long id);
}
