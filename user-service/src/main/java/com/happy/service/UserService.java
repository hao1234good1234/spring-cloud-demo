package com.happy.service;

import java.math.BigDecimal;

public interface UserService {

    boolean deductBalance(String userId, BigDecimal amount, String orderId);

}
