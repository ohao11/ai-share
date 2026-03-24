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
}
