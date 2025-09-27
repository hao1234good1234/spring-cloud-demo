package com.happy.api.client.user;

// com.happy.order.client.UserClientFallback.java

import org.springframework.stereotype.Component;

@Component //✅ fallback 类必须被 Spring 扫描到（加 @Component）
public class UserClientFallback implements UserClient {
    @Override
    public String getUserById(Long id) {
        System.out.println("🔥 触发降级：user-service 不可用");
        return "用户服务暂时不可用，已降级处理，ID=" + id;
    }
}