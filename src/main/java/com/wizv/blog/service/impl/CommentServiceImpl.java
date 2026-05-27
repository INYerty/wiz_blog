package com.wizv.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.wizv.blog.common.enums.ResultCode;
import com.wizv.blog.common.exception.BusinessException;
import com.wizv.blog.entity.Article;
import com.wizv.blog.entity.Comment;
import com.wizv.blog.mapper.ArticleMapper;
import com.wizv.blog.mapper.CommentMapper;
import com.wizv.blog.service.CommentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final CommentMapper commentMapper;
    private final ArticleMapper articleMapper;

    @Override
    public List<Comment> getCommentTree(Long articleId) {
        // 1. 查顶级评论
        List<Comment> topComments = commentMapper.selectTopComments(articleId);
        // 2. 查所有子评论
        List<Comment> subComments = commentMapper.selectSubComments(articleId);

        // 3. 按 parentId 分组
        Map<Long, List<Comment>> subMap = subComments.stream()
                .collect(Collectors.groupingBy(Comment::getParentId));

        // 4. 组装两级树
        for (Comment top : topComments) {
            top.setChildren(subMap.getOrDefault(top.getId(), new ArrayList<>()));
        }

        return topComments;
    }

    @Override
    @Transactional
    public Comment addComment(Comment comment) {
        // 校验文章存在
        Article article = articleMapper.selectById(comment.getArticleId());
        if (article == null) {
            throw new BusinessException(ResultCode.ARTICLE_NOT_FOUND);
        }

        // 如果是子评论, 校验父评论存在且属于同一篇文章
        if (comment.getParentId() != null && comment.getParentId() > 0) {
            Comment parent = commentMapper.selectById(comment.getParentId());
            if (parent == null) {
                throw new BusinessException(ResultCode.COMMENT_NOT_FOUND, "父评论不存在");
            }
            if (!parent.getArticleId().equals(comment.getArticleId())) {
                throw new BusinessException(ResultCode.BAD_REQUEST, "父评论与文章不匹配");
            }
            // 只允许两级: 如果父评论本身也是子评论, 则把 parentId 指向顶级
            if (parent.getParentId() != null && parent.getParentId() > 0) {
                comment.setParentId(parent.getParentId());
            }
        } else {
            comment.setParentId(0L);
        }

        commentMapper.insert(comment);

        // 更新文章评论数
        articleMapper.update(null, new LambdaUpdateWrapper<Article>()
                .eq(Article::getId, comment.getArticleId())
                .setSql("comment_count = comment_count + 1"));

        return comment;
    }

    @Override
    @Transactional
    public void deleteComment(Long commentId, Long userId) {
        Comment comment = commentMapper.selectById(commentId);
        if (comment == null) {
            throw new BusinessException(ResultCode.COMMENT_NOT_FOUND);
        }
        if (!comment.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN, "只能删除自己的评论");
        }

        commentMapper.deleteById(commentId);

        // 更新文章评论数 -1
        articleMapper.update(null, new LambdaUpdateWrapper<Article>()
                .eq(Article::getId, comment.getArticleId())
                .setSql("comment_count = GREATEST(comment_count - 1, 0)"));
    }
}
