package com.happy.exception;


import com.happy.result.GlobalResult;
import com.happy.result.SystemErrorType;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

/**
 * 创建全局异常处理器模板：实现统一异常处理
 * 对网关层的限流无效。
 * 处理业务逻辑异常、参数校验、权限等
 * RequestRateLimiter 是 Gateway 层的过滤器，它在请求到达你应用的 Spring MVC 层之前就已经拦截并返回了响应。
 * 换句话说：这个异常根本没有进入你的 common-web 模块的 @ControllerAdvice！
 * <p>
 * Gateway 是 Reactive（WebFlux），必须用 ErrorWebExceptionHandler
 * 业务服务是传统 Servlet（MVC），用 @ControllerAdvice
 */
@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * 处理所有未捕获的 Exception（兜底）
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public GlobalResult<?> handleException(Exception e, HttpServletRequest request) {
        log.error("全局异常处理器捕获异常：", e);
        return GlobalResult.error(SystemErrorType.SYSTEM_ERROR);
    }

    /**
     * 处理自定义业务异常
     */
    @ExceptionHandler(BusinessException.class)
    public GlobalResult<?> handleBusinessException(BusinessException e) {
        log.error("业务异常：", e);
        return GlobalResult.error(e.getCode(), e.getMessage());
    }

    /**
     * 处理参数校验异常（@Validated）
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public GlobalResult<?> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        log.error("参数校验异常：", e);
        List<FieldError> fieldErrors = e.getBindingResult().getFieldErrors();
        StringBuilder sb = new StringBuilder();
        for (FieldError fieldError : fieldErrors) {
            sb.append(fieldError.getDefaultMessage()).append("; ");
        }
        return GlobalResult.error(SystemErrorType.PARAMS_ERROR.getCode(), sb.toString());
    }

    /**
     * 处理 BindException（如 @Valid on @RequestParam）
     */
    @ExceptionHandler(BindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public GlobalResult<?> handleBindException(BindException e) {
        log.error("绑定异常：", e);
        List<FieldError> fieldErrors = e.getBindingResult().getFieldErrors();
        StringBuilder sb = new StringBuilder();
        for (FieldError fieldError : fieldErrors) {
            sb.append(fieldError.getDefaultMessage()).append("; ");
        }
        return GlobalResult.error(SystemErrorType.PARAMS_ERROR.getCode(), sb.toString());
    }

    /**
     * 处理空指针等特定异常
     */
    @ExceptionHandler(NullPointerException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public GlobalResult<?> handleNPE(NullPointerException e) {
        log.error("空指针异常：", e);
        return GlobalResult.error(SystemErrorType.SYSTEM_ERROR.getCode(), "空指针异常");
    }

    /**
     * 处理 ResponseStatusException（如 401、403、404 等）
     * 保留原始状态码，并包装为 GlobalResult
     */
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<GlobalResult<?>> handleResponseStatusException(ResponseStatusException e) {
        log.warn("捕获状态码异常：{} - {}", e.getStatusCode(), e.getReason(), e);

        SystemErrorType errorType = mapHttpStatusToErrorType(e.getStatusCode());

        String message = e.getReason() != null ? e.getReason() : errorType.getMessage();
        GlobalResult<?> result = GlobalResult.error(errorType.getCode(), message);

        return ResponseEntity.status(e.getStatusCode()).body(result);
    }

    /**
     * 映射 HttpStatusCode 到 SystemErrorType
     * 兼容 HttpStatus 枚举和自定义状态码
     */
    private SystemErrorType mapHttpStatusToErrorType(HttpStatusCode status) {
        if (status instanceof HttpStatus httpStatus) {
            return switch (httpStatus) {
                case UNAUTHORIZED -> SystemErrorType.UNAUTHORIZED;
                case FORBIDDEN -> SystemErrorType.FORBIDDEN;
                case BAD_REQUEST -> SystemErrorType.PARAMS_ERROR;
                case NOT_FOUND -> SystemErrorType.ROUTE_NOT_FOUND;
                case METHOD_NOT_ALLOWED -> SystemErrorType.METHOD_NOT_ALLOWED;
                case INTERNAL_SERVER_ERROR -> SystemErrorType.SYSTEM_ERROR;
                case SERVICE_UNAVAILABLE -> SystemErrorType.SERVICE_UNAVAILABLE;
                default -> SystemErrorType.SYSTEM_ERROR;
            };
        }

        // 处理非 HttpStatus 的情况（比如自定义状态码）
        return switch (status.value()) {
            case 401 -> SystemErrorType.UNAUTHORIZED;
            case 403 -> SystemErrorType.FORBIDDEN;
            case 400 -> SystemErrorType.PARAMS_ERROR;
            case 404 -> SystemErrorType.ROUTE_NOT_FOUND;
            case 405 -> SystemErrorType.METHOD_NOT_ALLOWED;
            case 500 -> SystemErrorType.SYSTEM_ERROR;
            case 503 -> SystemErrorType.SERVICE_UNAVAILABLE;
            default -> SystemErrorType.SYSTEM_ERROR;
        };
    }



    // 可继续添加其他异常处理...
}
