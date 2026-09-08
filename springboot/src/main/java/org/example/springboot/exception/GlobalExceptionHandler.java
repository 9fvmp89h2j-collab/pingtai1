package org.example.springboot.exception;

import jakarta.validation.ConstraintViolationException;
import org.example.springboot.common.Result;
import org.example.springboot.common.ResultCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(BusinessException.class)
    public Result<?> handleBusinessException(BusinessException e) {
        log.warn("Business exception: {}", e.getMessage());
        return Result.error(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(ServiceException.class)
    public Result<?> handleServiceException(ServiceException e) {
        log.error("Service exception: {}", e.getMessage(), e);
        return Result.error(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<?> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldError() == null
                ? "请求参数不正确"
                : e.getBindingResult().getFieldError().getDefaultMessage();
        return Result.error(ResultCode.VALIDATE_FAILED.getCode(), message);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public Result<?> handleConstraintViolationException(ConstraintViolationException e) {
        return Result.error(ResultCode.VALIDATE_FAILED.getCode(), e.getMessage());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public Result<?> handleDataIntegrityViolationException(DataIntegrityViolationException e) {
        log.error("Data integrity violation", e);
        String message = e.getMessage();
        if (message != null && message.contains("Data too long for column 'doctordetail'")) {
            return Result.error(ResultCode.VALIDATE_FAILED.getCode(), "故事详解内容过长，请精简后再保存");
        }
        if (message != null && message.contains("Data too long for column 'readingglossary'")) {
            return Result.error(ResultCode.VALIDATE_FAILED.getCode(), "重点词汇库内容过长，请精简后再保存");
        }
        return Result.error(ResultCode.VALIDATE_FAILED.getCode(), "数据不符合约束，请检查后重试");
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<Result<?>> handleNoResourceFoundException(NoResourceFoundException e) {
        log.warn("Resource not found: {}", e.getResourcePath());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Result.error("404", "请求的资源不存在"));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Result<?>> handleException(Exception e) {
        log.error("Unhandled server error", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Result.error(ResultCode.SYSTEM_ERROR.getCode(), "系统错误"));
    }
}
