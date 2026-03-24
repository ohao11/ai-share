package com.example.aishare.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.aishare.entity.Comment;

import java.util.List;

/**
 * 评论服务接口
 */
public interface CommentService extends IService<Comment> {

    /**
     * 获取文章评论列表
     */
    List<Comment> getCommentsByArticle(Long articleId);

    /**
     * 发表评论
     */
    Comment createComment(Long articleId, String content, Long parentId);
}
