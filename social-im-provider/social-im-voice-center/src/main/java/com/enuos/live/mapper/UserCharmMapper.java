package com.enuos.live.mapper;

import com.enuos.live.pojo.UserCharm;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface UserCharmMapper {
    int deleteByPrimaryKey(Long charmId);

    int insert(UserCharm record);

    UserCharm selectByPrimaryKey(Long charmId);

    Integer selectIsExistence(@Param("userId") Long userId, @Param("giveUserId") Long giveUserId, @Param("giftId") Long giftId);

    int updateByPrimaryKeySelective(UserCharm record);

    int updateGiftNum(UserCharm record);

    int upUserCountCharm(@Param("userId") Long userId, @Param("charm") Long charm);

    List<Map<String, Object>> getDedicateRanking(@Param("roomId") Long roomId, @Param("limit") Integer limit);

}