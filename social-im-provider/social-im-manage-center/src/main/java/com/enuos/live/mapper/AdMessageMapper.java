package com.enuos.live.mapper;

import com.enuos.live.dto.AdMessageDTO;
import com.enuos.live.pojo.AdMessage;

import java.util.List;

public interface AdMessageMapper {
    int deleteByPrimaryKey(Long id);

    int insert(AdMessage record);

    int update(AdMessageDTO record);

    List<AdMessage> queryList(AdMessageDTO adMessage);

    AdMessage queryDetail(Long id);

    AdMessage userQueryDetail(Long id);

}