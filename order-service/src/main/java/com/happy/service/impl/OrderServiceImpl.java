package com.happy.service.impl;

import com.happy.entity.LocalTransactionLog;
import com.happy.entity.Order;
import com.happy.mapper.LocalTransactionLogMapper;
import com.happy.mapper.OrderMapper;
import com.happy.message.DeductBalanceMessage;
import com.happy.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@RequiredArgsConstructor
@Service
@Transactional
public class OrderServiceImpl implements OrderService {

    private static final Logger logger = LoggerFactory.getLogger(OrderServiceImpl.class);
    // 使用lombok的@RequiredArgsConstructor注解将private final属性自动注入
    private final OrderMapper orderMapper;
    private final LocalTransactionLogMapper localTransactionLogMapper;
    private final RocketMQTemplate rocketMQTemplate;

    // 可靠消息+事务一致性方案
    @Transactional(rollbackFor = Exception.class) // 显式声明，更清晰
    public void createOrder(String userId, BigDecimal amount) {
        // 1. 创建订单
        // 1. 生成订单号
        String orderId = generateOrderId();
        Order order = new Order();
        order.setOrderId(orderId);
        order.setUserId(userId);
        order.setAmount(amount);
        order.setStatus("CREATED");
        order.setCreateTime(LocalDateTime.now());
        orderMapper.insert(order);

        // 2. 写本地事务日志（关键！和订单在同一个事务）
        LocalTransactionLog log = new LocalTransactionLog();
        log.setTxId(UUID.randomUUID().toString());
        log.setServiceName("order-service");
        log.setBusinessType("create_order");
        log.setBizId(orderId);
        log.setUserId(userId);
        log.setAmount(amount);
        log.setStatus(0); // PREPARED
        log.setCreateTime(LocalDateTime.now());
        log.setUpdateTime(LocalDateTime.now());
        localTransactionLogMapper.insert(log);


        // 3. 发送普通消息（即使失败，补偿任务会重发）
        DeductBalanceMessage message = new DeductBalanceMessage(orderId, userId, amount);
        try {
            rocketMQTemplate.convertAndSend("TOPIC_DEDUCT_BALANCE", message);
            logger.info("✅ 消息发送成功: orderId={}", orderId);
        } catch (Exception e) {
            logger.warn("⚠️ 消息发送失败，将由补偿任务重试: orderId={}", orderId);
            //RocketMQ 普通消息是非事务性的，这里不要抛异常 → 事务仍会提交（订单+日志已落库），如果发消息失败，靠补偿任务重试。
            //这正是 “本地消息表” 或 “可靠消息最终一致性” 方案的标准做法：先在本地事务中写日志 + 订单，再尝试发消息；如果发消息失败，靠补偿任务重试。
        }
    }

    @Override
    public int updateStatus(String orderId, String status) {
        Order order = new Order();
        order.setOrderId(orderId);
        order.setStatus(status);
        return orderMapper.updateByOrderId(order);
    }

    /**
     * 生成唯一订单号（简单实现，生产环境可用雪花算法）
     */
    private String generateOrderId() {
        return "ORDER-" + System.currentTimeMillis() + "-" + UUID.randomUUID().toString().substring(0, 8);
    }
}
