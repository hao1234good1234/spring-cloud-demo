package com.happy.controller;

import com.happy.service.SeataOrderService;
import io.seata.core.context.RootContext;
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
public class SeataOrderController {
    private static final Logger logger = LoggerFactory.getLogger(SeataOrderController.class);
    @Autowired
    private SeataOrderService seataOrderService;
    // seata分布式事物的发起方，创建订单
    @PostMapping("/seata/create")
    public String createOrder(
            @RequestParam("userId") String userId,
            @RequestParam("amount") BigDecimal amount) {
        logger.info("【Seata创建订单】当前 XID: {}", RootContext.getXID());
        seataOrderService.createOrderSeata(userId, amount);
        return "Order created, waiting for confirmation...";
    }
}

