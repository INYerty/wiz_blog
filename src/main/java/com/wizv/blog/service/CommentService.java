package com.wizv.blog.service;

import com.wizv.blog.entity.Comment;

import java.util.List;

public interface CommentService {

    /** 获取文章的树状评论列表 (两级) */
    List<Comment> getCommentTree(Long articleId);

    /** 发表评论 */
    Comment addComment(Comment comment);

    /** 删除评论 (仅本人或管理员) */
    void deleteComment(Long commentId, Long userId);
}
