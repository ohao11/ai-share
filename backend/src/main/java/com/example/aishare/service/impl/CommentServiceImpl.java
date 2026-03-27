package com.example.aishare.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.aishare.common.constants.SystemConstants;
import com.example.aishare.common.exception.BusinessException;
import com.example.aishare.dto.response.UserResponse;
import com.example.aishare.entity.Article;
import com.example.aishare.entity.Comment;
import com.example.aishare.mapper.ArticleMapper;
import com.example.aishare.mapper.CommentMapper;
import com.example.aishare.service.CommentService;
import com.example.aishare.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * 评论服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CommentServiceImpl extends ServiceImpl<CommentMapper, Comment> implements CommentService {

    private final ArticleMapper articleMapper;
    private final UserService userService;

    @Override
    public List<Comment> getCommentsByArticle(Long articleId) {
        return lambdaQuery()
                .eq(Comment::getArticleId, articleId)
                .eq(Comment::getStatus, SystemConstants.CommentStatus.APPROVED)
                .orderByAsc(Comment::getCreatedAt)
                .list();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Comment createComment(Long articleId, String content, Long parentId) {
        // 获取当前用户
        UserResponse currentUser = userService.getCurrentUser();

        Comment comment = new Comment();
        comment.setArticleId(articleId);
        comment.setContent(content);
        comment.setParentId(parentId);
        comment.setStatus(SystemConstants.CommentStatus.APPROVED); // TODO: 根据配置是否需要审核
        comment.setUserId(currentUser.getId());
        comment.setCreatedAt(OffsetDateTime.now());
        comment.setUpdatedAt(OffsetDateTime.now());

        save(comment);

        // 更新文章评论数
        articleMapper.update(null,
                new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<Article>()
                        .eq(Article::getId, articleId)
                        .setSql("comment_count = comment_count + 1"));

        return comment;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteComment(Long id) {
        Comment comment = getById(id);
        if (comment == null) {
            throw new BusinessException("评论不存在");
        }

        // 检查权限：只有评论作者才能删除
        UserResponse currentUser = userService.getCurrentUser();
        if (!comment.getUserId().equals(currentUser.getId())) {
            throw new BusinessException("无权删除此评论");
        }

        // 删除评论
        removeById(id);

        // 更新文章评论数
        articleMapper.update(null,
                new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<Article>()
                        .eq(Article::getId, comment.getArticleId())
                        .setSql("comment_count = GREATEST(0, comment_count - 1)"));

        log.info("删除评论成功：id={}, articleId={}", id, comment.getArticleId());
    }
}
