package com.example.aishare.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.aishare.common.result.PageResult;
import com.example.aishare.common.result.Result;
import com.example.aishare.dto.request.ArticleCreateRequest;
import com.example.aishare.dto.response.ArticleResponse;
import com.example.aishare.dto.response.UserResponse;
import com.example.aishare.entity.Article;
import com.example.aishare.entity.User;
import com.example.aishare.service.ArticleService;
import com.example.aishare.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 后台管理控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserService userService;
    private final ArticleService articleService;

    /**
     * 获取用户列表
     */
    @GetMapping("/users")
    public PageResult<UserResponse> getUsers(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword) {
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<UserResponse> result = userService.getUsers(page, size, keyword);
        return PageResult.of(result.getRecords(), result.getTotal(), result.getCurrent(), result.getSize());
    }

    /**
     * 获取用户详情
     */
    @GetMapping("/users/{id}")
    public Result<UserResponse> getUser(@PathVariable Long id) {
        return Result.success(userService.getUserById(id));
    }

    /**
     * 更新用户角色
     */
    @PutMapping("/users/{id}/role")
    public Result<Void> updateUserRole(
            @PathVariable Long id,
            @RequestParam Integer role) {
        userService.updateUserRole(id, role);
        return Result.success();
    }

    /**
     * 更新用户状态
     */
    @PutMapping("/users/{id}/status")
    public Result<Void> updateUserStatus(
            @PathVariable Long id,
            @RequestParam Integer status) {
        userService.updateUserStatus(id, status);
        return Result.success();
    }

    /**
     * 删除用户
     */
    @DeleteMapping("/users/{id}")
    public Result<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return Result.success();
    }

    /**
     * 获取所有文章（包含草稿）
     */
    @GetMapping("/articles")
    public PageResult<ArticleResponse> getArticles(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status) {
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<ArticleResponse> result = articleService.getAllArticles(page, size, categoryId, keyword, status);
        return PageResult.of(result.getRecords(), result.getTotal(), result.getCurrent(), result.getSize());
    }

    /**
     * 获取文章详情（管理员）
     */
    @GetMapping("/articles/{id}")
    public Result<ArticleResponse> getArticle(@PathVariable Long id) {
        return Result.success(articleService.getArticleById(id));
    }

    /**
     * 更新文章（管理员）
     */
    @PutMapping("/articles/{id}")
    public Result<ArticleResponse> updateArticle(
            @PathVariable Long id,
            @Valid @RequestBody ArticleCreateRequest request) {
        return Result.success(articleService.updateArticle(id, request));
    }

    /**
     * 删除文章（管理员）
     */
    @DeleteMapping("/articles/{id}")
    public Result<Void> deleteArticle(@PathVariable Long id) {
        articleService.deleteArticle(id);
        return Result.success();
    }

    /**
     * 下架文章
     */
    @PostMapping("/articles/{id}/remove")
    public Result<Void> removeArticle(@PathVariable Long id) {
        articleService.removeArticle(id);
        return Result.success();
    }

    /**
     * 获取数据统计
     */
    @GetMapping("/stats")
    public Result<Object> getStats() {
        return Result.success(userService.getStats());
    }
}
