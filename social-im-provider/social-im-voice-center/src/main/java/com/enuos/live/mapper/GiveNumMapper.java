package com.enuos.live.mapper;

import com.enuos.live.pojo.GiveNum;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface GiveNumMapper {

    GiveNum selectByPrimaryKey(Long id);

    List<GiveNum> selectGiveNumList();

    List<Map<String, Object>> selectGiftGiveNumList(@Param("giftId") Long giftId);
}