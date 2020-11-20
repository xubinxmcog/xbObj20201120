package com.enuos.live.mapper;

import com.enuos.live.dto.UserMusicDTO;
import com.enuos.live.pojo.UserMusic;
import com.enuos.live.pojo.UserMusicList;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface UserMusicListMapper {
    int deleteByPrimaryKey(Long id);

    int insert(UserMusicList record);

    UserMusicList selectByPrimaryKey(Long id);

    int selectDoesItExist(@Param("userId") Long userId, @Param("musicId") Long musicId);

    List<UserMusic> getUserMusics(UserMusicDTO dto);

    int updateByPrimaryKeySelective(UserMusicList record);

}