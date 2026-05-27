package com.wizv.blog.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("article")
public class Article {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String title;

    private String summary;

    private String content;

    private String contentHtml;

    private String cover;

    private Long authorId;

    private Long categoryId;

    private String tags;

    private Integer viewCount;

    private Integer likeCount;

    private Integer collectCount;

    private Integer commentCount;

    private Integer isTop;

    private Integer isPublished;

    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableLogic
    private Integer deleted;

    // ========== 非数据库字段 ==========

    /** 作者昵称 (关联查询) */
    @TableField(exist = false)
    private String authorName;

    /** 作者头像 (关联查询) */
    @TableField(exist = false)
    private String authorAvatar;

    /** 分类名称 (关联查询) */
    @TableField(exist = false)
    private String categoryName;
}
