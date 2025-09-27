package com.happy.service.impl;

import com.happy.entity.DeductRecord;
import com.happy.entity.User;
import com.happy.mapper.DeductRecordMapper;
import com.happy.mapper.UserMapper;
import com.happy.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

// UserServiceImpl.java
@Service
@Slf4j
@Transactional(rollbackFor = Exception.class) //扣款 + 记录 在同一事务
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private DeductRecordMapper deductRecordMapper;

    /**
     *
     * 可靠消息+事务一致性方案
     *
     * 扣减用户余额（幂等、防超扣、事务安全）
     *
     * @param userId   用户ID
     * @param amount   扣款金额
     * @param orderId  关联订单ID（用于幂等判断）
     * @return 是否成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class) // 显式声明，更清晰
    public boolean deductBalance(String userId, BigDecimal amount, String orderId) {
        // 1️⃣ 幂等校验：先查是否已处理过该订单
        DeductRecord existing = deductRecordMapper.selectByOrderId(orderId);
        if (existing != null) {
            log.info("【幂等】该订单已处理过，跳过重复消费: orderId={}", orderId);
            return true; // 已成功处理，返回 true
        }

        // 2️⃣ 查询用户
        User user = userMapper.selectByUserId(userId);
        if (user == null) {
            log.warn("用户不存在: userId={}", userId);
            throw new IllegalArgumentException("用户不存在");
        }

        // 3️⃣ 执行扣款（SQL 层面防超扣）
        int rows = userMapper.deductBalance(userId, amount);
        if (rows == 0) {
            log.warn("余额不足或扣款失败: userId={}, amount={}, balance={}",
                    userId, amount, user.getBalance());
            // 即使失败，也记录一条记录？看业务需求
            // 一般不记录，由上游重试或补偿
            return false;
        }

        // 4️⃣ 记录扣款成功
        DeductRecord record = new DeductRecord();
        record.setOrderId(orderId);
        record.setUserId(userId);
        record.setAmount(amount);
        record.setCreateTime(LocalDateTime.now());
        deductRecordMapper.insert(record);

        log.info("✅ 余额扣减成功: userId={}, amount={}, orderId={}", userId, amount, orderId);
        return true;
    }
}