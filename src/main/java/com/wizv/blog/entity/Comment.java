package com.wizv.blog.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@TableName("comment")
public class Comment {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long articleId;

    private Long userId;

    private Long parentId;

    private Long replyToUserId;

    private String content;

    private Integer likeCount;

    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableLogic
    private Integer deleted;

    // ========== 非数据库字段 ==========

    /** 评论者昵称 */
    @TableField(exist = false)
    private String username;

    /** 评论者头像 */
    @TableField(exist = false)
    private String userAvatar;

    /** 回复目标用户昵称 (用于 @展示) */
    @TableField(exist = false)
    private String replyToUsername;

    /** 子评论列表 (两级递归) */
    @TableField(exist = false)
    private List<Comment> children;
}
