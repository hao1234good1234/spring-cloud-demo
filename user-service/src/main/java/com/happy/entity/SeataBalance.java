package com.happy.entity;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;
import lombok.Data;

/**
 * 用户资金账户表，支持TCC模式下的余额冻结与解冻
 * seata_balance
 */
@Data
public class SeataBalance implements Serializable {
    /**
     * 用户ID，主键，全局唯一
     */
    private String userId;

    /**
     * 用户姓名，可用于展示（可选字段）
     */
    private String username;

    /**
     * 可用余额，单位：元，可直接消费的金额
     */
    private BigDecimal available;

    /**
     * 冻结金额，单位：元，TCC Try阶段冻结的资金
     */
    private BigDecimal frozen;

    /**
     * 乐观锁版本号，防止并发更新冲突
     */
    private Integer version;

    /**
     * 记录创建时间
     */
    private LocalDateTime createTime;

    /**
     * 最后更新时间
     */
    private LocalDateTime updateTime;
    @Serial
    private static final long serialVersionUID = 1L;
}