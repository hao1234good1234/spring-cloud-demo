package com.happy.service.impl;

import com.happy.api.client.balance.BalanceDeductClient;
import com.happy.entity.SeataOrder;
import com.happy.mapper.SeataOrderMapper;
import com.happy.service.SeataOrderService;
import io.seata.core.context.RootContext;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Seata 分布式事务订单服务实现类
 * 使用 AT 模式协调订单与余额服务
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SeataOrderServiceImpl implements SeataOrderService {

    private final SeataOrderMapper seataOrderMapper;

    private final BalanceDeductClient balanceDeductClient;

    @Override
    @GlobalTransactional(name = "create-order-at", rollbackFor = Exception.class)
    //@Transactional(rollbackFor = Exception.class)，之前是@Transactional实现的回滚，现在改为@GlobalTransactional实现的分布式事务进行回滚
    public void createOrderSeata(String userId, BigDecimal amount) {
        log.info("【AT创建订单】当前 XID: {}", RootContext.getXID());
        log.info("【AT创建订单】开始处理: 用户={}, 金额={}", userId, amount);
        // 1. 生成订单号
        String orderId = generateOrderId();

        // 2. 创建订单（状态为 CREATED）
        SeataOrder order = new SeataOrder();
        order.setOrderId(orderId);
        order.setUserId(userId);
        order.setAmount(amount);
        order.setStatus("CREATED"); // 状态改为正常
        order.setCreateTime(LocalDateTime.now());
        order.setUpdateTime(LocalDateTime.now());

        seataOrderMapper.insert(order);
        log.info("【AT创建订单】订单已插入: {}", orderId);

        // 3. 调用 user-service 扣减余额
        // ✅ 不再判断 Boolean result
        // ✅ 如果扣款失败（余额不足/异常），user-service 会抛异常 → 触发全局回滚
        balanceDeductClient.deductBalance(userId, amount);

        log.info("【AT创建订单】流程完成，等待全局提交,订单号: {}", orderId);
    }

    /**
     * 生成唯一订单号（简单实现，生产环境可用雪花算法）
     */
    private String generateOrderId() {
        return "ORDER-" + System.currentTimeMillis() + "-" + UUID.randomUUID().toString().substring(0, 8);
    }
}