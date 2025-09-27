package com.happy.controller;

import com.happy.annotation.SecureApi;
import com.happy.result.GlobalResult;
import com.happy.result.SystemErrorType;
import com.happy.service.SeataOrderService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@RestController
@RefreshScope  // ✅ 关键注解：开启配置刷新
@RequestMapping("/api/order")
@Tag(name = "订单服务", description = "订单服务核心接口")
public class SeataOrderController {
    private static final Logger logger = LoggerFactory.getLogger(SeataOrderController.class);
    @Autowired
    private SeataOrderService seataOrderService;

    // seata分布式事物的发起方，创建订单
    @PostMapping("/seata/create")
    @SecureApi(summary = "创建订单（seata的at模式实现分布式事务的方案）") // ✅ 自定义注解
    public GlobalResult<?> createOrder(
            @RequestParam("userId") String userId,
            @RequestParam("amount") BigDecimal amount) {
        try {
            seataOrderService.createOrderSeata(userId, amount);

            return GlobalResult.success();
        } catch (Exception e) {
            return GlobalResult.error(SystemErrorType.NOT_FOUND.getCode(), e.getMessage());
        }
    }
}
