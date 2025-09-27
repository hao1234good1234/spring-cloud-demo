package com.happy.service;

import java.math.BigDecimal;

public interface SeataOrderService {
    void createOrderSeata(String userId, BigDecimal amount);
}