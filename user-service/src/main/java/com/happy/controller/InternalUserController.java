package com.happy.controller;

import com.happy.exception.BusinessException;
import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.*;

@Hidden // 👈 整个 Controller 隐藏
@RestController
@RequestMapping("/internal/user")
public class InternalUserController {
    @Autowired
    private Environment environment;

    @GetMapping("/{id}")
    public String getUserById(@PathVariable("id") Long id,
                              @RequestHeader("X-Auth-User") String username,
                              @RequestHeader("X-Auth-Roles") String roles) {
        System.out.println("✅ 当前用户：" + username + " 角色是：" + roles + " 正在访问用户信息");
        if (username == null || username.isEmpty()) {
            throw new BusinessException(404, "用户不存在");
        }
        // 1、测试慢调用
//        try {
//            long start = System.currentTimeMillis();
//            Thread.sleep(5000);
//            long cost = System.currentTimeMillis() - start;
//            System.out.println("user-service 耗时: " + cost + "ms");
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
        // 2、测试异常比例/异常数
//        if (id % 2 == 0) { // 偶数 ID 抛异常
//            throw new RuntimeException("模拟服务异常");
//        }
        // 🎯 模拟 50% 异常率
//        if (random.nextBoolean()) {
//            throw new RuntimeException("随机异常，ID=" + id);
//        }
        String port = environment.getProperty("server.port");
        System.out.println("请求被 " + port + " 处理");
        return "服务端口：" + port + " | 用户信息: ID=" + id + ", 姓名=张三, 邮箱=zhangsan@example.com";
    }
}
