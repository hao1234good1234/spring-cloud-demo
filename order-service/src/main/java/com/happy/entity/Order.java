package com.happy.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单主表
 * t_order
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Order implements Serializable {
    /**
     * 自增主键，用于数据库索引优化
     */
    private Long id;

    /**
     * 业务订单号，全局唯一，对外使用
     */
    private String orderId;

    /**
     * 下单用户ID，关联用户系统
     */
    private String userId;

    /**
     * 订单金额，单位：元，保留两位小数
     */
    private BigDecimal amount;

    /**
     * 订单状态：CREATED（已创建）、PAID（已支付）、CANCELLED（已取消）等
     */
    private String status;

    /**
     * 订单创建时间
     */
    private LocalDateTime createTime;
    @Serial
    private static final long serialVersionUID = 1L;
}