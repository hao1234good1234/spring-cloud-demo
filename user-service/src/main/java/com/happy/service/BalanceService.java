package com.happy.service;

import java.math.BigDecimal;

public interface BalanceService {
    void deductBalance(String userId, BigDecimal amount);
}
