package com.happy.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;
import lombok.Data;

/**
 * 扣款记录表（用于消费幂等）
 * deduct_record
 */
@Data
public class DeductRecord implements Serializable {
    /**
     * 自增主键
     */
    private Long id;

    /**
     * 关联的订单号，唯一索引，用于幂等判断
     */
    private String orderId;

    /**
     * 被扣款的用户ID
     */
    private String userId;

    /**
     * 扣款金额，单位：元
     */
    private BigDecimal amount;

    /**
     * 扣款记录创建时间
     */
    private LocalDateTime createTime;

    private static final long serialVersionUID = 1L;
}