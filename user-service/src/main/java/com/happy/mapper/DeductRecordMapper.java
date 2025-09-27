package com.happy.mapper;

import com.happy.entity.DeductRecord;
import org.apache.ibatis.annotations.Param;

public interface DeductRecordMapper {
    int deleteByPrimaryKey(Long id);

    int insert(DeductRecord record);

    int insertSelective(DeductRecord record);

    DeductRecord selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(DeductRecord record);

    int updateByPrimaryKey(DeductRecord record);
    /**
     * 根据 order_id 查询是否已扣款（幂等校验）
     */
    DeductRecord selectByOrderId(@Param("orderId") String orderId);

    /**
     * 插入扣款记录
     */
    int insertRecord(DeductRecord record);
}