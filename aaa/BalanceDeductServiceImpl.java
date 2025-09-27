package com.happy.service.impl;

import com.happy.mapper.SeataBalanceMapper;
import com.happy.mapper.TccTransactionLogMapper;
import com.happy.service.BalanceDeductService;
import com.happy.utils.ActionContextUtils;
import io.seata.core.context.RootContext;
import io.seata.rm.tcc.api.BusinessActionContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * TCC 实现类：Seata 会通过 Spring 容器调用 confirmDeduct 和 cancelDeduct
 * 必须加'@Component'(@Service)注解！让 Spring 扫描到
 * 如果缺少 `@Component` + `@TwoPhaseBusinessAction` 的标准，TCC接口Seata RM无法识别这是TCC资源
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BalanceDeductServiceImpl implements BalanceDeductService {

    private final SeataBalanceMapper seataBalanceMapper;
    private final TccTransactionLogMapper transactionLogMapper;

    /**
     * Try 阶段：冻结用户余额
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean tryFreeze(String userId, BigDecimal amount) {
        // ✅ 获取当前全局事务 ID（XID）
        String xid = RootContext.getXID();
        log.info("【TCC-Try】开始冻结，XID={}, 用户={}, 金额={}", xid, userId, amount);

        // 1. 参数校验
        if (userId == null || amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            log.warn("【Service-Try】参数非法: userId={}, amount={}", userId, amount);
            return false;
        }

        // 2. 查询余额
        var balance = seataBalanceMapper.selectByPrimaryKey(userId);
        if (balance == null || balance.getAvailable().compareTo(amount) < 0) {
            log.warn("【Service-Try】余额不足或用户不存在，用户: {}, 可用金额: {}", userId, balance != null ? balance.getAvailable() : null);
            return false;
        }

        // 3. 冻结金额（available - amount, frozen + amount）
        int updated = seataBalanceMapper.freezeBalance(userId, amount);
        if (updated == 0) {
            log.warn("【Service-Try】冻结失败，可能并发冲突，用户: {}", userId);
            return false;
        }

        log.info("【Service-Try】冻结成功，用户: {}, 金额: {}", userId, amount);
        return true;
    }

    /**
     * Confirm 阶段：确认扣款
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean confirmDeduct(BusinessActionContext context) {
        String xid = context.getXid();
        String userId = ActionContextUtils.getString(context, "userId");
        BigDecimal amount = ActionContextUtils.getBigDecimal(context, "amount");

        log.info("【TCC-Confirm】开始执行，XID={}, 用户={}, 金额={}", xid, userId, amount);

        // 1. 参数缺失 → 幂等返回 true
        if (userId == null || amount == null) {
            log.warn("【Service-Confirm】参数缺失，视为已处理，XID: {}", xid);
            return true;
        }

        // 2. 金额非法
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            log.warn("【Service-Confirm】金额非法: {}, XID: {}", amount, xid);
            return true;
        }

        // 3. 幂等检查
        if (isConfirmProcessed(xid)) {
            log.info("【Service-Confirm】已处理，幂等返回，XID: {}", xid);
            return true;
        }

        // 4. 执行确认扣款
        int updated = seataBalanceMapper.confirmDeduct(userId, amount);
        if (updated == 0) {
            log.warn("【Service-Confirm】确认扣款失败，用户: {}", userId);
            return false;
        }

        // 5. 记录 Confirm 日志
        transactionLogMapper.logConfirm(xid);
        log.info("【Service-Confirm】确认扣款成功，XID: {}", xid);
        return true;
    }

    /**
     * Cancel 阶段：取消冻结
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean cancelDeduct(BusinessActionContext context) {
        String xid = context.getXid();
        String userId = ActionContextUtils.getString(context, "userId");
        BigDecimal amount = ActionContextUtils.getBigDecimal(context, "amount");

        log.info("【TCC-Cancel】开始执行，XID={}, 用户={}, 金额={}", xid, userId, amount);
        // 1. 参数缺失
        if (userId == null || amount == null) {
            log.warn("【Service-Cancel】参数缺失，视为已处理，XID: {}", xid);
            return true;
        }

        // 2. 金额非法
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            log.warn("【Service-Cancel】金额非法: {}, XID: {}", amount, xid);
            return true;
        }

        // 3. 幂等检查
        if (isCancelProcessed(xid)) {
            log.info("【Service-Cancel】已处理，幂等返回，XID: {}", xid);
            return true;
        }

        // 4. 释放冻结金额
        int updated = seataBalanceMapper.cancelFreeze(userId, amount);
        if (updated == 0) {
            log.warn("【Service-Cancel】释放冻结失败，用户: {}", userId);
            return false;
        }

        // 5. 记录 Cancel 日志
        transactionLogMapper.logCancel(xid);
        log.info("【Service-Cancel】取消冻结成功，XID: {}", xid);
        return true;
    }

    // ---------------- 幂等性检查实现 ----------------

    private Boolean isConfirmProcessed(String xid) {
        return transactionLogMapper.isConfirmProcessed(xid);
    }

    private Boolean isCancelProcessed(String xid) {
        return transactionLogMapper.isCancelProcessed(xid);
    }
}