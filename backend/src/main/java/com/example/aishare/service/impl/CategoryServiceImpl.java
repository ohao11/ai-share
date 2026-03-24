package com.example.aishare.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.aishare.entity.Category;
import com.example.aishare.mapper.CategoryMapper;
import com.example.aishare.service.CategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 分类服务实现
 */
@Slf4j
@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {

    @Override
    public List<Category> getCategoryTree() {
        return lambdaQuery()
                .orderByAsc(Category::getSortOrder)
                .list();
    }

    @Override
    public Category getBySlug(String slug) {
        return lambdaQuery()
                .eq(Category::getSlug, slug)
                .one();
    }
}
