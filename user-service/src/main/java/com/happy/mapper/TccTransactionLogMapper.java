package com.happy.mapper;

import com.happy.entity.TccTransactionLog;
import org.apache.ibatis.annotations.Param;

public interface TccTransactionLogMapper {
    int deleteByPrimaryKey(Long id);

    int insert(TccTransactionLog record);

    int insertSelective(TccTransactionLog record);

    TccTransactionLog selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(TccTransactionLog record);

    int updateByPrimaryKey(TccTransactionLog record);

    /**
     * 记录 Confirm 执行日志
     */
    void logConfirm(@Param("xid") String xid);

    /**
     * 记录 Cancel 执行日志
     */
    void logCancel(@Param("xid") String xid);

    /**
     * 判断 Confirm 是否已处理
     */
    Boolean isConfirmProcessed(@Param("xid") String xid);

    /**
     * 判断 Cancel 是否已处理
     */
    Boolean isCancelProcessed(@Param("xid") String xid);
}