package com.enuos.live.mapper;

import com.enuos.live.pojo.PayType;

import java.util.List;

public interface PayTypeMapper {
    int deleteByPrimaryKey(Long id);

    int insert(PayType record);

    PayType selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(PayType record);

    List<PayType> selectByList();

}