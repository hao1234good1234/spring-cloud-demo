package com.happy.service;

public interface LocalTransactionLogService {
    int updateStatusByBizId(String orderId, Integer status);
}

