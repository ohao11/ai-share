package com.example.aishare.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.aishare.entity.Tag;

import java.util.List;

/**
 * 标签服务接口
 */
public interface TagService extends IService<Tag> {

    /**
     * 获取热门标签
     */
    List<Tag> getHotTags(Integer limit);

    /**
     * 根据 Slug 获取标签
     */
    Tag getBySlug(String slug);
}
