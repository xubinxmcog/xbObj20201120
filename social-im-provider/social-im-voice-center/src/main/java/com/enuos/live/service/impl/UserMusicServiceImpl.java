package com.enuos.live.service.impl;

import com.enuos.live.dto.UserMusicDTO;
import com.enuos.live.error.ErrorCode;
import com.enuos.live.mapper.UserMusicListMapper;
import com.enuos.live.mapper.UserMusicMapper;
import com.enuos.live.pojo.UserMusic;
import com.enuos.live.pojo.UserMusicList;
import com.enuos.live.result.Result;
import com.enuos.live.service.UserMusicService;
import com.enuos.live.utils.page.PageInfo;
import com.github.pagehelper.PageHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

/**
 * @ClassName UserMusicServiceImpl
 * @Description: TODO music service
 * @Author xubin
 * @Date 2020/6/23
 * @Version V2.0
 **/
@Slf4j
@Service
public class UserMusicServiceImpl implements UserMusicService {

    @Autowired
    private UserMusicMapper userMusicMapper;

    @Autowired
    private UserMusicListMapper userMusicListMapper;


    /**
     * @MethodName: getMusics
     * @Description: TODO 音乐列表
     * @Param: [dto]
     * @Return: com.enuos.live.result.Result
     * @Author: xubin
     * @Date: 13:28 2020/6/23
     **/
    @Override
    public Result getMusics(UserMusicDTO dto) {
        if (Objects.isNull(dto.getAuditStatus())) {
            dto.setAuditStatus(1);
        }
        PageHelper.startPage(dto.getPageNum(), dto.getPageSize());
        List<UserMusic> musics = userMusicMapper.getMusics(dto);

        return Result.success(new PageInfo<>(musics));
    }

    @Override
    public Result getUserMusics(UserMusicDTO dto) {
        PageHelper.startPage(dto.getPageNum(), dto.getPageSize());
        List<UserMusic> userMusics = userMusicListMapper.getUserMusics(dto);
        return Result.success(new PageInfo<>(userMusics));
    }

    @Override
    public Result addUserMusics(UserMusicList record) {
        if (null == record.getId() || 0 == record.getId()) { // id为空添加
            int result = userMusicListMapper.selectDoesItExist(record.getUserId(), record.getMusicId());
            if (0 < result) {
                return Result.success("已添加过该歌曲");
            }
            userMusicListMapper.insert(record);
        } else {
            // id不为空删除
            userMusicListMapper.deleteByPrimaryKey(record.getId());
        }

        return Result.success();
    }

    @Override
    public Result upMusic(UserMusic dto) {

        if (!Objects.isNull(dto.getMusicId())) {
            int i = userMusicMapper.updateIsStatus(dto.getMusicId());
            if (i < 1) {
                return Result.error(ErrorCode.EXCEPTION_CODE, "删除失败");
            }
            return Result.success(0, "删除成功");
        }
        if (Objects.isNull(dto.getMusicName())) {
            return Result.error(ErrorCode.EXCEPTION_CODE, "音乐名不能为空");
        }
        if (Objects.isNull(dto.getMusicUrl())) {
            return Result.error(ErrorCode.EXCEPTION_CODE, "音乐链接不能为空");
        }
        Integer exist = userMusicMapper.isExist(dto.getMusicName());
        if (!Objects.isNull(exist) && exist > 0) {
            return Result.error(ErrorCode.EXCEPTION_CODE, "音乐已存在");
        }
        int insert = userMusicMapper.insert(dto);
        if (insert > 0) {
            return Result.success();
        } else {
            return Result.error(ErrorCode.EXCEPTION_CODE, "添加失败");
        }
    }

    /**
     * @MethodName: modifyMusic
     * @Description: TODO 音乐库修改音乐
     * @Param: [dto]
     * @Return: com.enuos.live.result.Result
     * @Author: xubin
     * @Date: 12:57 2020/7/24
    **/
    @Override
    public Result modifyMusic(UserMusic dto) {
        if (Objects.isNull(dto.getMusicId())){
            return Result.error(ErrorCode.EXCEPTION_CODE, "ID不能为空");
        }
        userMusicMapper.updateByPrimaryKeySelective(dto);
        return Result.success();
    }
}
