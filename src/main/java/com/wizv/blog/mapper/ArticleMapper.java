package com.wizv.blog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wizv.blog.entity.Article;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface ArticleMapper extends BaseMapper<Article> {

    /**
     * 分页查询文章列表 (关联作者和分类信息)
     * 列表页不查 content 字段, 减少传输量
     */
    @Select("""
        SELECT a.id, a.title, a.summary, a.cover, a.author_id, a.category_id,
               a.tags, a.view_count, a.like_count, a.collect_count, a.comment_count,
               a.is_top, a.is_published, a.status, a.created_at, a.updated_at,
               u.nickname AS author_name, u.avatar AS author_avatar,
               c.name AS category_name
        FROM article a
        LEFT JOIN user u ON a.author_id = u.id
        LEFT JOIN category c ON a.category_id = c.id
        WHERE a.deleted = 0 AND a.is_published = 1
        ORDER BY a.is_top DESC, a.created_at DESC
    """)
    IPage<Article> selectArticlePage(Page<Article> page);

    /**
     * 查询文章详情 (含全文)
     */
    @Select("""
        SELECT a.*, u.nickname AS author_name, u.avatar AS author_avatar,
               c.name AS category_name
        FROM article a
        LEFT JOIN user u ON a.author_id = u.id
        LEFT JOIN category c ON a.category_id = c.id
        WHERE a.id = #{id} AND a.deleted = 0
    """)
    Article selectArticleDetail(@Param("id") Long id);

    /**
     * 批量将 Redis 中的计数增量同步到 MySQL
     * 使用 view_count = view_count + #{incr} 原子操作, 避免并发覆盖
     */
    @Update("UPDATE article SET view_count = view_count + #{incr} WHERE id = #{id}")
    int incrViewCount(@Param("id") Long id, @Param("incr") int incr);

    @Update("UPDATE article SET like_count = GREATEST(like_count + #{incr}, 0) WHERE id = #{id}")
    int incrLikeCount(@Param("id") Long id, @Param("incr") int incr);

    @Update("UPDATE article SET collect_count = GREATEST(collect_count + #{incr}, 0) WHERE id = #{id}")
    int incrCollectCount(@Param("id") Long id, @Param("incr") int incr);
}
