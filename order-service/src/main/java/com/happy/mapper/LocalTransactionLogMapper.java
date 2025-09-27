package com.happy.mapper;

import com.happy.entity.LocalTransactionLog;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface LocalTransactionLogMapper {
    int deleteByPrimaryKey(String txId);

    int insert(LocalTransactionLog record);

    int insertSelective(LocalTransactionLog record);

    LocalTransactionLog selectByPrimaryKey(String txId);

    int updateByPrimaryKeySelective(LocalTransactionLog record);

    int updateByPrimaryKey(LocalTransactionLog record);
    List<LocalTransactionLog> findPendingBefore(@Param("minutes") int minutes);

    int updateStatusByBizId(LocalTransactionLog record);
}