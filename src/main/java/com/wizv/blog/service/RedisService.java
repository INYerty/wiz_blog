package com.wizv.blog.service;

import java.util.Set;

/**
 * Redis 服务接口
 * 封装所有 Redis 操作, 屏蔽键名细节
 */
public interface RedisService {

    // ========== 浏览量 (PV) ==========

    /** 浏览量 +1, 同时记录 UV */
    void incrementViewCount(Long articleId, Long visitorId);

    /** 获取实时浏览量 */
    int getViewCount(Long articleId);

    // ========== 独立访客 (UV) ==========

    /** 获取独立访客数 */
    long getUniqueVisitorCount(Long articleId);

    // ========== 点赞 ==========

    void incrementLikeCount(Long articleId);
    void decrementLikeCount(Long articleId);
    int getLikeCount(Long articleId);

    void setUserLikeStatus(Long userId, Long articleId);
    void removeUserLikeStatus(Long userId, Long articleId);
    boolean hasUserLiked(Long userId, Long articleId);

    // ========== 收藏 ==========

    void incrementCollectCount(Long articleId);
    void decrementCollectCount(Long articleId);
    int getCollectCount(Long articleId);

    // ========== 缓存管理 ==========

    /** 初始化文章计数器 (发布文章时调用) */
    void initArticleCounters(Long articleId, int viewCount, int likeCount, int collectCount);

    /** 清除文章详情缓存 */
    void evictArticleCache(Long articleId);

    // ========== 同步相关 ==========

    /** 获取所有有浏览量增量的文章ID */
    Set<String> getArticleIdsWithViewCount();

    /** 获取并重置浏览量增量 (原子操作) */
    int getAndResetViewCountIncr(Long articleId);

    /** 获取并重置点赞增量 */
    int getAndResetLikeCountIncr(Long articleId);

    /** 获取并重置收藏增量 */
    int getAndResetCollectCountIncr(Long articleId);

    /** 更新热门文章排行榜 */
    void updateHotRank(Long articleId, double score);
}
