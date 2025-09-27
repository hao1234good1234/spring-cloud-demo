package com.happy.api.client.user;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;


// 声明要调用的服务名，指定 fallback 类
@FeignClient(
        contextId = "userClient",
        name = "user-service",
        fallback = UserClientFallback.class)
public interface UserClient {
    @GetMapping("/internal/user/{id}")
    String getUserById(@PathVariable("id") Long id);
}