-- ============================================
-- Wiz_Blog 数据库建表脚本
-- Database: wiz_blog
-- Engine: InnoDB (支持事务)
-- Charset: utf8mb4 (支持 emoji)
-- ============================================

CREATE DATABASE IF NOT EXISTS wiz_blog
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE wiz_blog;

-- ============================================
-- 1. 用户表 (user)
-- ============================================
CREATE TABLE `user` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    `username`    VARCHAR(50)  NOT NULL COMMENT '用户名 (登录用)',
    `password`    VARCHAR(255) NOT NULL COMMENT '密码 (BCrypt加密)',
    `nickname`    VARCHAR(50)  NOT NULL COMMENT '昵称',
    `avatar`      VARCHAR(500) DEFAULT NULL COMMENT '头像URL',
    `email`       VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
    `role`        VARCHAR(20)  NOT NULL DEFAULT 'USER' COMMENT '角色: ADMIN / USER',
    `status`      TINYINT      NOT NULL DEFAULT 1 COMMENT '状态: 0-禁用 1-正常',
    `created_at`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`     TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0-未删 1-已删',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`),
    UNIQUE KEY `uk_email` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- ============================================
-- 2. 文章表 (article)
-- 设计要点:
--   - view_count / like_count / collect_count 为 MySQL 持久化值
--   - 实际高频读写走 Redis，定时任务异步刷回
--   - cover 字段存封面图 URL
--   - summary 用于列表页展示，避免加载全文
-- ============================================
CREATE TABLE `article` (
    `id`            BIGINT        NOT NULL AUTO_INCREMENT COMMENT '文章ID',
    `title`         VARCHAR(200)  NOT NULL COMMENT '文章标题',
    `summary`       VARCHAR(500)  DEFAULT NULL COMMENT '文章摘要 (列表页展示)',
    `content`       LONGTEXT      NOT NULL COMMENT '文章正文 (Markdown)',
    `content_html`  LONGTEXT      DEFAULT NULL COMMENT '渲染后的HTML (加速首屏)',
    `cover`         VARCHAR(500)  DEFAULT NULL COMMENT '封面图URL',
    `author_id`     BIGINT        NOT NULL COMMENT '作者ID',
    `category_id`   BIGINT        DEFAULT NULL COMMENT '分类ID',
    `tags`          VARCHAR(500)  DEFAULT NULL COMMENT '标签 (逗号分隔, 如: Java,Spring,Redis)',
    `view_count`    INT           NOT NULL DEFAULT 0 COMMENT '浏览量 (MySQL持久化, Redis为主)',
    `like_count`    INT           NOT NULL DEFAULT 0 COMMENT '点赞数',
    `collect_count` INT           NOT NULL DEFAULT 0 COMMENT '收藏数',
    `comment_count` INT           NOT NULL DEFAULT 0 COMMENT '评论数',
    `is_top`        TINYINT       NOT NULL DEFAULT 0 COMMENT '是否置顶: 0-否 1-是',
    `is_published`  TINYINT       NOT NULL DEFAULT 1 COMMENT '是否发布: 0-草稿 1-已发布',
    `status`        TINYINT       NOT NULL DEFAULT 1 COMMENT '状态: 0-下架 1-正常',
    `created_at`    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`       TINYINT       NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    KEY `idx_author_id` (`author_id`),
    KEY `idx_category_id` (`category_id`),
    KEY `idx_created_at` (`created_at`),
    KEY `idx_is_top_created` (`is_top` DESC, `created_at` DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文章表';

-- ============================================
-- 3. 分类表 (category)
-- ============================================
CREATE TABLE `category` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '分类ID',
    `name`        VARCHAR(50)  NOT NULL COMMENT '分类名称',
    `description` VARCHAR(200) DEFAULT NULL COMMENT '分类描述',
    `sort_order`  INT          NOT NULL DEFAULT 0 COMMENT '排序权重',
    `created_at`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `deleted`     TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='分类表';

-- ============================================
-- 4. 评论表 (comment) — 两级递归结构
-- 设计要点:
--   - parent_id = 0 表示顶级评论
--   - reply_to_user_id 用于 "回复 @某人" 的展示
--   - 只支持两级: 顶级评论 + 子评论 (子评论不再嵌套)
-- ============================================
CREATE TABLE `comment` (
    `id`               BIGINT       NOT NULL AUTO_INCREMENT COMMENT '评论ID',
    `article_id`       BIGINT       NOT NULL COMMENT '所属文章ID',
    `user_id`          BIGINT       NOT NULL COMMENT '评论者ID',
    `parent_id`        BIGINT       NOT NULL DEFAULT 0 COMMENT '父评论ID (0=顶级评论)',
    `reply_to_user_id` BIGINT       DEFAULT NULL COMMENT '回复目标用户ID (用于@展示)',
    `content`          TEXT         NOT NULL COMMENT '评论内容',
    `like_count`       INT          NOT NULL DEFAULT 0 COMMENT '评论点赞数',
    `status`           TINYINT      NOT NULL DEFAULT 1 COMMENT '状态: 0-隐藏 1-正常',
    `created_at`       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `deleted`          TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    KEY `idx_article_id` (`article_id`),
    KEY `idx_parent_id` (`parent_id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='评论表';

-- ============================================
-- 5. 点赞记录表 (like_record)
-- 用于防止重复点赞 + 点赞历史查询
-- ============================================
CREATE TABLE `like_record` (
    `id`          BIGINT   NOT NULL AUTO_INCREMENT COMMENT '记录ID',
    `user_id`     BIGINT   NOT NULL COMMENT '点赞用户ID',
    `target_id`   BIGINT   NOT NULL COMMENT '目标ID (文章ID或评论ID)',
    `target_type` TINYINT  NOT NULL COMMENT '目标类型: 1-文章 2-评论',
    `created_at`  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '点赞时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_target` (`user_id`, `target_id`, `target_type`),
    KEY `idx_target` (`target_id`, `target_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='点赞记录表';

-- ============================================
-- 6. 收藏记录表 (collect_record)
-- ============================================
CREATE TABLE `collect_record` (
    `id`          BIGINT   NOT NULL AUTO_INCREMENT COMMENT '记录ID',
    `user_id`     BIGINT   NOT NULL COMMENT '收藏用户ID',
    `article_id`  BIGINT   NOT NULL COMMENT '文章ID',
    `created_at`  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '收藏时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_article` (`user_id`, `article_id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='收藏记录表';

-- ============================================
-- Redis 键值策略说明 (不在SQL中执行, 仅供参考)
-- ============================================
-- 
-- | 用途                | Key 格式                          | Value 类型     | 说明                                     |
-- |---------------------|-----------------------------------|---------------|------------------------------------------|
-- | 文章浏览量(PV)       | article:view:{articleId}          | String (INT)  | INCR 原子递增, 定时刷回MySQL              |
-- | 文章独立访客(UV)     | article:uv:{articleId}            | HyperLogLog   | PFADD 去重, PFCOUNT 统计, 每日0点过期     |
-- | 文章点赞数           | article:like:{articleId}          | String (INT)  | INCR/DECR, 与 like_record 表双写          |
-- | 文章收藏数           | article:collect:{articleId}       | String (INT)  | INCR/DECR                                |
-- | 用户点赞状态         | user:liked:{userId}:{articleId}   | String (1/0)  | SET/GET, 快速判断是否已点赞, TTL 24h      |
-- | 每日UV标记           | daily:uv:{articleId}:{yyyyMMdd}   | HyperLogLog   | 按天去重, 防止跨天重复计数, TTL 48h       |
-- | 热门文章排行         | rank:hot:articles                 | ZSet           | score=viewCount, TOP20缓存               |
-- | 文章详情缓存         | cache:article:{articleId}         | Hash           | 文章详情JSON, TTL 30min, 减少DB查询       |
-- | 用户Token白名单      | token:whitelist:{userId}          | String (token) | 用于强制下线/单点登录, TTL=JWT过期时间     |
--

