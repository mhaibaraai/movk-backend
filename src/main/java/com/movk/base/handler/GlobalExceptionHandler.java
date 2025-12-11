/*
 * @Author yixuanmiao
 * @Date 2025/09/02 13:20
 */

package com.movk.base.handler;

import com.movk.base.exception.BusinessException;
import com.movk.base.result.R;
import com.movk.base.result.RCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.ConversionFailedException;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

/**
 * 全局异常处理器
 * 统一处理应用中抛出的各种异常，提供标准化的错误响应
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理业务异常
     * @param ex 业务异常
     * @param req HTTP请求
     * @return 标准错误响应
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<R<?>> handleBusinessException(BusinessException ex, HttpServletRequest req) {
        log.warn("BusinessException: method={}, uri={}, code={}, message={}", req.getMethod(), req.getRequestURI(), ex.getCode().getCode(), ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(R.error(ex.getCode(), ex.getMessage()));
    }

    /**
     * 处理缺少请求参数异常
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<R<?>> handleMissingServletRequestParameter(MissingServletRequestParameterException ex, HttpServletRequest req) {
        log.warn("MissingServletRequestParameter: method={}, uri={}, param={}", req.getMethod(), req.getRequestURI(), ex.getParameterName());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(R.error(RCode.BAD_REQUEST, null));
    }

    /**
     * 处理HTTP消息不可读异常（如JSON格式错误）
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<R<?>> handleHttpMessageNotReadable(HttpMessageNotReadableException ex, HttpServletRequest req) {
        ex.getMostSpecificCause();
        String reason = ex.getMostSpecificCause().getMessage();
        log.warn("HttpMessageNotReadable: method={}, uri={}, reason={}", req.getMethod(), req.getRequestURI(), reason);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(R.error(RCode.BAD_REQUEST, null));
    }

    /**
     * 处理方法参数类型不匹配异常
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<R<?>> handleMethodArgumentTypeMismatch(MethodArgumentTypeMismatchException ex, HttpServletRequest req) {
        log.warn("MethodArgumentTypeMismatch: method={}, uri={}, name={}, value={}", req.getMethod(), req.getRequestURI(), ex.getName(), ex.getValue());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(R.error(RCode.BAD_REQUEST, null));
    }

    /**
     * 处理HTTP请求方法不支持异常
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<R<?>> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex, HttpServletRequest req) {
        log.warn("MethodNotSupported: method={}, uri={}, supported={}", req.getMethod(), req.getRequestURI(), ex.getSupportedHttpMethods());
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(R.error(RCode.METHOD_NOT_ALLOWED, null));
    }

    /**
     * 处理媒体类型不支持异常
     */
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<R<?>> handleMediaTypeNotSupported(HttpMediaTypeNotSupportedException ex, HttpServletRequest req) {
        log.warn("MediaTypeNotSupported: method={}, uri={}, contentType={}", req.getMethod(), req.getRequestURI(), ex.getContentType());
        return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE).body(R.error(RCode.UNSUPPORTED_MEDIA_TYPE, null));
    }

    /**
     * 处理类型转换失败异常
     */
    @ExceptionHandler(ConversionFailedException.class)
    public ResponseEntity<R<?>> handleConversionFailed(ConversionFailedException ex, HttpServletRequest req) {
        log.warn("ConversionFailed: method={}, uri={}, value={}, targetType={}", req.getMethod(), req.getRequestURI(), ex.getValue(), ex.getTargetType());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(R.error(RCode.BAD_REQUEST, null));
    }

    /**
     * 处理数据访问异常（数据库相关）
     */
    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<R<?>> handleDataAccess(DataAccessException ex, HttpServletRequest req) {
        log.error("DataAccessException: method={}, uri={}", req.getMethod(), req.getRequestURI(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(R.error(RCode.DATABASE_ERROR, null));
    }

    /**
     * 处理找不到处理器异常（404错误）
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<R<?>> handleNoHandlerFound(NoHandlerFoundException ex, HttpServletRequest req) {
        log.warn("NoHandlerFound: method={}, uri={}", req.getMethod(), req.getRequestURI(), ex);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(R.error(RCode.NOT_FOUND, null));
    }

    /**
     * 处理Spring Security认证异常
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<R<?>> handleAuthentication(AuthenticationException ex, HttpServletRequest req) {
        log.warn("AuthenticationException: method={}, uri={}, message={}", req.getMethod(), req.getRequestURI(), ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(R.error(RCode.UNAUTHORIZED, null));
    }

    /**
     * 处理Spring Security授权异常
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<R<?>> handleAccessDenied(AccessDeniedException ex, HttpServletRequest req) {
        log.warn("AccessDeniedException: method={}, uri={}, message={}", req.getMethod(), req.getRequestURI(), ex.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(R.error(RCode.FORBIDDEN, null));
    }

    /**
     * 处理凭证错误异常
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<R<?>> handleBadCredentials(BadCredentialsException ex, HttpServletRequest req) {
        log.warn("BadCredentialsException: method={}, uri={}, message={}", req.getMethod(), req.getRequestURI(), ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(R.error(RCode.INVALID_CREDENTIALS, null));
    }

    /**
     * 处理方法参数校验异常
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<R<?>> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpServletRequest req) {
        log.warn("MethodArgumentNotValid: method={}, uri={}, errors={}", req.getMethod(), req.getRequestURI(), ex.getBindingResult().getAllErrors());
        
        // 获取第一个验证错误信息
        String errorMessage = null;
        if (!ex.getBindingResult().getAllErrors().isEmpty()) {
            errorMessage = ex.getBindingResult().getAllErrors().get(0).getDefaultMessage();
        }
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(R.error(RCode.VALIDATION_FAILED, errorMessage));
    }

    /**
     * 处理约束违反异常（@Validated 校验）
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<R<?>> handleConstraintViolation(ConstraintViolationException ex, HttpServletRequest req) {
        String errorMessage = ex.getConstraintViolations().stream()
            .map(ConstraintViolation::getMessage)
            .collect(Collectors.joining("; "));
        log.warn("ConstraintViolation: method={}, uri={}, message={}", req.getMethod(), req.getRequestURI(), errorMessage);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(R.error(RCode.VALIDATION_FAILED, errorMessage));
    }

    /**
     * 处理参数绑定异常
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<R<?>> handleIllegalArgument(IllegalArgumentException ex, HttpServletRequest req) {
        log.warn("IllegalArgumentException: method={}, uri={}, message={}", req.getMethod(), req.getRequestURI(), ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(R.error(RCode.BAD_REQUEST, ex.getMessage()));
    }

    /**
     * 处理所有未被捕获的异常（兜底处理）
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<R<?>> handleUnhandled(Exception ex, HttpServletRequest req) {
        log.error("UnhandledException: method={}, uri={}", req.getMethod(), req.getRequestURI(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(R.error(RCode.INTERNAL_ERROR, null));
    }
}
