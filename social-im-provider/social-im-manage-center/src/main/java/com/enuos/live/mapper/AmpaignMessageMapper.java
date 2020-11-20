package com.enuos.live.mapper;

import com.enuos.live.pojo.AmpaignMessage;

import java.util.Date;
import java.util.List;

public interface AmpaignMessageMapper {
    int deleteByPrimaryKey(Long id);

    int insert(AmpaignMessage record);

    AmpaignMessage selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(AmpaignMessage record);

    List<AmpaignMessage> selectByList(Date userTime);

}