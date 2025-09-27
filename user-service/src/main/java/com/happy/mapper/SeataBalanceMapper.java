package com.happy.mapper;

import com.happy.entity.SeataBalance;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;

public interface SeataBalanceMapper {
    int deleteByPrimaryKey(String userId);

    int insert(SeataBalance record);

    int insertSelective(SeataBalance record);

    SeataBalance selectByPrimaryKey(String userId);

    int updateByPrimaryKeySelective(SeataBalance record);

    int updateByPrimaryKey(SeataBalance record);

    /**
     * seata的at模式，直接扣款
     * 条件：可用余额 >= amount 且 version 匹配（乐观锁）
     */
    int deductBalance(@Param("userId") String userId, @Param("amount") BigDecimal amount);
    /**
     * Try 阶段：冻结金额
     * 条件：可用余额 >= amount 且 version 匹配（乐观锁）
     */
    int freezeBalance(@Param("userId") String userId, @Param("amount") BigDecimal amount);

    /**
     * Confirm 阶段：确认扣款（从冻结金额中扣除）
     * 条件：冻结金额 >= amount
     */
    int confirmDeduct(@Param("userId") String userId, @Param("amount") BigDecimal amount);

    /**
     * Cancel 阶段：取消冻结（释放冻结金额回可用）
     * 条件：冻结金额 >= amount
     */
    int cancelFreeze(@Param("userId") String userId, @Param("amount") BigDecimal amount);
}