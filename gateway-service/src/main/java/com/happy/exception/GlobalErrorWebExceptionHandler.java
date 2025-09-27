package com.happy.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.happy.result.GlobalResult;
import com.happy.result.SystemErrorType;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

/**
 * 网关全局异常处理器 - 统一包装为 GlobalResult 格式
 * 处理路由失败、服务熔断、超时、连接拒绝等
 * 映射异常到 SystemErrorType，确保返回格式与业务层一致
 * 适配 Spring 6+，使用 HttpStatusCode 接口
 *
 *
 * Gateway 是 Reactive（WebFlux），必须用 ErrorWebExceptionHandler
 * 业务服务是传统 Servlet（MVC），用 @ControllerAdvice
 */
@Component
@Order(-2) // 优先级高于默认异常处理器
public class GlobalErrorWebExceptionHandler implements ErrorWebExceptionHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        // 📝 生产环境应使用日志框架记录异常，如：log.error("网关异常", ex);
        ex.printStackTrace(); // 仅用于调试

        // 🧩 构造统一响应对象
        GlobalResult<String> result = buildErrorResponse(ex);

        try {
            // 🔄 序列化为 JSON 字符串
            String json = objectMapper.writeValueAsString(result);
            byte[] bytes = json.getBytes(StandardCharsets.UTF_8);

            // 🎯 设置响应头
            ServerHttpResponse response = exchange.getResponse();
            response.setStatusCode(getHttpStatus(ex)); // 设置 HTTP 状态码
            response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
            response.getHeaders().setContentLength(bytes.length);

            // 📤 写入响应体
            DataBuffer buffer = response.bufferFactory().wrap(bytes);
            return response.writeWith(Mono.just(buffer));

        } catch (Exception e) {
            e.printStackTrace();
            return Mono.error(e);
        }
    }

    /**
     * 根据异常类型构建 GlobalResult，映射到 SystemErrorType
     */
    private GlobalResult<String> buildErrorResponse(Throwable ex) {
        // 1. 处理 ResponseStatusException（如：转发失败、服务返回 4xx/5xx）
        if (ex instanceof ResponseStatusException) {
            ResponseStatusException rse = (ResponseStatusException) ex;
            HttpStatusCode statusCode = rse.getStatusCode(); // ✅ Spring 6 正确用法
            SystemErrorType errorType = mapHttpStatusCodeToErrorType(statusCode);
            String message = rse.getReason() != null ? rse.getReason() : errorType.getMessage();
            return GlobalResult.error(errorType.getCode(), message);
        }

        // 2. 根据异常消息关键词匹配（fallback）
        String msg = ex.getMessage() != null ? ex.getMessage() : "未知错误";

        if (msg.contains("Connection refused") || msg.contains("拒绝连接")) {
            return GlobalResult.error(SystemErrorType.CONNECTION_REFUSED);
        }
        if (msg.contains("timeout") || msg.contains("Timeout") || msg.contains("超时")) {
            return GlobalResult.error(SystemErrorType.GATEWAY_TIMEOUT);
        }
        if (msg.contains("404") || msg.contains("Not Found") || msg.contains("无法找到")) {
            return GlobalResult.error(SystemErrorType.ROUTE_NOT_FOUND);
        }
        if (msg.contains("502") || msg.contains("Bad Gateway")) {
            return GlobalResult.error(SystemErrorType.BAD_GATEWAY);
        }

        // 3. 默认：系统错误
        return GlobalResult.error(SystemErrorType.SYSTEM_ERROR);
    }

    /**
     * 将 HttpStatusCode 映射到 SystemErrorType
     */
    private SystemErrorType mapHttpStatusCodeToErrorType(HttpStatusCode status) {
        int code = status.value(); // 获取数字状态码，如 404, 500

        switch (code) {
            case 400:
                return SystemErrorType.PARAMS_ERROR;
            case 401:
                return SystemErrorType.UNAUTHORIZED;
            case 403:
                return SystemErrorType.FORBIDDEN;
            case 404:
                return SystemErrorType.ROUTE_NOT_FOUND;
            case 405:
                return SystemErrorType.METHOD_NOT_ALLOWED;
            case 429:
                return SystemErrorType.RATE_LIMIT_EXCEEDED;
            case 500:
                return SystemErrorType.SYSTEM_ERROR;
            case 502:
                return SystemErrorType.BAD_GATEWAY;
            case 503:
                return SystemErrorType.SERVICE_UNAVAILABLE;
            case 504:
                return SystemErrorType.GATEWAY_TIMEOUT;
            default:
                return SystemErrorType.SYSTEM_ERROR;
        }
    }

    /**
     * 根据异常推断应返回的 HTTP 状态码（用于设置响应头）
     */
    private HttpStatusCode getHttpStatus(Throwable ex) {
        if (ex instanceof ResponseStatusException) {
            return ((ResponseStatusException) ex).getStatusCode();
        }

        GlobalResult<String> result = buildErrorResponse(ex);
        int code = result.getCode();

        // 优先尝试转换为标准 HTTP 状态码
        if (code >= 100 && code < 600) {
            try {
                return HttpStatus.valueOf(code);
            } catch (Exception ignored) {}
        }

        // 否则，根据错误类型决定默认状态码
        if (code == SystemErrorType.CONNECTION_REFUSED.getCode()) {
            return HttpStatus.BAD_GATEWAY;
        }
        if (code == SystemErrorType.GATEWAY_TIMEOUT.getCode()) {
            return HttpStatus.GATEWAY_TIMEOUT;
        }
        if (code == SystemErrorType.SERVICE_UNAVAILABLE.getCode()) {
            return HttpStatus.SERVICE_UNAVAILABLE;
        }

        return HttpStatus.INTERNAL_SERVER_ERROR;
    }
}