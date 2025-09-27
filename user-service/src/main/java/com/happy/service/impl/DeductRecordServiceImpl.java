package com.happy.service.impl;

import com.happy.entity.DeductRecord;
import com.happy.mapper.DeductRecordMapper;
import com.happy.mapper.SeataBalanceMapper;
import com.happy.service.BalanceService;
import com.happy.service.DeductRecordService;
import io.seata.core.context.RootContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * Seata AT 模式实现类：直接扣款，由 Seata 管理回滚
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DeductRecordServiceImpl implements DeductRecordService {

    private final DeductRecordMapper deductRecordMapper;

    @Override
    public DeductRecord queryByOrderId(String orderId) {
        return deductRecordMapper.selectByOrderId(orderId);
    }
}