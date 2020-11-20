package com.enuos.live.mapper;

import com.enuos.live.pojo.VersionApp;

public interface VersionAppMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(VersionApp record);

    VersionApp selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(VersionApp record);

    VersionApp selectByPlatform(String platform);
}