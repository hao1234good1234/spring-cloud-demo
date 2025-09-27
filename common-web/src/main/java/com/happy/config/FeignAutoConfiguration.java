package com.happy.config;

import feign.Feign;
import feign.RequestInterceptor;
import io.seata.core.context.RootContext;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Arrays;
import java.util.List;

// 这个配置的作用是：feign调用的服务之间实现数据透传
@Configuration
@ConditionalOnClass(Feign.class)
@EnableConfigurationProperties
public class FeignAutoConfiguration {

    private static final List<String> HEADERS_TO_PROPAGATE = Arrays.asList(
            "X-Auth-User",
            "X-Auth-Roles",
            "X-Request-Id",
            "X-Trace-Id",
            "Authorization"
    );

    @Bean
    @ConditionalOnMissingBean // 避免重复注册
    public RequestInterceptor requestInterceptor() {
        return template -> {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes == null) return;

            HttpServletRequest request = attributes.getRequest();

            // 自动透传指定 Header
            HEADERS_TO_PROPAGATE.forEach(headerName -> {
                String value = request.getHeader(headerName);
                if (value != null && !value.isEmpty()) {
                    template.header(headerName, value);
                }
            });
            // 透传 Seata 的 XID
            // seata的at模式实现分布式事务之间的服务调用是通过feign客户端调用的，但是http调用默认是使用httpclient实现，所以seata的at模式下，httpclient不会透传XID，所以需要手动透传
            String xid = RootContext.getXID();
            if (xid != null) {
                template.header(RootContext.KEY_XID, xid); // 👈 把 XID 塞进 HTTP Header
            }
            // 如果没有 X-Request-Id，生成一个
            if (request.getHeader("X-Request-Id") == null) {
                template.header("X-Request-Id", java.util.UUID.randomUUID().toString());
            }
        };
    }
}
