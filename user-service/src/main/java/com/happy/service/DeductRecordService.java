package com.happy.service;

import com.happy.entity.DeductRecord;

import java.math.BigDecimal;

public interface DeductRecordService {
    DeductRecord queryByOrderId(String orderId);
}
