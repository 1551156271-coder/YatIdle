package com.yatidle.backend.common.exception;

import com.yatidle.backend.common.Result;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public Result<Void> handleBusinessException(BusinessException e) {
        Result<Void> r = new Result<>();
        r.setCode(400);
        r.setMessage(e.getMessage());
        return r;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Void> handleValidException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldError() == null
                ? "参数校验失败"
                : e.getBindingResult().getFieldError().getDefaultMessage();
        Result<Void> r = new Result<>();
        r.setCode(400);
        r.setMessage(message);
        return r;
    }

    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception e) {
        Result<Void> r = new Result<>();
        r.setCode(500);
        r.setMessage("服务器内部错误");
        return r;
    }
}
