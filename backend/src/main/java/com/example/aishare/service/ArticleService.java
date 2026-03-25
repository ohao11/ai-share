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
     * 分页获取文章列表（仅已发布）
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
     * 下架文章
     */
    void removeArticle(Long id);

    /**
     * 获取所有文章（管理员，包含草稿）
     */
    Page<ArticleResponse> getAllArticles(Integer page, Integer size, Long categoryId, String keyword, Integer status);

    /**
     * 增加阅读量
     */
    void incrementViewCount(Long id);

    /**
     * 点赞文章
     * @return true 表示点赞成功，false 表示取消点赞
     */
    boolean likeArticle(Long articleId, Long userId);

    /**
     * 检查用户是否已点赞
     */
    boolean isLiked(Long articleId, Long userId);

    /**
     * 按作者ID查询文章
     */
    Page<ArticleResponse> getArticlesByAuthor(Integer page, Integer size, Long authorId);
}
