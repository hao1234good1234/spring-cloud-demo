package com.happy.message;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 *  订单服务，订单完成事件
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DeductSuccessEvent {
    private String orderId;
    private String userId;
    private BigDecimal amount;
//    private Long timestamp; // 可选：事件发生时间
}