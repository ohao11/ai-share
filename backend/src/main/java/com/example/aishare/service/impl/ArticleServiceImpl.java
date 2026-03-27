package com.example.aishare.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.aishare.common.constants.SystemConstants;
import com.example.aishare.common.exception.BusinessException;
import com.example.aishare.dto.request.ArticleCreateRequest;
import com.example.aishare.dto.response.ArticleResponse;
import com.example.aishare.dto.response.TagResponse;
import com.example.aishare.entity.Article;
import com.example.aishare.entity.ArticleLike;
import com.example.aishare.entity.ArticleTag;
import com.example.aishare.entity.Category;
import com.example.aishare.entity.Tag;
import com.example.aishare.entity.User;
import com.example.aishare.mapper.ArticleLikeMapper;
import com.example.aishare.mapper.ArticleMapper;
import com.example.aishare.mapper.ArticleTagMapper;
import com.example.aishare.mapper.CategoryMapper;
import com.example.aishare.mapper.TagMapper;
import com.example.aishare.mapper.UserMapper;
import com.example.aishare.dto.response.UserResponse;
import com.example.aishare.service.ArticleService;
import com.example.aishare.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;

/**
 * 文章服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ArticleServiceImpl extends ServiceImpl<ArticleMapper, Article> implements ArticleService {

    private final ArticleLikeMapper articleLikeMapper;
    private final ArticleTagMapper articleTagMapper;
    private final TagMapper tagMapper;
    private final UserMapper userMapper;
    private final CategoryMapper categoryMapper;
    private final UserService userService;

    @Override
    public Page<ArticleResponse> getArticles(Integer page, Integer size, Long categoryId, String keyword) {
        // 如果有关键词，使用全文搜索
        if (keyword != null && !keyword.isBlank()) {
            return searchArticles(page, size, categoryId, keyword);
        }

        Page<Article> articlePage = new Page<>(page, size);

        Page<Article> result = lambdaQuery()
                .eq(Article::getStatus, SystemConstants.ArticleStatus.PUBLISHED)
                .eq(categoryId != null, Article::getCategoryId, categoryId)
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
    public Page<ArticleResponse> searchArticles(Integer page, Integer size, Long categoryId, String keyword) {
        int offset = (page - 1) * size;
        // 处理 NULL 值，使用 0 表示不筛选分类，使用空字符串表示不筛选关键词
        Long effectiveCategoryId = categoryId != null ? categoryId : 0L;
        String effectiveKeyword = keyword != null ? keyword : "";

        List<Article> articles = baseMapper.searchArticles(effectiveCategoryId, effectiveKeyword, size, offset);
        Long total = baseMapper.countSearchArticles(effectiveCategoryId, effectiveKeyword);

        Page<ArticleResponse> responsePage = new Page<>(page, size);
        responsePage.setTotal(total);
        responsePage.setRecords(articles.stream()
                .map(this::convertToArticleResponse)
                .toList());

        return responsePage;
    }

    @Override
    public Page<ArticleResponse> getAllArticles(Integer page, Integer size, Long categoryId, String keyword, Integer status) {
        Page<Article> articlePage = new Page<>(page, size);

        Page<Article> result = lambdaQuery()
                .eq(categoryId != null, Article::getCategoryId, categoryId)
                .eq(status != null, Article::getStatus, status)
                .like(keyword != null && !keyword.isBlank(), Article::getTitle, keyword)
                .orderByDesc(Article::getCreatedAt)
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
        // 获取当前登录用户
        UserResponse currentUser = userService.getCurrentUser();

        Article article = new Article();
        article.setTitle(request.getTitle());
        article.setSlug(request.getSlug());
        article.setSummary(request.getSummary());
        article.setContent(request.getContent());
        article.setCoverImage(request.getCoverImage());
        article.setCategoryId(request.getCategoryId());
        article.setStatus(request.getStatus() != null ? request.getStatus() : SystemConstants.ArticleStatus.DRAFT);
        article.setAuthorId(currentUser.getId());

        save(article);

        // 处理标签关联
        if (request.getTagIds() != null && !request.getTagIds().isEmpty()) {
            saveArticleTags(article.getId(), request.getTagIds());
        }

        return convertToArticleResponse(article);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ArticleResponse updateArticle(Long id, ArticleCreateRequest request) {
        Article article = getById(id);
        if (article == null) {
            throw new BusinessException("文章不存在");
        }

        // 检查权限：只有作者才能修改文章
        UserResponse currentUser = userService.getCurrentUser();
        if (!article.getAuthorId().equals(currentUser.getId())) {
            throw new BusinessException("无权修改此文章");
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

        // 更新标签关联
        // 先删除旧的关联
        articleTagMapper.delete(new LambdaQueryWrapper<ArticleTag>()
                .eq(ArticleTag::getArticleId, id));
        // 再添加新的关联
        if (request.getTagIds() != null && !request.getTagIds().isEmpty()) {
            saveArticleTags(id, request.getTagIds());
        }

        return convertToArticleResponse(article);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteArticle(Long id) {
        Article article = getById(id);
        if (article == null) {
            throw new BusinessException("文章不存在");
        }

        // 检查权限：只有作者才能删除文章
        UserResponse currentUser = userService.getCurrentUser();
        if (!article.getAuthorId().equals(currentUser.getId())) {
            throw new BusinessException("无权删除此文章");
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

        // 检查权限：只有作者才能发布文章
        UserResponse currentUser = userService.getCurrentUser();
        if (!article.getAuthorId().equals(currentUser.getId())) {
            throw new BusinessException("无权发布此文章");
        }

        article.setStatus(SystemConstants.ArticleStatus.PUBLISHED);
        article.setPublishedAt(OffsetDateTime.now());
        updateById(article);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeArticle(Long id) {
        Article article = getById(id);
        if (article == null) {
            throw new BusinessException("文章不存在");
        }

        // 检查权限：只有作者才能下架文章
        UserResponse currentUser = userService.getCurrentUser();
        if (!article.getAuthorId().equals(currentUser.getId())) {
            throw new BusinessException("无权下架此文章");
        }

        article.setStatus(SystemConstants.ArticleStatus.REMOVED);
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
    public boolean likeArticle(Long articleId, Long userId) {
        Article article = getById(articleId);
        if (article == null) {
            throw new BusinessException("文章不存在");
        }

        // 检查是否已点赞
        LambdaQueryWrapper<ArticleLike> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ArticleLike::getArticleId, articleId)
                    .eq(ArticleLike::getUserId, userId);
        ArticleLike existingLike = articleLikeMapper.selectOne(queryWrapper);

        if (existingLike != null) {
            // 已点赞，取消点赞
            articleLikeMapper.deleteById(existingLike.getId());
            // 减少点赞数
            baseMapper.update(null,
                    new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<Article>()
                            .eq(Article::getId, articleId)
                            .setSql("like_count = GREATEST(0, like_count - 1)"));
            return false;
        } else {
            // 未点赞，添加点赞
            ArticleLike articleLike = new ArticleLike();
            articleLike.setArticleId(articleId);
            articleLike.setUserId(userId);
            articleLike.setCreatedAt(OffsetDateTime.now());
            articleLikeMapper.insert(articleLike);
            // 增加点赞数
            baseMapper.update(null,
                    new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<Article>()
                            .eq(Article::getId, articleId)
                            .setSql("like_count = like_count + 1"));
            return true;
        }
    }

    @Override
    public boolean isLiked(Long articleId, Long userId) {
        if (userId == null) {
            return false;
        }
        LambdaQueryWrapper<ArticleLike> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ArticleLike::getArticleId, articleId)
                    .eq(ArticleLike::getUserId, userId);
        return articleLikeMapper.selectCount(queryWrapper) > 0;
    }

    @Override
    public Page<ArticleResponse> getArticlesByAuthor(Integer page, Integer size, Long authorId) {
        Page<Article> articlePage = new Page<>(page, size);

        Page<Article> result = lambdaQuery()
                .eq(Article::getAuthorId, authorId)
                .orderByDesc(Article::getCreatedAt)
                .page(articlePage);

        Page<ArticleResponse> responsePage = new Page<>(page, size);
        responsePage.setTotal(result.getTotal());
        responsePage.setRecords(result.getRecords().stream()
                .map(this::convertToArticleResponse)
                .toList());

        return responsePage;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void unlikeArticle(Long articleId, Long userId) {
        Article article = getById(articleId);
        if (article == null) {
            throw new BusinessException("文章不存在");
        }

        // 查找点赞记录
        LambdaQueryWrapper<ArticleLike> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ArticleLike::getArticleId, articleId)
                    .eq(ArticleLike::getUserId, userId);
        ArticleLike existingLike = articleLikeMapper.selectOne(queryWrapper);

        if (existingLike == null) {
            throw new BusinessException("尚未点赞");
        }

        // 删除点赞记录
        articleLikeMapper.deleteById(existingLike.getId());

        // 减少点赞数
        baseMapper.update(null,
                new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<Article>()
                        .eq(Article::getId, articleId)
                        .setSql("like_count = GREATEST(0, like_count - 1)"));

        log.info("取消点赞成功：articleId={}, userId={}", articleId, userId);
    }

    @Override
    public Page<ArticleResponse> getArticlesByTag(Integer page, Integer size, Long tagId) {
        // 先查询该标签关联的文章ID列表
        List<Long> articleIds = articleTagMapper.selectArticleIdsByTagId(tagId);
        if (articleIds == null || articleIds.isEmpty()) {
            Page<ArticleResponse> emptyPage = new Page<>(page, size);
            emptyPage.setTotal(0);
            emptyPage.setRecords(Collections.emptyList());
            return emptyPage;
        }

        Page<Article> articlePage = new Page<>(page, size);
        Page<Article> result = lambdaQuery()
                .in(Article::getId, articleIds)
                .eq(Article::getStatus, SystemConstants.ArticleStatus.PUBLISHED)
                .orderByDesc(Article::getPublishedAt)
                .page(articlePage);

        Page<ArticleResponse> responsePage = new Page<>(page, size);
        responsePage.setTotal(result.getTotal());
        responsePage.setRecords(result.getRecords().stream()
                .map(this::convertToArticleResponse)
                .toList());

        return responsePage;
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

        // 查询作者名称
        if (article.getAuthorId() != null) {
            User author = userMapper.selectById(article.getAuthorId());
            if (author != null) {
                response.setAuthorName(author.getUsername());
            }
        }

        // 查询分类名称
        if (article.getCategoryId() != null) {
            Category category = categoryMapper.selectById(article.getCategoryId());
            if (category != null) {
                response.setCategoryName(category.getName());
            }
        }

        // 查询文章标签
        List<Long> tagIds = articleTagMapper.selectTagIdsByArticleId(article.getId());
        if (tagIds != null && !tagIds.isEmpty()) {
            response.setTagIds(tagIds);
            List<Tag> tags = tagMapper.selectBatchIds(tagIds);
            List<TagResponse> tagResponses = tags.stream().map(tag -> {
                TagResponse tr = new TagResponse();
                tr.setId(tag.getId());
                tr.setName(tag.getName());
                tr.setSlug(tag.getSlug());
                return tr;
            }).toList();
            response.setTags(tagResponses);
        } else {
            response.setTagIds(Collections.emptyList());
            response.setTags(Collections.emptyList());
        }

        return response;
    }

    /**
     * 保存文章标签关联
     */
    private void saveArticleTags(Long articleId, List<Long> tagIds) {
        for (Long tagId : tagIds) {
            ArticleTag articleTag = new ArticleTag();
            articleTag.setArticleId(articleId);
            articleTag.setTagId(tagId);
            articleTagMapper.insert(articleTag);
        }
    }
}
