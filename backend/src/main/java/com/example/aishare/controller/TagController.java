package com.example.aishare.controller;

import com.example.aishare.common.result.Result;
import com.example.aishare.entity.Tag;
import com.example.aishare.service.TagService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 标签控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/tags")
@RequiredArgsConstructor
public class TagController {

    private final TagService tagService;

    /**
     * 获取标签列表
     */
    @GetMapping
    public Result<List<Tag>> getTags() {
        return Result.success(tagService.list());
    }

    /**
     * 获取热门标签
     */
    @GetMapping("/hot")
    public Result<List<Tag>> getHotTags(
            @RequestParam(defaultValue = "10") Integer limit) {
        return Result.success(tagService.getHotTags(limit));
    }

    /**
     * 根据 ID 获取标签
     */
    @GetMapping("/id/{id}")
    public Result<Tag> getTagById(@PathVariable Long id) {
        return Result.success(tagService.getById(id));
    }

    /**
     * 根据 Slug 获取标签
     */
    @GetMapping("/{slug}")
    public Result<Tag> getTagBySlug(@PathVariable String slug) {
        return Result.success(tagService.getBySlug(slug));
    }

    /**
     * 创建标签
     */
    @PostMapping
    public Result<Tag> createTag(@RequestBody Tag tag) {
        return Result.success(tagService.createTag(tag));
    }

    /**
     * 更新标签
     */
    @PutMapping("/{id}")
    public Result<Tag> updateTag(@PathVariable Long id, @RequestBody Tag tag) {
        tag.setId(id);
        return Result.success(tagService.updateTag(tag));
    }

    /**
     * 删除标签
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteTag(@PathVariable Long id) {
        tagService.deleteTag(id);
        return Result.success();
    }
}
