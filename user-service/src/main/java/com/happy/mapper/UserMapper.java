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




}