package com.happy.service.impl;

import com.happy.api.client.balance.BalanceDeductClient;
import com.happy.entity.SeataOrder;
import com.happy.mapper.SeataOrderMapper;
import com.happy.service.SeataOrderService;
import io.seata.core.context.RootContext;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Seata 分布式事务订单服务实现类
 * 使用 TCC 模式协调订单与余额服务
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SeataOrderServiceImpl implements SeataOrderService {

    private final SeataOrderMapper seataOrderMapper;

    private final BalanceDeductClient balanceDeductClient;

    /**
     * 创建订单（Seata TCC 分布式事务）
     *
     * @param userId 用户ID
     * @param amount 订单金额
     */
    @Override
    @RefreshScope // Nacos 修改 vgroup-mapping 后自动生效,支持动态刷新
    //全局事务入口，Seata 开启全局事务 ，开发环境测试设置数据库的超时时间为1小时,timeoutMills = 3600000
    @GlobalTransactional(name = "create-order-tcc", rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    public void createOrderSeata(String userId, BigDecimal amount) {
        log.info("【Seata创建订单】当前 XID: {}", RootContext.getXID());  //获取当前线程绑定的全局事务ID（XID）
        log.info("【Seata创建订单】开始处理: 用户={}, 金额={}", userId, amount);

        // 1️⃣ 生成唯一订单号
        String orderId = generateOrderId();

        // 2️⃣ Try 阶段：创建订单（状态为 TRYING）
        SeataOrder order = new SeataOrder();
        order.setOrderId(orderId);
        order.setUserId(userId);
        order.setAmount(amount);
        order.setStatus("TRYING"); // TCC 状态：尝试中
        order.setCreateTime(LocalDateTime.now());
        order.setUpdateTime(LocalDateTime.now());
        int result = seataOrderMapper.insert(order);
        if (result <= 0) {
            log.error("【Seata创建订单】订单插入失败: {}", orderId);
            throw new RuntimeException("订单创建失败");
        }
        log.info("【Seata创建订单】订单创建成功，订单号: {}, 状态: TRYING", orderId);

        // 3️⃣ Try 阶段：调用 user-service 冻结余额（TCC Try）
        Boolean frozenSuccess = balanceDeductClient.tryFreeze(userId, amount);
        if (!frozenSuccess) {
            log.error("【Seata创建订单】余额冻结失败，用户: {}, 金额: {}", userId, amount);
            throw new RuntimeException("余额不足或冻结失败");
        }
        log.info("【Seata创建订单】余额冻结成功，用户: {}, 冻结金额: {}", userId, amount);

        // 4️⃣ 如果到这里没有异常，Seata 将自动提交事务
        // → Seata 会异步调用：
        //    - balanceTccAction.confirmDeduct()  确认扣款
        //    - （订单状态自然生效，无需 confirm）

        log.info("【Seata创建订单】订单创建流程完成，等待全局提交，订单号: {}", orderId);
    }

    /**
     * 生成唯一订单号（简单实现，生产环境可用雪花算法）
     */
    private String generateOrderId() {
        return "ORDER-" + System.currentTimeMillis() + "-" + UUID.randomUUID().toString().substring(0, 8);
    }
}