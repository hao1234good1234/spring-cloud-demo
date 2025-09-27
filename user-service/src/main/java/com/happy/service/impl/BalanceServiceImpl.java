package com.happy.service.impl;

import com.happy.mapper.SeataBalanceMapper;
import com.happy.service.BalanceService;
import io.seata.core.context.RootContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * Seata AT 模式实现类：直接扣款，由 Seata 管理回滚
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BalanceServiceImpl implements BalanceService {

    private final SeataBalanceMapper seataBalanceMapper;

    /**
     * 扣减余额（本地事务，由 Seata 代理成分支事务）
     * ✅ 成功：执行扣款
     * ✅ 失败：抛异常 → 触发 Seata 全局回滚
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void deductBalance(String userId, BigDecimal amount) {
        log.info("【AT扣减余额】当前 XID: {}", RootContext.getXID());
        log.info("【AT扣减余额】开始处理: 用户={}, 金额={}", userId, amount);

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("金额必须大于0");
        }

        // 查询余额
        var balance = seataBalanceMapper.selectByPrimaryKey(userId);
        if (balance == null) {
            log.error("【AT扣减余额】用户不存在: {}", userId);
            throw new RuntimeException("用户不存在");
        }
        if (balance.getAvailable().compareTo(amount) < 0) {
            log.error("【AT扣减余额】余额不足，用户: {}, 当前余额: {}, 请求扣款: {}",
                    userId, balance.getAvailable(), amount);
            throw new RuntimeException("余额不足");
        }

        // ✅ 执行真实扣款 SQL
        int updated = seataBalanceMapper.deductBalance(userId, amount);
        if (updated == 0) {
            log.warn("【AT扣减余额】扣减影响行数为0，可能并发问题，用户: {}", userId);
            throw new RuntimeException("扣减失败，可能并发问题");
        }

        log.info("【AT扣减余额】余额扣减成功：用户={}，金额={}", userId, amount);
    }
}