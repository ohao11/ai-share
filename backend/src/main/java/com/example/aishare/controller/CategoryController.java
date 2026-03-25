package com.example.aishare.controller;

import com.example.aishare.common.result.Result;
import com.example.aishare.entity.Category;
import com.example.aishare.service.CategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 分类控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    /**
     * 获取分类列表
     */
    @GetMapping
    public Result<List<Category>> getCategories() {
        return Result.success(categoryService.getCategoryTree());
    }

    /**
     * 根据 Slug 获取分类
     */
    @GetMapping("/{slug}")
    public Result<Category> getCategoryBySlug(@PathVariable String slug) {
        return Result.success(categoryService.getBySlug(slug));
    }

    /**
     * 创建分类
     */
    @PostMapping
    public Result<Category> createCategory(@RequestBody Category category) {
        return Result.success(categoryService.createCategory(category));
    }

    /**
     * 更新分类
     */
    @PutMapping("/{id}")
    public Result<Category> updateCategory(
            @PathVariable Long id,
            @RequestBody Category category) {
        return Result.success(categoryService.updateCategory(id, category));
    }

    /**
     * 删除分类
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return Result.success();
    }
}
