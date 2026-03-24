package com.example.aishare.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.aishare.common.constants.SystemConstants;
import com.example.aishare.entity.Comment;
import com.example.aishare.mapper.CommentMapper;
import com.example.aishare.service.CommentService;
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
        Comment comment = new Comment();
        comment.setArticleId(articleId);
        comment.setContent(content);
        comment.setParentId(parentId);
        comment.setStatus(SystemConstants.CommentStatus.APPROVED); // TODO: 根据配置是否需要审核
        comment.setUserId(1L); // TODO: 从当前用户获取
        comment.setCreatedAt(OffsetDateTime.now());
        comment.setUpdatedAt(OffsetDateTime.now());

        save(comment);

        // 更新文章评论数
        // TODO: 使用原子操作更新 article 表的 comment_count

        return comment;
    }
}
