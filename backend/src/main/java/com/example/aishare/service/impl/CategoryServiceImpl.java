package com.example.aishare.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.aishare.common.exception.BusinessException;
import com.example.aishare.entity.Category;
import com.example.aishare.mapper.ArticleMapper;
import com.example.aishare.mapper.CategoryMapper;
import com.example.aishare.service.CategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 分类服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {

    private final ArticleMapper articleMapper;

    @Override
    public List<Category> getCategoryTree() {
        List<Category> categories = lambdaQuery()
                .orderByAsc(Category::getSortOrder)
                .list();

        // 统计每个分类的文章数量
        for (Category category : categories) {
            Long count = articleMapper.selectCount(
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<com.example.aishare.entity.Article>()
                            .eq(com.example.aishare.entity.Article::getCategoryId, category.getId())
                            .eq(com.example.aishare.entity.Article::getStatus, 1) // 仅统计已发布的
            );
            category.setArticleCount(count.intValue());
        }

        return categories;
    }

    @Override
    public Category getBySlug(String slug) {
        return lambdaQuery()
                .eq(Category::getSlug, slug)
                .one();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Category createCategory(Category category) {
        // 检查 Slug 是否已存在
        Long count = lambdaQuery().eq(Category::getSlug, category.getSlug()).count();
        if (count > 0) {
            throw new BusinessException("Slug 已存在");
        }

        save(category);
        log.info("创建分类成功：id={}, name={}, slug={}", category.getId(), category.getName(), category.getSlug());
        return category;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Category updateCategory(Long id, Category category) {
        Category existing = getById(id);
        if (existing == null) {
            throw new BusinessException("分类不存在");
        }

        // 如果修改了 Slug，检查是否与其他分类重复
        if (!existing.getSlug().equals(category.getSlug())) {
            Long count = lambdaQuery()
                    .eq(Category::getSlug, category.getSlug())
                    .ne(Category::getId, id)
                    .count();
            if (count > 0) {
                throw new BusinessException("Slug 已存在");
            }
        }

        existing.setName(category.getName());
        existing.setSlug(category.getSlug());
        existing.setDescription(category.getDescription());
        existing.setParentId(category.getParentId());
        existing.setSortOrder(category.getSortOrder());

        updateById(existing);
        log.info("更新分类成功：id={}, name={}", id, existing.getName());
        return existing;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteCategory(Long id) {
        Category existing = getById(id);
        if (existing == null) {
            throw new BusinessException("分类不存在");
        }

        // 检查是否有子分类
        Long childCount = lambdaQuery().eq(Category::getParentId, id).count();
        if (childCount > 0) {
            throw new BusinessException("分类下存在子分类，无法删除");
        }

        // 检查是否有关联文章（需要注入 ArticleService）
        // 简单处理：直接删除，数据库外键约束会处理关联

        removeById(id);
        log.info("删除分类成功：id={}", id);
    }
}
