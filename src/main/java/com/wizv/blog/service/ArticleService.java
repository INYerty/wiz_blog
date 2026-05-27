package com.wizv.blog.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wizv.blog.entity.Article;

/**
 * 文章服务接口
 */
public interface ArticleService {

    /** 分页查询文章列表 (不加载正文) */
    IPage<Article> getArticlePage(int pageNum, int pageSize, Long categoryId, String keyword);

    /** 获取文章详情 (含全文 + Redis 实时计数) */
    Article getArticleDetail(Long articleId, Long visitorId);

    /** 发布文章 */
    Article publishArticle(Article article);

    /** 更新文章 */
    void updateArticle(Article article);

    /** 删除文章 (逻辑删除) */
    void deleteArticle(Long articleId, Long userId);

    /** 点赞/取消点赞 */
    boolean toggleLike(Long articleId, Long userId);

    /** 收藏/取消收藏 */
    boolean toggleCollect(Long articleId, Long userId);
}
