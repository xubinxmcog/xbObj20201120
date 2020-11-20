package com.enuos.live.mapper;

import com.enuos.live.pojo.User;

public interface UserMapper {

    User selectByPrimaryKey(Integer id);

    String selectByUserName(Long userId);
}