package com.happy.listener;

import com.happy.mapper.OrderMapper;
import com.happy.message.DeductFailedEvent;
import com.happy.service.LocalTransactionLogService;
import com.happy.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Slf4j
@RocketMQMessageListener(topic = "TOPIC_DEDUCT_FAILED", consumerGroup = "group-order-failed")
public class DeductFailedListener implements RocketMQListener<DeductFailedEvent> {

    @Autowired
    private OrderService orderService;

    @Autowired
    private LocalTransactionLogService localTransactionLogService;

    @Override
    @Transactional(rollbackFor = Exception.class) // 👈 加在这里
    public void onMessage(DeductFailedEvent event) {
        // 更新订单状态为失败
        orderService.updateStatus(event.getOrderId(), "CANCELLED");
        localTransactionLogService.updateStatusByBizId(event.getOrderId(), 2);
        log.info("🚫 订单支付失败: orderId={}, reason={}", event.getOrderId(), event.getReason());
    }
}