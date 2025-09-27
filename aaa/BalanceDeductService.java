package com.happy.service;

import io.seata.rm.tcc.api.BusinessActionContext;

import java.math.BigDecimal;

public interface BalanceDeductService {
    boolean tryFreeze(String userId, BigDecimal amount);

    boolean confirmDeduct(BusinessActionContext context);

    boolean cancelDeduct(BusinessActionContext context);
}
