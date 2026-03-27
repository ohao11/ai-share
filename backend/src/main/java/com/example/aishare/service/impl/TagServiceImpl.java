package com.example.aishare.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.aishare.common.exception.BusinessException;
import com.example.aishare.entity.Tag;
import com.example.aishare.mapper.ArticleTagMapper;
import com.example.aishare.mapper.TagMapper;
import com.example.aishare.service.TagService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 标签服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TagServiceImpl extends ServiceImpl<TagMapper, Tag> implements TagService {

    private final ArticleTagMapper articleTagMapper;

    @Override
    public List<Tag> getHotTags(Integer limit) {
        return lambdaQuery()
                .orderByDesc(Tag::getCreatedAt)
                .last("LIMIT " + limit)
                .list();
    }

    @Override
    public Tag getBySlug(String slug) {
        return lambdaQuery()
                .eq(Tag::getSlug, slug)
                .one();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Tag createTag(Tag tag) {
        // 检查名称是否已存在
        Long count = lambdaQuery()
                .eq(Tag::getName, tag.getName())
                .count();
        if (count > 0) {
            throw new BusinessException("标签名称已存在");
        }

        // 检查 Slug 是否已存在
        count = lambdaQuery().eq(Tag::getSlug, tag.getSlug()).count();
        if (count > 0) {
            throw new BusinessException("标签 Slug 已存在");
        }

        save(tag);
        log.info("创建标签成功：id={}, name={}, slug={}", tag.getId(), tag.getName(), tag.getSlug());
        return tag;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Tag updateTag(Tag tag) {
        Tag existingTag = getById(tag.getId());
        if (existingTag == null) {
            throw new BusinessException("标签不存在");
        }

        // 检查名称是否已存在（排除当前标签）
        Long count = lambdaQuery()
                .eq(Tag::getName, tag.getName())
                .ne(Tag::getId, tag.getId())
                .count();
        if (count > 0) {
            throw new BusinessException("标签名称已存在");
        }

        // 检查 Slug 是否已存在（排除当前标签）
        count = lambdaQuery()
                .eq(Tag::getSlug, tag.getSlug())
                .ne(Tag::getId, tag.getId())
                .count();
        if (count > 0) {
            throw new BusinessException("标签 Slug 已存在");
        }

        updateById(tag);
        log.info("更新标签成功：id={}, name={}, slug={}", tag.getId(), tag.getName(), tag.getSlug());
        return tag;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteTag(Long id) {
        Tag tag = getById(id);
        if (tag == null) {
            throw new BusinessException("标签不存在");
        }

        // 删除文章-标签关联
        articleTagMapper.deleteByTagId(id);

        // 删除标签
        removeById(id);
        log.info("删除标签成功：id={}, name={}", id, tag.getName());
    }
}
