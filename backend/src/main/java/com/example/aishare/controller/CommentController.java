package com.example.aishare.controller;

import com.example.aishare.common.result.Result;
import com.example.aishare.dto.request.CommentCreateRequest;
import com.example.aishare.entity.Comment;
import com.example.aishare.service.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 评论控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/articles/{articleId}/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    /**
     * 获取文章评论列表
     */
    @GetMapping
    public Result<List<Comment>> getComments(@PathVariable Long articleId) {
        return Result.success(commentService.getCommentsByArticle(articleId));
    }

    /**
     * 发表评论
     */
    @PostMapping
    public Result<Comment> createComment(
            @PathVariable Long articleId,
            @Valid @RequestBody CommentCreateRequest request) {
        Comment comment = commentService.createComment(articleId, request.getContent(), request.getParentId());
        return Result.success(comment);
    }

    /**
     * 删除评论
     */
    @DeleteMapping("/{commentId}")
    public Result<Void> deleteComment(@PathVariable Long commentId) {
        commentService.deleteComment(commentId);
        return Result.success();
    }
}
