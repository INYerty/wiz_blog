-- ==================================================
-- 1. 插入新的分类
-- ==================================================
INSERT INTO `category` (`name`, `description`, `sort_order`) VALUES
('生活随笔', '记录生活中的点滴感悟和有趣故事', 20),
('项目实战', '从零到一的完整项目开发过程记录', 30),
('源码阅读', '深入剖析知名开源框架的底层原理', 40),
('面试总结', '整理常见的面试问题和解题思路', 50);

-- ==================================================
-- 2. 将现有文章关联到新分类
-- ==================================================
-- 请将下面的 `article_id_x` 替换为您 `article` 表中实际的文章 ID。
-- 您可以按需修改 `category_id`，使其指向上面创建的分类。
-- 上面创建的分类 ID 通常会从 1 开始自增 (1=技术杂谈, 2=生活随笔, ...)。

-- 示例:
-- UPDATE `article` SET `category_id` = 1 WHERE `id` = 101; -- 将 ID 为 101 的文章分到"技术杂谈"
-- UPDATE `article` SET `category_id` = 2 WHERE `id` = 102; -- 将 ID 为 102 的文章分到"生活随笔"

-- 请在这里写下您自己的更新语句:
-- UPDATE `article` SET `category_id` = [您的分类ID] WHERE `id` = [您的文章ID];
-- UPDATE `article` SET `category_id` = [您的分类ID] WHERE `id` = [您的文章ID];
-- UPDATE `article` SET `category_id` = [您的分类ID] WHERE `id` = [您的文章ID];

-- 提示: 如果您不确定文章和分类的ID，可以分别查询 `article` 和 `category` 表来确认。
SELECT * FROM `article`;
SELECT * FROM `category`;

