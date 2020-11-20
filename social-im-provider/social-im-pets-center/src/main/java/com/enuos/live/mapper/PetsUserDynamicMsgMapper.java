package com.enuos.live.mapper;

import com.enuos.live.pojo.PetsUserDynamicMsg;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface PetsUserDynamicMsgMapper {
    int deleteByPrimaryKey(Long id);

    int insert(PetsUserDynamicMsg record);

    PetsUserDynamicMsg selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(PetsUserDynamicMsg record);

    List<PetsUserDynamicMsg> get(@Param("userId") Long userId, @Param("isRead") Integer isRead);

    int upIsReadStatus(@Param("list") List<Long> list);

    List<Map<String, Object>> getFriendList(@Param("userId") Long userId);

}