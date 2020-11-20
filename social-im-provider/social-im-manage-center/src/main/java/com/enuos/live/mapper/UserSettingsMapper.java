package com.enuos.live.mapper;

import com.enuos.live.pojo.UserSettings;

public interface UserSettingsMapper {
    int deleteByPrimaryKey(Long id);

    int insert(UserSettings record);

    UserSettings selectByUserId(Long userId);

    int updateByUserIdSelective(UserSettings record);
}