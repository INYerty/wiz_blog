package com.wizv.blog.task;

import com.wizv.blog.mapper.ArticleMapper;
import com.wizv.blog.service.RedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedisSyncTask {

    private final RedisService redisService;
    private final ArticleMapper articleMapper;

    /**
     * 每 5 分钟执行一次: 将 Redis 中的浏览量增量刷回 MySQL
     * 注意：去掉了类或方法上的 @Transactional！
     */
    @Scheduled(fixedRate = 300_000) // 5 min
    public void syncArticleCounters() {
        log.debug("开始同步 Redis → MySQL 计数数据...");

        // 1. 在事务外部进行 Redis 网络 I/O，此时不占用任何 MySQL 连接
        Set<String> dirtyViewIds = redisService.getArticleIdsWithViewCount();
        if (dirtyViewIds == null || dirtyViewIds.isEmpty()) {
            return;
        }

        int syncedCount = 0;

        // 2. 遍历处理。由于去掉了方法级别的事务，每次循环不会锁死连接池
        for (String idStr : dirtyViewIds) {
            try {
                Long articleId = Long.parseLong(idStr);
                // 依然是原子操作
                int viewIncr = redisService.getAndResetViewCountIncr(articleId);

                if (viewIncr > 0) {
                    // 每次更新是一条独立的 SQL，利用 MySQL 自身的行锁，即用即释放
                    articleMapper.incrViewCount(articleId, viewIncr);
                    syncedCount++;
                }
            } catch (Exception e) {
                log.error("同步单篇文章 {} 计数失败，跳过并继续", idStr, e);
            }
        }

        if (syncedCount > 0) {
            log.info("Redis → MySQL 同步完成, 更新了 {} 篇文章的浏览量", syncedCount);
        }
    }

    /**
     * 每天凌晨 0:00 执行: 全量校准 (兜底策略)
     * 同样去掉了 @Transactional
     */
    @Scheduled(cron = "0 0 0 * * ?")
    public void fullSyncCalibration() {
        log.info("开始每日全量校准...");
        // TODO: 具体的全量校准逻辑
        log.info("每日全量校准完成");
    }
}