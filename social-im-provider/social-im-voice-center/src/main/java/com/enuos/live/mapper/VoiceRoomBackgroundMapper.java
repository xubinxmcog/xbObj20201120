package com.enuos.live.mapper;

import com.enuos.live.pojo.VoiceRoomBackground;

import java.util.List;

public interface VoiceRoomBackgroundMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(VoiceRoomBackground record);

    int insertSelective(VoiceRoomBackground record);

    VoiceRoomBackground selectByPrimaryKey(Integer id);

    // 语音房背景图列表
    List<VoiceRoomBackground> getVoiceRoomBackgrounds();

    int updateByPrimaryKeySelective(VoiceRoomBackground record);

    int updateByPrimaryKey(VoiceRoomBackground record);
}