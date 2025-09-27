package com.happy.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 本地事务日志表（可靠消息核心表，支持消息补偿与幂等处理）
 * local_transaction_log
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LocalTransactionLog implements Serializable {
    /**
     * 事务ID，通常用UUID生成，全局唯一，作为消息幂等键
     */
    private String txId;

    /**
     * 服务名称，如 order-service，用于跨服务追踪和排查
     */
    private String serviceName;

    /**
     * 业务类型，如 create_order、deduct_balance，标识具体业务场景
     */
    private String businessType;

    /**
     * 业务ID，如订单号 orderId、交易流水号，用于关联具体业务记录
     */
    private String bizId;

    /**
     * 用户ID，记录该事务关联的用户，用于消息重发时精准投递
     */
    private String userId;

    /**
     * 金额字段，记录该事务涉及的金额（如扣款、充值等），用于补偿时重建完整消息
     */
    private BigDecimal amount;

    /**
     * 事务状态：0=PREPARED（待处理/待确认）、1=CONFIRMED（已成功）、2=FAILED（处理失败）
     */
    private Integer status;

    /**
     * 记录创建时间，用于补偿任务判断超时（如超过5分钟未处理）
     */
    private LocalDateTime createTime;

    /**
     * 记录最后更新时间，便于监控处理进度和重试时间
     */
    private LocalDateTime updateTime;

    private static final long serialVersionUID = 1L;
}