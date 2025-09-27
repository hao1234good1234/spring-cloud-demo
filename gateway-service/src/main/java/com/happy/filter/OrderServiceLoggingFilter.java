package com.happy.filter;

import com.happy.constant.CommonConstant;
import org.apache.skywalking.apm.toolkit.trace.TraceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
// 过滤请求，将请求的 Trace ID 打印日志
@Order(CommonConstant.SkyWalkingTraceHeaderFilter_ORDER)
@Component
public class OrderServiceLoggingFilter implements GlobalFilter {
    private static final Logger log = LoggerFactory.getLogger(OrderServiceLoggingFilter.class);

//    private final WebClient webClient = WebClient.builder().build();

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getPath().value();

        if (path.startsWith("/api/order/")) {
            String traceId = TraceContext.traceId();
            log.info(">>> [GATEWAY] 即将调用 Order-Service，Path: {}, Trace ID: {}", path, traceId);

            // 你可以在这里用 WebClient 显式调用（但会破坏 Gateway 原生路由）
            // 不推荐，除非你要做特殊处理
        }

        return chain.filter(exchange);
    }
}