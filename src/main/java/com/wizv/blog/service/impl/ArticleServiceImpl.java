package com.wizv.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wizv.blog.common.enums.ResultCode;
import com.wizv.blog.common.exception.BusinessException;
import com.wizv.blog.entity.Article;
import com.wizv.blog.entity.CollectRecord;
import com.wizv.blog.entity.LikeRecord;
import com.wizv.blog.mapper.ArticleMapper;
import com.wizv.blog.mapper.CollectRecordMapper;
import com.wizv.blog.mapper.LikeRecordMapper;
import com.wizv.blog.service.ArticleService;
import com.wizv.blog.service.RedisService;
import com.wizv.blog.util.MarkdownUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class ArticleServiceImpl implements ArticleService {

    private final ArticleMapper articleMapper;
    private final LikeRecordMapper likeRecordMapper;
    private final CollectRecordMapper collectRecordMapper;
    private final RedisService redisService;

    @Override
    public IPage<Article> getArticlePage(int pageNum, int pageSize, Long categoryId, String keyword) {
        Page<Article> page = new Page<>(pageNum, pageSize);
        IPage<Article> articlePage;
        // 如果有筛选条件, 可以用 MyBatis-Plus 的 wrapper
        // 这里简化处理, 直接走 Mapper 的自定义 SQL
        if (categoryId == null && !StringUtils.hasText(keyword)) {
            articlePage = articleMapper.selectArticlePage(page);
        } else {
            LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<Article>()
                    .eq(Article::getIsPublished, 1)
                    .eq(categoryId != null, Article::getCategoryId, categoryId)
                    .like(StringUtils.hasText(keyword), Article::getTitle, keyword)
                    .orderByDesc(Article::getIsTop)
                    .orderByDesc(Article::getCreatedAt);
            articlePage = articleMapper.selectPage(page, wrapper);
        }

        // 从 Redis 获取最新的浏览量, 覆盖从数据库查出的旧数据
        articlePage.getRecords().forEach(article -> {
            article.setViewCount(redisService.getViewCount(article.getId()));
        });
        return articlePage;
    }

    @Override
    public Article getArticleDetail(Long articleId, Long visitorId) {
        Article article = articleMapper.selectArticleDetail(articleId);
        if (article == null) {
            throw new BusinessException(ResultCode.ARTICLE_NOT_FOUND);
        }
        if (article.getStatus() == 0) {
            throw new BusinessException(ResultCode.ARTICLE_OFFLINE);
        }

        // 浏览量 +1 (走 Redis, 不直接操作 MySQL)
        redisService.incrementViewCount(articleId, visitorId);

        // 浏览量用 Redis 实时数据，点赞/收藏已同步写 MySQL，直接读即可
        article.setViewCount(redisService.getViewCount(articleId));

        return article;
    }

    @Override
    @Transactional
    public Article publishArticle(Article article) {
        article.setViewCount(0);
        article.setLikeCount(0);
        article.setCollectCount(0);
        article.setCommentCount(0);
        // 将 Markdown 内容转换为 HTML
        article.setContentHtml(MarkdownUtil.toHtml(article.getContent()));
        articleMapper.insert(article);

        // 初始化 Redis 计数器
        redisService.initArticleCounters(article.getId(), 0, 0, 0);
        log.info("文章发布成功: id={}, title={}", article.getId(), article.getTitle());
        return article;
    }

    @Override
    @Transactional
    public void updateArticle(Article article) {
        Article existing = articleMapper.selectById(article.getId());
        if (existing == null) {
            throw new BusinessException(ResultCode.ARTICLE_NOT_FOUND);
        }
        // 更新时同样转换 Markdown
        article.setContentHtml(MarkdownUtil.toHtml(article.getContent()));
        articleMapper.updateById(article);
        // 清除文章详情缓存, 下次读取获取最新数据
        redisService.evictArticleCache(article.getId());
    }

    @Override
    @Transactional
    public void deleteArticle(Long articleId, Long userId) {
        Article article = articleMapper.selectById(articleId);
        if (article == null) {
            throw new BusinessException(ResultCode.ARTICLE_NOT_FOUND);
        }
        if (!article.getAuthorId().equals(userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN, "只能删除自己的文章");
        }
        articleMapper.deleteById(articleId);
        redisService.evictArticleCache(articleId);
    }

    @Override
    @Transactional
    public boolean toggleLike(Long articleId, Long userId) {
        // 检查文章是否存在
        Article article = articleMapper.selectById(articleId);
        if (article == null) {
            throw new BusinessException(ResultCode.ARTICLE_NOT_FOUND);
        }

        // 查 MySQL 点赞记录
        LambdaQueryWrapper<LikeRecord> wrapper = new LambdaQueryWrapper<LikeRecord>()
                .eq(LikeRecord::getUserId, userId)
                .eq(LikeRecord::getTargetId, articleId)
                .eq(LikeRecord::getTargetType, 1); // 1=文章
        LikeRecord record = likeRecordMapper.selectOne(wrapper);

        if (record != null) {
            // 已点赞 → 取消点赞
            likeRecordMapper.deleteById(record.getId());
            redisService.decrementLikeCount(articleId);
            redisService.removeUserLikeStatus(userId, articleId);
            // 同步 MySQL
            articleMapper.incrLikeCount(articleId, -1);
            return false; // 返回 false 表示取消
        } else {
            // 未点赞 → 点赞
            LikeRecord newRecord = new LikeRecord();
            newRecord.setUserId(userId);
            newRecord.setTargetId(articleId);
            newRecord.setTargetType(1);
            likeRecordMapper.insert(newRecord);
            redisService.incrementLikeCount(articleId);
            redisService.setUserLikeStatus(userId, articleId);
            // 同步 MySQL
            articleMapper.incrLikeCount(articleId, 1);
            return true; // 返回 true 表示点赞
        }
    }

    @Override
    @Transactional
    public boolean toggleCollect(Long articleId, Long userId) {
        Article article = articleMapper.selectById(articleId);
        if (article == null) {
            throw new BusinessException(ResultCode.ARTICLE_NOT_FOUND);
        }

        LambdaQueryWrapper<CollectRecord> wrapper = new LambdaQueryWrapper<CollectRecord>()
                .eq(CollectRecord::getUserId, userId)
                .eq(CollectRecord::getArticleId, articleId);
        CollectRecord record = collectRecordMapper.selectOne(wrapper);

        if (record != null) {
            collectRecordMapper.deleteById(record.getId());
            redisService.decrementCollectCount(articleId);
            articleMapper.incrCollectCount(articleId, -1);
            return false;
        } else {
            CollectRecord newRecord = new CollectRecord();
            newRecord.setUserId(userId);
            newRecord.setArticleId(articleId);
            collectRecordMapper.insert(newRecord);
            redisService.incrementCollectCount(articleId);
            articleMapper.incrCollectCount(articleId, 1);
            return true;
        }
    }
}
