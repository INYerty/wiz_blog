package com.wizv.blog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wizv.blog.entity.Comment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface CommentMapper extends BaseMapper<Comment> {

    /**
     * 查询某篇文章的顶级评论 (parent_id = 0)
     */
    @Select("""
        SELECT cm.*, u.nickname AS username, u.avatar AS user_avatar
        FROM comment cm
        LEFT JOIN user u ON cm.user_id = u.id
        WHERE cm.article_id = #{articleId} AND cm.parent_id = 0 AND cm.deleted = 0
        ORDER BY cm.created_at DESC
    """)
    List<Comment> selectTopComments(@Param("articleId") Long articleId);

    /**
     * 查询某篇文章的所有子评论 (parent_id > 0)
     */
    @Select("""
        SELECT cm.*, u.nickname AS username, u.avatar AS user_avatar,
               ru.nickname AS reply_to_username
        FROM comment cm
        LEFT JOIN user u ON cm.user_id = u.id
        LEFT JOIN user ru ON cm.reply_to_user_id = ru.id
        WHERE cm.article_id = #{articleId} AND cm.parent_id > 0 AND cm.deleted = 0
        ORDER BY cm.created_at ASC
    """)
    List<Comment> selectSubComments(@Param("articleId") Long articleId);
}
