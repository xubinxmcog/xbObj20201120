package com.enuos.live.service;

import com.enuos.live.dto.UserMusicDTO;
import com.enuos.live.pojo.UserMusic;
import com.enuos.live.pojo.UserMusicList;
import com.enuos.live.result.Result;

public interface UserMusicService {

    Result getMusics(UserMusicDTO dto);

    Result getUserMusics(UserMusicDTO dto);

    Result addUserMusics(UserMusicList dto);

    Result upMusic(UserMusic dto);

    Result modifyMusic(UserMusic dto);
}
