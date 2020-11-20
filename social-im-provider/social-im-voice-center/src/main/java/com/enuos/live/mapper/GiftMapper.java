package com.enuos.live.mapper;

import com.enuos.live.pojo.Gift;

import java.util.List;

public interface GiftMapper {
    int deleteByPrimaryKey(Long giftId);

    int insert(Gift record);

    Gift selectByPrimaryKey(Long giftId);

    List<Gift> getList();

    int updateByPrimaryKeySelective(Gift record);

}