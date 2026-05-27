package com.wizv.blog.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 统一响应状态码枚举
 * 规范前后端交互的每一个错误场景, 杜绝模糊的 "系统错误"
 */
@Getter
@AllArgsConstructor
public enum ResultCode {

    // ========== 成功 ==========
    SUCCESS(200, "操作成功"),

    // ========== 客户端错误 4xx ==========
    BAD_REQUEST(400, "请求参数错误"),
    UNAUTHORIZED(401, "未登录或Token已过期"),
    FORBIDDEN(403, "无权限访问"),
    NOT_FOUND(404, "资源不存在"),
    METHOD_NOT_ALLOWED(405, "请求方法不允许"),
    CONFLICT(409, "资源冲突 (如重复点赞)"),

    // ========== 服务端错误 5xx ==========
    INTERNAL_ERROR(500, "服务器内部错误"),
    DATABASE_ERROR(501, "数据库操作异常"),
    REDIS_ERROR(502, "缓存服务异常"),

    // ========== 业务错误 1xxx ==========
    USER_NOT_FOUND(1001, "用户不存在"),
    USERNAME_EXISTS(1002, "用户名已存在"),
    PASSWORD_WRONG(1003, "密码错误"),
    ACCOUNT_DISABLED(1004, "账号已被禁用"),

    ARTICLE_NOT_FOUND(1101, "文章不存在"),
    ARTICLE_OFFLINE(1102, "文章已下架"),

    COMMENT_NOT_FOUND(1201, "评论不存在"),
    COMMENT_CONTENT_EMPTY(1202, "评论内容不能为空"),

    ALREADY_LIKED(1301, "已点赞过该文章"),
    NOT_LIKED(1302, "尚未点赞该文章"),
    ALREADY_COLLECTED(1303, "已收藏过该文章"),
    NOT_COLLECTED(1304, "尚未收藏该文章");

    private final int code;
    private final String message;
}
