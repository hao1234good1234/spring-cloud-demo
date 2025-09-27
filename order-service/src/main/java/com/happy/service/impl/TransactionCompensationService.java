package com.happy.service.impl;

import com.happy.api.client.deduct.DeductRecordClient;
import com.happy.entity.LocalTransactionLog;
import com.happy.entity.Order;
import com.happy.mapper.LocalTransactionLogMapper;
import com.happy.mapper.OrderMapper;
import com.happy.message.DeductBalanceMessage;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(rollbackFor = Exception.class)
public class TransactionCompensationService {
    private static final Logger logger = LoggerFactory.getLogger(TransactionCompensationService.class);
    @Autowired
    private LocalTransactionLogMapper localTransactionLogMapper;
    @Autowired
    private LogProcessingService logProcessingService;
    /**
     * 执行补偿：查找超时的 PREPARED 事务并重新发送消息
     */
    public void compensatePendingTransactions() {
        List<LocalTransactionLog> pendingLogs = localTransactionLogMapper.findPendingBefore(5);

        for (LocalTransactionLog log : pendingLogs) {
            // 每条日志独立处理，失败不影响其他
            try {
                logProcessingService.processSingleLog(log); // 👈 带事务的方法
            } catch (Exception e) {
                logger.error("处理单条补偿日志失败: {}", log.getBizId(), e);
                // 不 throw，继续处理下一条
            }
        }
    }



}