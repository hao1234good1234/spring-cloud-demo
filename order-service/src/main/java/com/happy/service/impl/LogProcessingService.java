package com.happy.service.impl;

import com.happy.api.client.deduct.DeductRecordClient;
import com.happy.entity.LocalTransactionLog;
import com.happy.entity.Order;
import com.happy.mapper.LocalTransactionLogMapper;
import com.happy.mapper.OrderMapper;
import com.happy.message.DeductBalanceMessage;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
@Service
public class LogProcessingService {
    private static final Logger logger = LoggerFactory.getLogger(LogProcessingService.class);

    @Autowired
    private LocalTransactionLogMapper localTransactionLogMapper;
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private RocketMQTemplate rocketMQTemplate;
    @Autowired
    private DeductRecordClient deductRecordClient;
    // 👇 关键：这个方法加事务
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    protected void processSingleLog(LocalTransactionLog log) {
        String orderId = log.getBizId();
        // 1. 调用 user-service的 deduct_record表 查询该订单是否已处理
        boolean isDeducted = deductRecordClient.isDeducted(log.getBizId()); // 新增接口

        if (isDeducted) {
            // 已扣款 → 标记成功
            LocalTransactionLog localTransactionLog = LocalTransactionLog.builder().bizId(orderId).status(1).build();
            localTransactionLogMapper.updateStatusByBizId(localTransactionLog);
            orderMapper.updateByPrimaryKey(Order.builder().orderId(log.getBizId()).status("PAID").build());
        } else {
            // 未扣款 → 查询订单当前状态
            Order order = orderMapper.selectByOrderId(log.getBizId());
            String orderStatus = "";
            if (order != null) {
                orderStatus = order.getStatus();
            }
            if ("CANCELLED".equals(orderStatus)) {
                // 已明确失败，不再处理
                logger.info("订单已失败，跳过补偿: {}", log.getBizId());
            } else {
                // 可能是系统异常导致未处理 → 重发一次（可加最大重试次数限制）
                if (log.getRetryCount() < 3) {
                    // 3. 发送普通消息（即使失败，补偿任务会重发）
                    DeductBalanceMessage message = new DeductBalanceMessage(orderId, log.getUserId(), log.getAmount());
//                    rocketMQTemplate.convertAndSend("TOPIC_DEDUCT_BALANCE", message);
                    // 重试次数加 1
                    localTransactionLogMapper.updateStatusByBizId(LocalTransactionLog.builder().bizId(orderId).retryCount(log.getRetryCount() + 1).build());
                } else {
                    // 4. 订单超时，超过重试次数，人工介入或标记失败
                    orderMapper.updateByOrderId(Order.builder().orderId(log.getBizId()).status("PAY_TIMEOUT").build());
                    localTransactionLogMapper.updateStatusByBizId(LocalTransactionLog.builder().bizId(orderId).status(2).build());
                }
            }
        }
    }
}
