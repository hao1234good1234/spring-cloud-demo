package com.happy.entity;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import lombok.Data;

/**
 * 用户信息表
 * t_user
 */
@Data
public class User implements Serializable {
    /**
     * 自增主键
     */
    private Long id;

    /**
     * 用户业务ID，全局唯一，如 UUID 或业务编号
     */
    private String userId;

    /**
     * 用户名，用于展示
     */
    private String username;

    /**
     * 账户余额，单位：元，扣款时需保证不为负数
     */
    private BigDecimal balance;
    @Serial
    private static final long serialVersionUID = 1L;
}