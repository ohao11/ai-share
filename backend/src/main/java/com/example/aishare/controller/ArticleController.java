package com.example.aishare.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.aishare.common.result.PageResult;
import com.example.aishare.common.result.Result;
import com.example.aishare.dto.request.ArticleCreateRequest;
import com.example.aishare.dto.response.ArticleResponse;
import com.example.aishare.dto.response.UserResponse;
import com.example.aishare.service.ArticleService;
import com.example.aishare.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 文章控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/articles")
@RequiredArgsConstructor
public class ArticleController {

    private final ArticleService articleService;
    private final UserService userService;

    /**
     * 获取文章列表
     */
    @GetMapping
    public PageResult<ArticleResponse> getArticles(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String keyword) {
        Page<ArticleResponse> result = articleService.getArticles(page, size, categoryId, keyword);
        return PageResult.of(result.getRecords(), result.getTotal(), result.getCurrent(), result.getSize());
    }

    /**
     * 获取文章详情
     */
    @GetMapping("/{id}")
    public Result<ArticleResponse> getArticle(@PathVariable Long id) {
        return Result.success(articleService.getArticleById(id));
    }

    /**
     * 创建文章
     */
    @PostMapping
    public Result<ArticleResponse> createArticle(@Valid @RequestBody ArticleCreateRequest request) {
        return Result.success(articleService.createArticle(request));
    }

    /**
     * 更新文章
     */
    @PutMapping("/{id}")
    public Result<ArticleResponse> updateArticle(
            @PathVariable Long id,
            @Valid @RequestBody ArticleCreateRequest request) {
        return Result.success(articleService.updateArticle(id, request));
    }

    /**
     * 删除文章
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteArticle(@PathVariable Long id) {
        articleService.deleteArticle(id);
        return Result.success();
    }

    /**
     * 发布文章
     */
    @PostMapping("/{id}/publish")
    public Result<Void> publishArticle(@PathVariable Long id) {
        articleService.publishArticle(id);
        return Result.success();
    }

    /**
     * 点赞文章
     */
    @PostMapping("/{id}/like")
    public Result<Map<String, Boolean>> likeArticle(@PathVariable Long id) {
        UserResponse user = userService.getCurrentUser();
        boolean liked = articleService.likeArticle(id, user.getId());
        return Result.success(Map.of("liked", liked));
    }

    /**
     * 取消点赞
     */
    @DeleteMapping("/{id}/like")
    public Result<Void> unlikeArticle(@PathVariable Long id) {
        UserResponse user = userService.getCurrentUser();
        articleService.unlikeArticle(id, user.getId());
        return Result.success();
    }

    /**
     * 检查是否点赞
     */
    @GetMapping("/{id}/liked")
    public Result<Map<String, Boolean>> isLiked(@PathVariable Long id) {
        UserResponse user = userService.getCurrentUser();
        boolean liked = articleService.isLiked(id, user.getId());
        return Result.success(Map.of("liked", liked));
    }

    /**
     * 按作者查询文章
     */
    @GetMapping("/author/{authorId}")
    public PageResult<ArticleResponse> getArticlesByAuthor(
            @PathVariable Long authorId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        Page<ArticleResponse> result = articleService.getArticlesByAuthor(page, size, authorId);
        return PageResult.of(result.getRecords(), result.getTotal(), result.getCurrent(), result.getSize());
    }
}
