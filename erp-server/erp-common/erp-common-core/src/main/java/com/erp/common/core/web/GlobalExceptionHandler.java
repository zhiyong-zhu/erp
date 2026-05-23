package com.erp.common.core.web;

import com.erp.common.core.domain.R;
import com.erp.common.core.exception.BizException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(BizException.class)
    public R<Void> handleBizException(BizException ex) {
        log.warn("Business exception: code={}, message={}", ex.getCode(), ex.getMessage());
        return R.fail(ex.getCode(), ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public R<Void> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldError() != null
                ? ex.getBindingResult().getFieldError().getDefaultMessage()
                : "参数校验失败";
        log.warn("Validation exception: {}", message);
        return R.fail(10004, message);
    }

    @ExceptionHandler(Exception.class)
    public R<Void> handleException(Exception ex) {
        log.error("Unhandled exception", ex);
        return R.fail(10006, "服务器内部错误");
    }
}
