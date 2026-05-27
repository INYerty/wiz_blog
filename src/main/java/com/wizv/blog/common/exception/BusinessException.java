package com.wizv.blog.common.exception;

import com.wizv.blog.common.enums.ResultCode;
import lombok.Getter;

/**
 * 自定义业务异常
 * <p>
 * 所有业务逻辑中的错误都抛此异常, 由 GlobalExceptionHandler 统一捕获并转为 Result 响应
 * 绝不让 Controller 层出现 try-catch 散落各处
 * </p>
 */
@Getter
public class BusinessException extends RuntimeException {

    private final int code;

    public BusinessException(ResultCode resultCode) {
        super(resultCode.getMessage());
        this.code = resultCode.getCode();
    }

    public BusinessException(ResultCode resultCode, String message) {
        super(message);
        this.code = resultCode.getCode();
    }

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }
}
