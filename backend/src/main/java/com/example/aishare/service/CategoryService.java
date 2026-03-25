package com.example.aishare.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.aishare.entity.Category;

import java.util.List;

/**
 * 分类服务接口
 */
public interface CategoryService extends IService<Category> {

    /**
     * 获取分类树
     */
    List<Category> getCategoryTree();

    /**
     * 根据 Slug 获取分类
     */
    Category getBySlug(String slug);

    /**
     * 创建分类
     */
    Category createCategory(Category category);

    /**
     * 更新分类
     */
    Category updateCategory(Long id, Category category);

    /**
     * 删除分类
     */
    void deleteCategory(Long id);
}
