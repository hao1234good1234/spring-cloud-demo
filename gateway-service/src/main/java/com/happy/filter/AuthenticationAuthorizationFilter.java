package com.happy.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.happy.constant.CommonConstant;
import com.happy.result.GlobalResult;
import com.happy.result.SystemErrorType;
import com.happy.utils.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 全局认证与授权过滤器
 * 功能：
 * - JWT 认证
 * - 基于角色的访问控制（支持多角色）
 * - 用户信息透传（X-Auth-User, X-Auth-Roles）
 * - 支持路由元数据配置 requiredRoles
 * 登录返回 token
 * 所有 /api/** 接口需要带 Authorization: Bearer xxx
 * 角色不足返回 403
 */
@Order(CommonConstant.AuthFilter_ORDER) // 优先级第二
@Component
public class AuthenticationAuthorizationFilter implements GlobalFilter {
    //在 AuthFilter 中注入 ObjectMapper
    @Autowired
    private ObjectMapper objectMapper; // Spring Boot 自动配置

    private static final Logger log = LoggerFactory.getLogger(AuthenticationAuthorizationFilter.class);

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    private static final List<String> AUTH_WHITELIST = Arrays.asList(
            "/auth/login",
            "/auth/register",
            "/login",
            "/register",
            "/doc.html",           // Knife4j UI
            "/webjars/",           // Swagger UI 静态资源
            "/swagger-resources",  // Swagger 资源发现
            "/v3/api-docs",        // OpenAPI 3.0 文档
            "/v2/api-docs",        // Swagger 2.0 文档（兼容）
            "/csrf"                // 可选，防止 CSRF 干扰
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();

        // 1. 放行 OPTIONS 预检请求
        if (request.getMethod() == HttpMethod.OPTIONS) {
            return chain.filter(exchange);
        }

        // 2. 放行白名单接口
        String path = request.getURI().getPath();
        if (isWhitelisted(path)) {
            return chain.filter(exchange);
        }

        // 3. 获取并验证 JWT Token
        String token = getTokenFromRequest(request);
        if (token == null) {
            return setResponse(exchange, HttpStatus.UNAUTHORIZED,
                    SystemErrorType.MISSING_TOKEN.getCode(), SystemErrorType.MISSING_TOKEN.getMessage());
        }

        if (!JwtUtil.validateToken(token)) {
            return setResponse(exchange, HttpStatus.UNAUTHORIZED,
                    SystemErrorType.INVALID_TOKEN.getCode(), SystemErrorType.INVALID_TOKEN.getMessage());
        }

        // =======================
        // 4. 提取并解析 Authorization Header
        // =======================
        String authHeader = request.getHeaders().getFirst("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        token = authHeader.substring(7); // 去掉 "Bearer "

        // 5. 解析 Token 获取用户信息
        String username;
        Collection<String> roles;
        try {
            username = JwtUtil.getUsernameFromToken(token);
            roles = JwtUtil.getRolesFromToken(token);
        } catch (Exception e) {
            return setResponse(exchange, HttpStatus.UNAUTHORIZED,
                    SystemErrorType.INVALID_TOKEN.getCode(), SystemErrorType.INVALID_TOKEN.getMessage());
        }

        if (username == null || roles.isEmpty()) {
            return setResponse(exchange, HttpStatus.FORBIDDEN,
                    SystemErrorType.INVALID_TOKEN.getCode(), SystemErrorType.INVALID_TOKEN.getMessage());
        }

        // 6. 👉 获取当前请求匹配的路由
        Route route = exchange.getAttribute(org.springframework.cloud.gateway.support.ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR);
        if (route == null) {
            return setResponse(exchange, HttpStatus.INTERNAL_SERVER_ERROR,
                    SystemErrorType.ROUTE_NOT_FOUND.getCode(), SystemErrorType.ROUTE_NOT_FOUND.getMessage());
        }

        // 7. 👉 从路由元数据中获取 requiredRoles
        Set<String> requiredRoles = getRequiredRolesFromRoute(route);
        if (!requiredRoles.isEmpty()) {
            // 检查用户是否有至少一个所需角色（OR 逻辑）
            boolean hasAccess = roles.stream().anyMatch(requiredRoles::contains);
            if (!hasAccess) {
                return setResponse(exchange, HttpStatus.FORBIDDEN,
                        SystemErrorType.UNAUTHORIZED.getCode(), SystemErrorType.UNAUTHORIZED.getMessage());
            }
        }
        // 如果 requiredRoles 为空 → 表示无需特定角色，放行

        // 8. ✅ 鉴权通过 → 透传用户信息给下游服务
        //在 Controller 中
        //public ResponseEntity<User> getProfile(@RequestHeader("X-Auth-User") String username,@RequestHeader("X-Auth-Roles") String roles)
        ServerHttpRequest modifiedRequest = request.mutate()
                .header("X-Auth-User", username)
                .header("X-Auth-Roles", String.join(",", roles)) // 逗号分隔传递
                .build();
        // 测试redis连接
//        testRedisConnection();

        return chain.filter(exchange.mutate().request(modifiedRequest).build());
    }


    //  当请求包含"/auth/login","/auth/register","/login","/register"，直接放行，否则进行JWT验证
    private boolean isWhitelisted(String path) {
        return AUTH_WHITELIST.stream().anyMatch(path::contains);
    }

    private String getTokenFromRequest(ServerHttpRequest request) {
        String authHeader = request.getHeaders().getFirst("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }

    private Set<String> getRequiredRolesFromRoute(Route route) {
        Map<String, Object> metadata = route.getMetadata();
        Object roles = metadata.get("requiredRoles");
        if (roles == null) return Collections.emptySet();

        if (roles instanceof Collection) {
            return ((Collection<?>) roles).stream()
                    .map(Object::toString)
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toSet());
        } else if (roles instanceof String) {
            // 支持字符串格式： "ADMIN, OPERATOR"
            return Arrays.stream(((String) roles).split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toSet());
        }
        return Collections.emptySet();
    }

    private Mono<Void> setResponse(ServerWebExchange exchange, HttpStatus status, Integer code, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(status);
        response.getHeaders().add("Content-Type", "application/json;charset=UTF-8");

        // 使用 Result + ObjectMapper 序列化
        GlobalResult<?> globalResult = GlobalResult.error(code, message);
        String body;
        try {
            body = objectMapper.writeValueAsString(globalResult);
        } catch (Exception e) {
            // 备用方案：防止序列化失败
            body = String.format("{\"code\":%d,\"success\":false,\"message\":\"%s\"}", status.value(), "服务器内部错误");
        }

        DataBuffer buffer = response.bufferFactory().wrap(body.getBytes(StandardCharsets.UTF_8));
        return response.writeWith(Mono.just(buffer));
    }

    // 测试redis 连接
//    public void testRedisConnection() {
//        try {
//            // 测试 Redis 连接
//            String result = redisTemplate.opsForValue().get("testKey");
//            log.info("Redis connection test: {}", result == null ? "Key not found" : result);
//
//            // 设置一个测试键值对
//            redisTemplate.opsForValue().set("testKey", "testValue");
//            log.info("Set testKey to testValue");
//
//            // 再次获取测试键值对
//            result = redisTemplate.opsForValue().get("testKey");
//            log.info("Redis connection test after set: {}", result);
//        } catch (Exception e) {
//            log.error("Failed to connect to Redis", e);
//        }
//    }
}