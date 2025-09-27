package com.happy.job;

import com.happy.service.impl.TransactionCompensationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class TransactionCompensationJob {

    @Autowired
    private TransactionCompensationService compensationService;

    /**
     * 每 30 秒扫描一次，重发未完成的消息
     */
    @Scheduled(fixedRate = 30000)
    public void compensate() {
        log.info("⏰ 开始执行事务补偿任务...");
        try {
            compensationService.compensatePendingTransactions();
        } catch (Exception e) {
            log.error("事务补偿任务执行失败", e);
        }
        log.info("✅ 事务补偿任务执行完成");
    }
}