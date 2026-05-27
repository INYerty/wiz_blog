package com.wizv.blog.controller;

import com.wizv.blog.common.result.Result;
import com.wizv.blog.entity.Comment;
import com.wizv.blog.service.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 评论控制器
 */
@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    /** 获取文章的树状评论列表 */
    @GetMapping("/article/{articleId}")
    public Result<List<Comment>> getCommentTree(@PathVariable Long articleId) {
        return Result.success(commentService.getCommentTree(articleId));
    }

    /** 发表评论 (需认证) */
    @PostMapping
    public Result<Comment> addComment(@Valid @RequestBody Comment comment) {
        return Result.success("评论成功", commentService.addComment(comment));
    }

    /** 删除评论 (需认证, 仅本人或管理员) */
    @DeleteMapping("/{id}")
    public Result<Void> deleteComment(@PathVariable Long id, @RequestParam Long userId) {
        commentService.deleteComment(id, userId);
        return Result.success();
    }
}
