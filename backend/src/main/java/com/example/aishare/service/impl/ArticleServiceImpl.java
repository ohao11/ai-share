package com.example.aishare.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.aishare.common.constants.SystemConstants;
import com.example.aishare.common.exception.BusinessException;
import com.example.aishare.dto.request.ArticleCreateRequest;
import com.example.aishare.dto.response.ArticleResponse;
import com.example.aishare.entity.Article;
import com.example.aishare.mapper.ArticleMapper;
import com.example.aishare.service.ArticleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;

/**
 * 文章服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ArticleServiceImpl extends ServiceImpl<ArticleMapper, Article> implements ArticleService {

    @Override
    public Page<ArticleResponse> getArticles(Integer page, Integer size, Long categoryId, String keyword) {
        Page<Article> articlePage = new Page<>(page, size);

        Page<Article> result = lambdaQuery()
                .eq(Article::getStatus, SystemConstants.ArticleStatus.PUBLISHED)
                .eq(categoryId != null, Article::getCategoryId, categoryId)
                .like(keyword != null && !keyword.isBlank(), Article::getTitle, keyword)
                .orderByDesc(Article::getPublishedAt)
                .page(articlePage);

        Page<ArticleResponse> responsePage = new Page<>(page, size);
        responsePage.setTotal(result.getTotal());
        responsePage.setRecords(result.getRecords().stream()
                .map(this::convertToArticleResponse)
                .toList());

        return responsePage;
    }

    @Override
    public ArticleResponse getArticleById(Long id) {
        Article article = getById(id);
        if (article == null) {
            throw new BusinessException("文章不存在");
        }

        // 增加阅读量
        incrementViewCount(id);

        return convertToArticleResponse(article);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ArticleResponse createArticle(ArticleCreateRequest request) {
        Article article = new Article();
        article.setTitle(request.getTitle());
        article.setSlug(request.getSlug());
        article.setSummary(request.getSummary());
        article.setContent(request.getContent());
        article.setCoverImage(request.getCoverImage());
        article.setCategoryId(request.getCategoryId());
        article.setStatus(request.getStatus() != null ? request.getStatus() : SystemConstants.ArticleStatus.DRAFT);
        article.setAuthorId(1L); // TODO: 从当前用户获取

        save(article);
        return convertToArticleResponse(article);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ArticleResponse updateArticle(Long id, ArticleCreateRequest request) {
        Article article = getById(id);
        if (article == null) {
            throw new BusinessException("文章不存在");
        }

        article.setTitle(request.getTitle());
        article.setSlug(request.getSlug());
        article.setSummary(request.getSummary());
        article.setContent(request.getContent());
        article.setCoverImage(request.getCoverImage());
        article.setCategoryId(request.getCategoryId());
        if (request.getStatus() != null) {
            article.setStatus(request.getStatus());
        }

        updateById(article);
        return convertToArticleResponse(article);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteArticle(Long id) {
        Article article = getById(id);
        if (article == null) {
            throw new BusinessException("文章不存在");
        }

        removeById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void publishArticle(Long id) {
        Article article = getById(id);
        if (article == null) {
            throw new BusinessException("文章不存在");
        }

        article.setStatus(SystemConstants.ArticleStatus.PUBLISHED);
        article.setPublishedAt(ZonedDateTime.now());
        updateById(article);
    }

    @Override
    public void incrementViewCount(Long id) {
        baseMapper.update(null,
                new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<Article>()
                        .eq(Article::getId, id)
                        .setSql("view_count = view_count + 1"));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void likeArticle(Long id) {
        Article article = getById(id);
        if (article == null) {
            throw new BusinessException("文章不存在");
        }

        baseMapper.update(null,
                new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<Article>()
                        .eq(Article::getId, id)
                        .setSql("like_count = like_count + 1"));
    }

    private ArticleResponse convertToArticleResponse(Article article) {
        ArticleResponse response = new ArticleResponse();
        response.setId(article.getId());
        response.setTitle(article.getTitle());
        response.setSlug(article.getSlug());
        response.setSummary(article.getSummary());
        response.setContent(article.getContent());
        response.setCoverImage(article.getCoverImage());
        response.setAuthorId(article.getAuthorId());
        response.setCategoryId(article.getCategoryId());
        response.setStatus(article.getStatus());
        response.setViewCount(article.getViewCount());
        response.setLikeCount(article.getLikeCount());
        response.setCommentCount(article.getCommentCount());
        response.setPublishedAt(article.getPublishedAt());
        response.setCreatedAt(article.getCreatedAt());
        response.setUpdatedAt(article.getUpdatedAt());
        return response;
    }
}
