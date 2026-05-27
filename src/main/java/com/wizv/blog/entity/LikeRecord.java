package com.wizv.blog.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("like_record")
public class LikeRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long targetId;

    /** 目标类型: 1-文章 2-评论 */
    private Integer targetType;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
