package com.happy.api.client.balance;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;

/**
 * Feign 客户端：调用 user-service 扣减余额（AT 模式）
 */
@FeignClient(
        contextId = "balanceDeductClient",
        name = "user-service"
)
public interface BalanceDeductClient {
    /**
     * 扣减余额
     * ✅ 异常会通过 Feign 传播到 order-service
     */
    @PostMapping("/balance/deduct")
    void deductBalance(
            @RequestParam("userId") String userId,
            @RequestParam("amount") BigDecimal amount
    );

    /**
     * 查询扣减记录
     */
    @GetMapping("/deduct-record/{orderId}")
    Boolean isDeducted(@PathVariable String orderId);
}



