package com.happy.config.ratelimit;


import com.happy.utils.JwtUtil;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import reactor.core.publisher.Mono;

import java.net.InetSocketAddress;
import java.util.Optional;

/**
 * gateway的限流过滤器 + redis实现限流的限流配置类
 * 定义 KeyResolver：按 IP 限流
 */
@Configuration
public class RateLimiterConfig {

    /**
     * 1、按客户端 IP 限流
     *
     * @return KeyResolver
     */
    @Bean
    @Primary
    public KeyResolver ipKeyResolver() {
        return exchange -> {
            // 优先从 X-Forwarded-For 头获取
            String xff = exchange.getRequest().getHeaders().getFirst("X-Forwarded-For");
            if (xff != null && !xff.isEmpty() && !"unknown".equalsIgnoreCase(xff)) {
                // X-Forwarded-For 可能是 "client, proxy1, proxy2"，取第一个
                String clientIp = xff.split(",")[0].trim();
                return Mono.just(clientIp);
            }

            // 其次尝试 X-Real-IP
            String xri = exchange.getRequest().getHeaders().getFirst("X-Real-IP");
            if (xri != null && !xri.isEmpty()) {
                return Mono.just(xri.trim());
            }

            // 最后 fallback 到远程地址
            InetSocketAddress remoteAddress = exchange.getRequest().getRemoteAddress();
            if (remoteAddress != null) {
                System.out.println("【限流Key】IP = " + remoteAddress.getAddress().getHostAddress()); // 打印出来确认
                return Mono.just(remoteAddress.getAddress().getHostAddress());
            }

            // 安全兜底
            return Mono.just("unknown");
        };
    }

    // 简化写法（Lambda）：
    // @Bean
    // public KeyResolver ipKeyResolver() {
    //     return exchange -> Mono.just(
    //         exchange.getRequest().getRemoteAddress().getAddress().getHostAddress()
    //     );
    // }
    // 2、按照userid进行限流，需要配置key-resolver: "#{@userKeyResolver}"
    @Bean
    public KeyResolver userKeyResolver() {
        return exchange -> {
            // 1. 获取 Authorization 头
            String authHeader = exchange.getRequest().getHeaders().getFirst("Authorization");

            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7); // 去掉 "Bearer "

                try {
                    // 2. 解析 JWT 获取 userId
                    String userId = JwtUtil.getUserIdFromToken(token);
                    System.out.println("【限流Key】userId = " + userId); // 打印出来确认
                    if (userId != null && !userId.isEmpty()) {
                        return Mono.just(userId);
                    }
                } catch (Exception e) {
                    // 解析失败，可能是非法 Token
                    return Mono.just("invalid_user");
                }
            }

            // 3. 未登录用户统一限流
            return Mono.just("anonymous");
        };
    }

    /**
     * 3、按请求路径（Path）限流
     * 所有访问相同路径的请求，共用同一个限流桶
     * 例如：/api/user/login 被 100 个不同 IP 访问，也只算一次请求
     *
     * @return KeyResolver
     */
    @Bean
    public KeyResolver pathKeyResolver() {
        return exchange -> {
            // 获取请求路径，如：/api/user/login
            String path = exchange.getRequest().getURI().getPath();
            System.out.println("【限流Key】请求路径 = " + path); // 打印日志方便调试
            return Mono.just(path);
        };
    }

    /**
     * 4、按照请求头中的 App-Key 进行限流
     * <p>
     * 适用场景：
     * - 第三方应用接入 API 网关
     * - 多租户系统中按租户限流
     * - 开放平台控制调用方配额
     * <p>
     * 请求示例：
     * GET /api/users/123
     * Headers:
     * App-Key: abc123-secret-key-xyz
     *
     * @return KeyResolver
     */
    @Bean
    public KeyResolver appKeyResolver() {
        return exchange -> {
            // 1. 获取请求头中的 App-Key
            String appKey = exchange.getRequest().getHeaders().getFirst("App-Key");

            // 2. 校验 App-Key 是否存在
            if (appKey == null || appKey.trim().isEmpty()) {
                System.out.println("【限流Key】App-Key 不存在，使用默认 key: anonymous-app");
                // 可返回 error 拒绝请求，或统一走 anonymous 限流
                return Mono.just("anonymous-app");
            }

            // 3. 去除前后空格，防止伪造
            appKey = appKey.trim();
            System.out.println("【限流Key】App-Key = " + appKey); // 打印日志，方便调试

            // 4. 返回 App-Key 作为限流的唯一标识
            return Mono.just(appKey);
        };
    }

    /**
     * 5、按照请求参数中的 tenantId 进行限流
     * <p>
     * 适用场景：
     * - SaaS 多租户系统
     * - 按客户维度控制 API 调用频率
     * - 防止某个租户高频调用影响其他租户
     * <p>
     * 支持传参方式：
     * /api/data?tenantId=company_a
     * 或
     * 在params中添加 tenantId=company_a
     * <p>
     * <p>
     * 注意：
     * ❌ 不支持 JSON Body 中的 tenantId（如 {"tenantId": "xxx"}）
     * 因为 Gateway 无法在不读取 Body 的情况下获取 JSON 字段
     * 如需支持，需结合自定义 PreFilter 缓存 Body
     *
     * @return KeyResolver
     */
//    @Bean
//    public KeyResolver tenantKeyResolver() {
//        return exchange -> Mono
//                .justOrEmpty(exchange.getRequest().getQueryParams().getFirst("tenantId"))
//                .map(String::trim)
//                .filter(id -> !id.isEmpty())
//                .switchIfEmpty(Mono.just("default"));
//    }
    @Bean
    public KeyResolver tenantKeyResolver() {
        return exchange -> {
            // 第一步：从请求头中获取 X-Tenant-ID
            String rawTenantId = exchange.getRequest().getQueryParams().getFirst("tenantId");
            // 第二步：包装成一个可能为空的 Mono
            // 如果 rawTenantId 是 null，Mono 就是 empty；否则就是 Mono.just(rawTenantId)
            Mono<String> tenantIdMono = Mono.justOrEmpty(rawTenantId);

            // 第三步：对值进行 trim() 操作（去掉前后空格）
            // map 是“转换”操作：把 String 转成 trim() 后的 String
            Mono<String> trimmedMono = tenantIdMono.map(id -> id.trim());

            // 第四步：过滤，只保留非空字符串
            // 如果 trim 后是空字符串（比如全是空格），就变成 empty
            Mono<String> filteredMono = trimmedMono.filter(id -> !id.isEmpty());

            // 第五步：如果前面的结果是 empty（即没取到有效值），就用 "default" 作为默认值
            Mono<String> result = filteredMono.switchIfEmpty(Mono.just("default"));

            // 返回最终的 Mono<String>
            return result;
        };
    }

    /**
     * 6、按照 ip + path 组合限流
     */

    @Bean
    public KeyResolver ipPathKeyResolver() {
        return exchange -> {
            // 获取真实 IP
            String ip = Optional.ofNullable(exchange.getRequest().getHeaders().getFirst("X-Forwarded-For"))
                    .filter(s -> !s.isEmpty() && !"unknown".equalsIgnoreCase(s))
                    .map(s -> s.split(",")[0].trim())
                    .orElseGet(() -> {
                        InetSocketAddress remoteAddress = exchange.getRequest().getRemoteAddress();
                        return remoteAddress != null ? remoteAddress.getAddress().getHostAddress() : "unknown";
                    });

            // 获取路径
            String path = exchange.getRequest().getURI().getPath();

            // 组合 Key
            String key = ip + "_" + path;

            return Mono.just(key);
        };
    }


}