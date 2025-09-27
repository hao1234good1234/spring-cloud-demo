package com.happy.controller;


import com.happy.service.BalanceService;
import com.happy.service.DeductRecordService;
import io.seata.core.context.RootContext;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@Hidden // 👈 整个 Controller 隐藏
@RestController
@RequiredArgsConstructor
@Slf4j
public class DeductRecordController {

    private final DeductRecordService deductRecordService;

    @GetMapping("/deduct-record/{orderId}")
    public boolean isDeducted(String orderId) {
        return deductRecordService.queryByOrderId(orderId) != null;
    }

}