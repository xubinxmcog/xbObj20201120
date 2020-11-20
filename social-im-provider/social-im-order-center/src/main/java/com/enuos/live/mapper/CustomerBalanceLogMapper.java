package com.enuos.live.mapper;

import com.enuos.live.pojo.CustomerBalanceLog;

import java.util.List;

public interface CustomerBalanceLogMapper {

    int insert(CustomerBalanceLog record);

    CustomerBalanceLog selectByPrimaryKey(Long id);

    List<CustomerBalanceLog> selectByUerId(Long userId);

    int updateByPrimaryKeySelective(CustomerBalanceLog record);
}