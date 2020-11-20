package com.enuos.live.mapper;

import com.enuos.live.pojo.VoiceRoomUserTime;
import org.apache.ibatis.annotations.Param;

public interface VoiceRoomUserTimeMapper {

    int insert(VoiceRoomUserTime record);

    VoiceRoomUserTime selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(VoiceRoomUserTime record);

    int update(VoiceRoomUserTime record);

    int deleteVoiceRoomUserTime(VoiceRoomUserTime record);

    Double getRoomAddUpMin(@Param("userId") Long userId);
}