package com.happy.service;

import java.math.BigDecimal;


public interface OrderService {
    void createOrder(String userId, BigDecimal amount);
    int updateStatus(String orderId, String status);
}