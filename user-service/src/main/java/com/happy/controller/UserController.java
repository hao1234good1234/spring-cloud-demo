package com.happy.controller;

import com.happy.annotation.SecureApi;
import com.happy.exception.BusinessException;
import com.happy.result.GlobalResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@RestController
@RefreshScope  // ✅ 支持刷新
@RequestMapping("/api/user")
@Tag(name = "用户服务", description = "用户服务核心接口")
public class UserController {
    @Value("${custom.welcome:默认欢迎语}")
    private String welcomeMsg;
    @Autowired
    private Environment environment;


    private final Random random = new Random();

    @GetMapping("/test")
    @Operation(hidden = true) // 👈 隐藏这个接口
    public GlobalResult<String> hello() {
        return GlobalResult.success(welcomeMsg);
    }

    @GetMapping("/{id}")
    @SecureApi(summary = "获取用户信息") // ✅ 自定义注解
    public GlobalResult<String> getUserById(@PathVariable("id") Long id,
                                            @Parameter(hidden = true)
                                            @RequestHeader("X-Auth-User") String username,
                                            @Parameter(hidden = true)
                                            @RequestHeader("X-Auth-Roles") String roles) {
        System.out.println("✅ 当前用户：" + username + " 角色是：" + roles + " 正在获取用户信息");
        if (username == null || username.isEmpty()) {
            throw new BusinessException(404, "用户不存在");
        }

        String port = environment.getProperty("server.port");
        System.out.println("请求被 " + port + " 处理");
        return GlobalResult.success("服务端口：" + port + " | 用户信息: ID=" + id + ", 姓名=张三, 邮箱=zhangsan@example.com");
    }

    @GetMapping("/info")
    @SecureApi(summary = "获取用户信息2") // ✅ 自定义注解
    public GlobalResult<Map<String,String>> getUserInfo(@Parameter(hidden = true)
                                         @RequestHeader("X-Auth-User") String username,
                                         @Parameter(hidden = true)
                                         @RequestHeader("X-Auth-Roles") String roles) {
        return GlobalResult.success(
                Map.of(
                "username", username,
                "role", roles,
                "email", username + "@happy.com"
        ));
    }

    // 测试限流
    @GetMapping("/testLimit")
    @SecureApi(summary = "测试限流") // ✅ 自定义注解
    public GlobalResult<String> testLimit() {
        return GlobalResult.success("Hello from user-service!");
    }


}

