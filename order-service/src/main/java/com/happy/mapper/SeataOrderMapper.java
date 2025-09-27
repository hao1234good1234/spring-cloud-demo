package com.happy.mapper;

import com.happy.entity.SeataOrder;

public interface SeataOrderMapper {
    int deleteByPrimaryKey(Long id);

    int insert(SeataOrder record);

    int insertSelective(SeataOrder record);

    SeataOrder selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(SeataOrder record);

    int updateByPrimaryKey(SeataOrder record);
}