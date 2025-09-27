package com.happy.mapper;

import com.happy.entity.User;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.math.BigDecimal;

/** 
 *  用户信息表  包含余额
 */
public interface UserMapper {
    int deleteByPrimaryKey(Long id);

    int insert(User record);

    int insertSelective(User record);

    User selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(User record);

    int updateByPrimaryKey(User record);

    /**
     * 根据 user_id 查询用户（包含余额）
     */
    User selectByUserId(@Param("userId") String userId);

    /**
     * 扣减余额（带余额不足校验 + 乐观锁）
     *
     * @return 影响行数（1=成功，0=失败）
     */
    int deductBalance(
            @Param("userId") String userId,
            @Param("amount") BigDecimal amount
    );

    // freezeBalance: 冻结金额（可用余额减，冻结金额加）
    @Update("UPDATE users SET balance = balance - #{amount}, frozen_balance = frozen_balance + #{amount} " +
            "WHERE user_id = #{userId} AND balance >= #{amount}")
    int freezeBalance(@Param("userId") String userId, @Param("amount") BigDecimal amount);

    // deductFrozenBalance: 扣除已冻结金额
    @Update("UPDATE users SET frozen_balance = frozen_balance - #{amount} WHERE user_id = #{userId}")
    int deductFrozenBalance(@Param("userId") String userId, @Param("amount") BigDecimal amount);

    // unfreezeBalance: 解冻金额
    @Update("UPDATE users SET balance = balance + #{amount}, frozen_balance = frozen_balance - #{amount} " +
            "WHERE user_id = #{userId}")
    int unfreezeBalance(@Param("userId") String userId, @Param("amount") BigDecimal amount);


}