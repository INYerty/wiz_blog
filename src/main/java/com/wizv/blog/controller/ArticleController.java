package com.wizv.blog.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wizv.blog.common.result.Result;
import com.wizv.blog.entity.Article;
import com.wizv.blog.service.ArticleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 文章控制器
 *
 * <p>接口设计遵循 RESTful 风格:
 * GET    /api/articles          — 分页列表
 * GET    /api/articles/{id}     — 文章详情
 * POST   /api/articles          — 发布文章 (需登录)
 * PUT    /api/articles/{id}     — 更新文章 (需登录)
 * DELETE /api/articles/{id}     — 删除文章 (需登录)
 * POST   /api/articles/{id}/like    — 点赞/取消 (需登录)
 * POST   /api/articles/{id}/collect — 收藏/取消 (需登录)
 * </p>
 */
@RestController
@RequestMapping("/api/articles")
@RequiredArgsConstructor
public class ArticleController {

    private final ArticleService articleService;

    /** 分页查询文章列表 */
    @GetMapping
    public Result<IPage<Article>> getArticlePage(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String keyword) {
        return Result.success(articleService.getArticlePage(pageNum, pageSize, categoryId, keyword));
    }

    /** 获取文章详情 */
    @GetMapping("/{id}")
    public Result<Article> getArticleDetail(
            @PathVariable Long id,
            @RequestParam(required = false, defaultValue = "0") Long visitorId) {
        return Result.success(articleService.getArticleDetail(id, visitorId));
    }

    /** 发布文章 (需认证) */
    @PostMapping
    public Result<Article> publishArticle(@Valid @RequestBody Article article) {
        return Result.success("文章发布成功", articleService.publishArticle(article));
    }

    /** 更新文章 (需认证) */
    @PutMapping("/{id}")
    public Result<Void> updateArticle(@PathVariable Long id, @Valid @RequestBody Article article) {
        article.setId(id);
        articleService.updateArticle(article);
        return Result.success();
    }

    /** 删除文章 (需认证) */
    @DeleteMapping("/{id}")
    public Result<Void> deleteArticle(@PathVariable Long id, @RequestParam Long userId) {
        articleService.deleteArticle(id, userId);
        return Result.success();
    }

    /** 点赞/取消点赞 (需认证) */
    @PostMapping("/{id}/like")
    public Result<Boolean> toggleLike(@PathVariable Long id, @RequestParam Long userId) {
        boolean liked = articleService.toggleLike(id, userId);
        return Result.success(liked ? "点赞成功" : "已取消点赞", liked);
    }

    /** 收藏/取消收藏 (需认证) */
    @PostMapping("/{id}/collect")
    public Result<Boolean> toggleCollect(@PathVariable Long id, @RequestParam Long userId) {
        boolean collected = articleService.toggleCollect(id, userId);
        return Result.success(collected ? "收藏成功" : "已取消收藏", collected);
    }
}
