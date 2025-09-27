package com.happy.controller;


import com.happy.service.BalanceService;
import io.seata.core.context.RootContext;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@Hidden // 👈 整个 Controller 隐藏
@RestController
@RequestMapping("/balance")
@RequiredArgsConstructor
@Slf4j
public class BalanceController {

    private final BalanceService balanceService;

    @PostMapping("/deduct")
    public void deductBalance(@RequestParam String userId, @RequestParam BigDecimal amount) {
        log.info("【AT扣减余额】收到请求，XID = {}", RootContext.getXID());
        balanceService.deductBalance(userId, amount);
    }
}