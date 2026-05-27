package com.wizv.blog.service.impl;

import com.wizv.blog.service.RedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Redis 服务实现
 *
 * <h3>键值策略总览:</h3>
 * <ul>
 *   <li>article:view:{id}          — String, PV 累计增量 (GETSET 原子刷回)</li>
 *   <li>article:like:{id}          — String, 点赞增量</li>
 *   <li>article:collect:{id}       — String, 收藏增量</li>
 *   <li>article:uv:{id}:{date}     — HyperLogLog, 每日去重访客 (TTL 48h)</li>
 *   <li>user:liked:{uid}:{aid}     — String "1", 用户点赞状态 (TTL 24h)</li>
 *   <li>rank:hot:articles          — ZSet, 热门文章排行</li>
 *   <li>article:dirty:view         — Set, 有 PV 变更的文章ID集合 (用于定时同步)</li>
 *   <li>article:dirty:like         — Set, 有赞变更的文章ID集合</li>
 *   <li>article:dirty:collect      — Set, 有收藏变更的文章ID集合</li>
 * </ul>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RedisServiceImpl implements RedisService {

    private final StringRedisTemplate redis;

    private static final String VIEW_KEY_PREFIX = "article:view:";
    private static final String LIKE_KEY_PREFIX = "article:like:";
    private static final String COLLECT_KEY_PREFIX = "article:collect:";
    private static final String UV_KEY_PREFIX = "article:uv:";
    private static final String USER_LIKED_PREFIX = "user:liked:";
    private static final String HOT_RANK_KEY = "rank:hot:articles";
    private static final String DIRTY_VIEW_SET = "article:dirty:view";
    private static final String DIRTY_LIKE_SET = "article:dirty:like";
    private static final String DIRTY_COLLECT_SET = "article:dirty:collect";
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyyMMdd");

    // ========== 浏览量 ==========

    @Override
    public void incrementViewCount(Long articleId, Long visitorId) {
        String key = VIEW_KEY_PREFIX + articleId;
        redis.opsForValue().increment(key);
        // 标记为脏数据, 等待定时任务刷回
        redis.opsForSet().add(DIRTY_VIEW_SET, articleId.toString());
        // UV 去重: HyperLogLog, 按天分 key
        String uvKey = UV_KEY_PREFIX + articleId + ":" + LocalDate.now().format(DATE_FMT);
        redis.opsForHyperLogLog().add(uvKey, visitorId.toString());
        redis.expire(uvKey, 48, TimeUnit.HOURS);
        // 更新热榜
        updateHotRank(articleId, getViewCount(articleId));
    }

    @Override
    public int getViewCount(Long articleId) {
        String val = redis.opsForValue().get(VIEW_KEY_PREFIX + articleId);
        return val != null ? Integer.parseInt(val) : 0;
    }

    // ========== UV ==========

    @Override
    public long getUniqueVisitorCount(Long articleId) {
        String uvKey = UV_KEY_PREFIX + articleId + ":" + LocalDate.now().format(DATE_FMT);
        Long count = redis.opsForHyperLogLog().size(uvKey);
        return count != null ? count : 0;
    }

    // ========== 点赞 ==========

    @Override
    public void incrementLikeCount(Long articleId) {
        redis.opsForValue().increment(LIKE_KEY_PREFIX + articleId);
        redis.opsForSet().add(DIRTY_LIKE_SET, articleId.toString());
    }

    @Override
    public void decrementLikeCount(Long articleId) {
        redis.opsForValue().decrement(LIKE_KEY_PREFIX + articleId);
        redis.opsForSet().add(DIRTY_LIKE_SET, articleId.toString());
    }

    @Override
    public int getLikeCount(Long articleId) {
        String val = redis.opsForValue().get(LIKE_KEY_PREFIX + articleId);
        return val != null ? Math.max(0, Integer.parseInt(val)) : 0;
    }

    @Override
    public void setUserLikeStatus(Long userId, Long articleId) {
        String key = USER_LIKED_PREFIX + userId + ":" + articleId;
        redis.opsForValue().set(key, "1", 24, TimeUnit.HOURS);
    }

    @Override
    public void removeUserLikeStatus(Long userId, Long articleId) {
        redis.delete(USER_LIKED_PREFIX + userId + ":" + articleId);
    }

    @Override
    public boolean hasUserLiked(Long userId, Long articleId) {
        return Boolean.TRUE.equals(redis.hasKey(USER_LIKED_PREFIX + userId + ":" + articleId));
    }

    // ========== 收藏 ==========

    @Override
    public void incrementCollectCount(Long articleId) {
        redis.opsForValue().increment(COLLECT_KEY_PREFIX + articleId);
        redis.opsForSet().add(DIRTY_COLLECT_SET, articleId.toString());
    }

    @Override
    public void decrementCollectCount(Long articleId) {
        redis.opsForValue().decrement(COLLECT_KEY_PREFIX + articleId);
        redis.opsForSet().add(DIRTY_COLLECT_SET, articleId.toString());
    }

    @Override
    public int getCollectCount(Long articleId) {
        String val = redis.opsForValue().get(COLLECT_KEY_PREFIX + articleId);
        return val != null ? Math.max(0, Integer.parseInt(val)) : 0;
    }

    // ========== 缓存管理 ==========

    @Override
    public void initArticleCounters(Long articleId, int viewCount, int likeCount, int collectCount) {
        redis.opsForValue().set(VIEW_KEY_PREFIX + articleId, String.valueOf(viewCount));
        redis.opsForValue().set(LIKE_KEY_PREFIX + articleId, String.valueOf(likeCount));
        redis.opsForValue().set(COLLECT_KEY_PREFIX + articleId, String.valueOf(collectCount));
    }

    @Override
    public void evictArticleCache(Long articleId) {
        // 如有详情缓存 Hash, 在此清除
        log.debug("清除文章缓存: articleId={}", articleId);
    }

    // ========== 同步相关 (供定时任务调用) ==========

    @Override
    public Set<String> getArticleIdsWithViewCount() {
        return redis.opsForSet().members(DIRTY_VIEW_SET);
    }

    @Override
    public int getAndResetViewCountIncr(Long articleId) {
        // GETSET 原子操作: 获取当前值并重置为 0
        String val = redis.opsForValue().getAndSet(VIEW_KEY_PREFIX + articleId, "0");
        redis.opsForSet().remove(DIRTY_VIEW_SET, articleId.toString());
        return val != null ? Integer.parseInt(val) : 0;
    }

    @Override
    public int getAndResetLikeCountIncr(Long articleId) {
        String val = redis.opsForValue().getAndSet(LIKE_KEY_PREFIX + articleId, "0");
        redis.opsForSet().remove(DIRTY_LIKE_SET, articleId.toString());
        return val != null ? Integer.parseInt(val) : 0;
    }

    @Override
    public int getAndResetCollectCountIncr(Long articleId) {
        String val = redis.opsForValue().getAndSet(COLLECT_KEY_PREFIX + articleId, "0");
        redis.opsForSet().remove(DIRTY_COLLECT_SET, articleId.toString());
        return val != null ? Integer.parseInt(val) : 0;
    }

    @Override
    public void updateHotRank(Long articleId, double score) {
        redis.opsForZSet().add(HOT_RANK_KEY, articleId.toString(), score);
        // 只保留 TOP 100
        redis.opsForZSet().removeRange(HOT_RANK_KEY, 0, -101);
    }
}
