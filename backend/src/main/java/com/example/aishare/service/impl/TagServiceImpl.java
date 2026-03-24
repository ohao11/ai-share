package com.example.aishare.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.aishare.entity.Tag;
import com.example.aishare.mapper.TagMapper;
import com.example.aishare.service.TagService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 标签服务实现
 */
@Slf4j
@Service
public class TagServiceImpl extends ServiceImpl<TagMapper, Tag> implements TagService {

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
}
