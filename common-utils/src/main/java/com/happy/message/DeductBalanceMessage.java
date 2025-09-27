package com.happy.message;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
/**
 * 订单服务向用户服务发送的扣款消息
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DeductBalanceMessage {
    private String orderId;
    private String userId;
    private BigDecimal amount;
}