package com.enuos.live.controller;

import cn.hutool.core.util.StrUtil;
import com.enuos.live.annotations.Cipher;
import com.enuos.live.dto.UserMusicDTO;
import com.enuos.live.pojo.UserMusic;
import com.enuos.live.pojo.UserMusicList;
import com.enuos.live.result.Result;
import com.enuos.live.service.UserMusicService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

/**
 * @ClassName UserMusicController
 * @Description: TODO 音乐
 * @Author xubin
 * @Date 2020/6/23
 * @Version V2.0
 **/
@Api("音乐")
@Slf4j
@RestController
@RequestMapping("/music")
public class UserMusicController {

    @Autowired
    private UserMusicService userMusicService;


    @ApiOperation("音乐库")
    @Cipher
    @PostMapping("/getMusics")
    public Result getMusics(@RequestBody UserMusicDTO dto) {
        log.info("音乐列表入参=[{}]", dto.toString());

        return userMusicService.getMusics(dto);

    }

    @ApiOperation("用户音乐列表")
    @Cipher
    @PostMapping("/getUserMusics")
    public Result getUserMusics(@RequestBody UserMusicDTO dto) {
        log.info("用户音乐列表入参=[{}]", dto.toString());
        return userMusicService.getUserMusics(dto);
    }

    @ApiOperation("用户添加、删除音乐")
    @Cipher
    @PostMapping("/addUserMusics")
    public Result addUserMusics(@RequestBody UserMusicList dto) {
        log.info("用户添加音乐入参=[{}]", dto.toString());
        return userMusicService.addUserMusics(dto);
    }

    @ApiOperation("音乐库添加、删除音乐")
    @Cipher
    @PostMapping("/upMusic")
    public Result upMusic(@RequestBody UserMusic dto, HttpServletRequest httpRequest) {
        log.info("音乐库添加、删除音乐入参=[{}]", dto.toString());
        String userId = httpRequest.getHeader("userId");
        if (StrUtil.isNotEmpty(userId)){
            dto.setUserId(Long.valueOf(userId));
        }
        return userMusicService.upMusic(dto);
    }

    @ApiOperation("音乐库修改音乐")
    @Cipher
    @PostMapping("/modifyMusic")
    public Result modifyMusic(@RequestBody UserMusic dto, HttpServletRequest httpRequest) {
        log.info("音乐库修改音乐=[{}]", dto.toString());
        String userId = httpRequest.getHeader("userId");
        if (StrUtil.isNotEmpty(userId)){
            dto.setUserId(Long.valueOf(userId));
        }
        return userMusicService.modifyMusic(dto);
    }

}
