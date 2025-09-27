package com.happy.listener;

import com.happy.entity.Order;
import com.happy.mapper.OrderMapper;
import com.happy.message.DeductSuccessEvent;
import com.happy.service.LocalTransactionLogService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

// 在 order-service 中
@Component
@RocketMQMessageListener(
        topic = "TOPIC_DEDUCT_SUCCESS",
        consumerGroup = "group-order-service"
)
@Slf4j
public class DeductSuccessListener implements RocketMQListener<DeductSuccessEvent> {

    @Autowired
    private LocalTransactionLogService localTransactionLogService;
    @Autowired
    private OrderMapper orderMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void onMessage(DeductSuccessEvent event) {
        String orderId = event.getOrderId();

        Order order = orderMapper.selectByOrderId(orderId);
        if (order == null || "PAID".equals(order.getStatus())) {
            log.info("订单已处理或不存在，跳过: {}", orderId);
            return;
        }

        // 非终态才更新
        if (!List.of("CANCELLED", "PAY_TIMEOUT").contains(order.getStatus())) {
            orderMapper.updateByOrderId(Order.builder().orderId(orderId).status("PAID").build());
            localTransactionLogService.updateStatusByBizId(orderId, 1);
            log.info("✅ 订单支付成功: {}", orderId);
        } else {
            log.warn("订单已终态，忽略消息: orderId={}, status={}", orderId, order.getStatus());
        }
    }
}