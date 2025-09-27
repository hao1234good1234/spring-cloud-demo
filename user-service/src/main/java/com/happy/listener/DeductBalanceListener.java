package com.happy.listener;

import com.happy.message.DeductBalanceMessage;
import com.happy.message.DeductFailedEvent;
import com.happy.message.DeductSuccessEvent;
import com.happy.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * 扣款余额消息监听器
 * 职责：接收消息 → 调用 Service → 处理结果
 * 不包含任何业务逻辑或 Mapper 操作
 */
@RocketMQMessageListener(
        topic = "TOPIC_DEDUCT_BALANCE",
        consumerGroup = "user_balance_consumer"
)
@Component
@Slf4j
public class DeductBalanceListener implements RocketMQListener<DeductBalanceMessage> {

    @Autowired
    private UserService userService;
    @Autowired
    private RocketMQTemplate rocketMQTemplate; // 发送“扣款成功”事件

    @Override
    public void onMessage(DeductBalanceMessage message) {
        String orderId = message.getOrderId();
        String userId = message.getUserId();
        BigDecimal amount = message.getAmount();

        log.info("📩 收到扣款消息: orderId={}, userId={}, amount={}", orderId, userId, amount);

        try {
            // ✅ 只调用 Service，所有逻辑（幂等、扣款、记录）都在 Service 内部完成
            boolean success = userService.deductBalance(userId, amount, orderId);

            if (success) {
                // ✅ 发送“扣款成功”事件，通知订单服务可以更新状态
                DeductSuccessEvent event = new DeductSuccessEvent(orderId, userId, amount);
                rocketMQTemplate.convertAndSend("TOPIC_DEDUCT_SUCCESS", event);

                log.info("✅ 扣款成功，已发送成功事件: orderId={}", orderId);
            } else {
                // ❗ 余额不足是业务失败，不应重试！
                log.warn("❌ 余额不足，扣款失败，标记订单失败: orderId={}", message.getOrderId());
                // 发送“扣款失败”事件
                rocketMQTemplate.convertAndSend("TOPIC_DEDUCT_FAILED",
                        new DeductFailedEvent(message.getOrderId(), "INSUFFICIENT_BALANCE"));
                // ✅ 不抛异常！避免 RocketMQ 重试
                return;
            }
        } catch (IllegalArgumentException e) {
            // 用户不存在等业务异常 → 不重试
            log.error("❌ 业务异常，不重试: {}", e.getMessage());
            rocketMQTemplate.convertAndSend("TOPIC_DEDUCT_FAILED",
                    new DeductFailedEvent(message.getOrderId(), "USER_NOT_FOUND"));
            return; // ✅ 不抛异常
        } catch (Exception e) {
            // 系统异常（DB、网络等）→ 抛出异常，触发重试
            log.error("💥 消费消息失败，将触发重试: orderId={}, userId={}", orderId, userId, e);
            throw e; // 抛出异常，触发 RocketMQ 重试机制
        }
    }


}