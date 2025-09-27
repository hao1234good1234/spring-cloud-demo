package com.happy.service.impl;

import com.happy.entity.LocalTransactionLog;
import com.happy.mapper.LocalTransactionLogMapper;
import com.happy.service.LocalTransactionLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class LocalTransactionLogServiceImpl implements LocalTransactionLogService {

    @Autowired
    LocalTransactionLogMapper localTransactionLogMapper;
    @Override
    public int updateStatusByBizId(String orderId, Integer status) {
        // 确保订单id是唯一的
        LocalTransactionLog localTransactionLog = LocalTransactionLog.builder().bizId(orderId).status(status).build();
        return localTransactionLogMapper.updateStatusByBizId(localTransactionLog);
    }
}
