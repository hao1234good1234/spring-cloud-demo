package com.happy.mapper;

import com.happy.entity.Order;

public interface OrderMapper {
    int deleteByPrimaryKey(Long id);

    int insert(Order record);

    int insertSelective(Order record);

    Order selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(Order record);

    int updateByPrimaryKey(Order record);
    int updateByOrderId(Order record);
    Order selectByOrderId(String orderId);

}