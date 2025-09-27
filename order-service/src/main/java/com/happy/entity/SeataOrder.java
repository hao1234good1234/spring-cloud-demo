package com.happy.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;
import lombok.Data;

/**
 * 订单主表，支持TCC分布式事务（TRYING/CONFIRMED/CANCELED）
 * seata_order
 */
@Data
public class SeataOrder implements Serializable {
    /**
     * 主键ID，自增
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
     * 订单金额，单位：元，精确到分
     */
    private BigDecimal amount;

    /**
     * 订单状态：TRYING（尝试中）, CONFIRMED（已确认）, CANCELED（已取消）
     */
    private String status;

    /**
     * 订单创建时间
     */
    private LocalDateTime createTime;

    /**
     * 最后更新时间
     */
    private LocalDateTime updateTime;

    private static final long serialVersionUID = 1L;
}