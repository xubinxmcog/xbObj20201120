package com.enuos.live.mapper;

import com.enuos.live.dto.UserMusicDTO;
import com.enuos.live.pojo.UserMusic;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface UserMusicMapper {
    int deleteByPrimaryKey(Long musicId);

    int insert(UserMusic record);

    Integer isExist(@Param("musicName") String musicName);

    UserMusic selectByPrimaryKey(Long musicId);

    List<UserMusic> getMusics(UserMusicDTO dto);

    int updateByPrimaryKeySelective(UserMusic record);

    int updateIsStatus(Long musicId);

}