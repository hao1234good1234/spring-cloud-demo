package com.happy.controller;

import com.happy.annotation.SecureApi;
import com.happy.api.client.user.UserClient;
import com.happy.dto.CreateOrderRequest;
import com.happy.exception.BusinessException;
import com.happy.result.GlobalResult;
import com.happy.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.*;

@RestController
@RefreshScope  // ✅ 关键注解：开启配置刷新
@RequestMapping("/api/order")
@Tag(name = "订单服务", description = "订单服务核心接口")
public class OrderController {
    private static final Logger logger = LoggerFactory.getLogger(OrderController.class);
    @Autowired
    private OrderService orderService;

    @Value("${custom.msg:默认消息}")
    private String customMsg;

    @Autowired
    private UserClient userClient;

    @GetMapping("/{id}")
    @SecureApi(summary = "获取订单信息") // ✅ 自定义注解
    public GlobalResult<String> getOrder(@PathVariable("id") Long id,
                                         @Parameter(hidden = true)
                                         @RequestHeader("X-Auth-User") String username,
                                         @Parameter(hidden = true)
                                         @RequestHeader("X-Auth-Roles") String roles) {
        System.out.println("✅ 当前用户：" + username + " 角色是：" + roles + " 正在获取订单" + id + "的详细信息");
        if (username == null || username.isEmpty()) {
            throw new BusinessException(404, "用户不存在");
        }
        long start = System.currentTimeMillis();
        // 模拟订单数据
        String order = "订单ID: " + id + ", 商品: iPhone, 价格: 9999";
        // 调用 user-service 获取用户信息
        String user = userClient.getUserById(id);
        long cost = System.currentTimeMillis() - start;
        System.out.println("✅ 调用 user-service 耗时: " + cost + "ms");
        return GlobalResult.success(order + " | " + user);
    }

    //测试自定义参数nacos自动刷新
    @GetMapping("/test")
    @Operation(hidden = true) // 👈 隐藏这个接口
    public GlobalResult<String> testConfig() {
        return GlobalResult.success("当前配置消息：" + customMsg);
    }

    @GetMapping("/test-exception")
    @SecureApi(summary = "测试全局异常") // ✅ 自定义注解
    public GlobalResult<String> testException() {
        throw new RuntimeException("测试全局异常");
    }


    @PostMapping("/create")
    @SecureApi(summary = "创建订单（可靠消息+事务一致性方案）") // ✅ 自定义注解
    public GlobalResult<?> createOrder(@Valid @RequestBody CreateOrderRequest req) {
        try {
            orderService.createOrder(req.getUserId(), req.getAmount());
            return GlobalResult.builder()
                    .code(202) // Accepted
                    .message("订单已创建，正在处理支付")
                    .build();
        } catch (Exception e) {
            return GlobalResult.error(400, "创建订单失败：" + e.getMessage());
        }
    }

}

