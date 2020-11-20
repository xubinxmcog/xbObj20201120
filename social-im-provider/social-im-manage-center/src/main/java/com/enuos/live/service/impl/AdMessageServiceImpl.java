package com.enuos.live.service.impl;

import com.enuos.live.common.constants.ReleaseTypeEnum;
import com.enuos.live.dto.AdMessageDTO;
import com.enuos.live.mapper.AdMessageMapper;
import com.enuos.live.pojo.AdMessage;
import com.enuos.live.result.Result;
import com.enuos.live.service.AdMessageService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import java.util.Date;

/**
 * @ClassName AdMessageServiceImpl
 * @Description: TODO 广告service
 * @Author xubin
 * @Date 2020/4/16
 * @Version V1.0
 **/
@Slf4j
@Component
public class AdMessageServiceImpl implements AdMessageService {

    @Autowired
    private AdMessageMapper adMessageMapper;

    @Override
    public Result insert(AdMessage adMessage) {
        if (ObjectUtils.isEmpty(adMessage.getReleaseTime())) {
            adMessage.setReleaseTime(new Date());
        }
        adMessageMapper.insert(adMessage);
        return Result.success(adMessage.getId());
    }

    @Override
    public Result update(AdMessageDTO adMessage) {
        if (ObjectUtils.isEmpty(adMessage.getReleaseTime())) {
            adMessage.setReleaseTime(new Date());
        }
        adMessageMapper.update(adMessage);
        return Result.success(adMessage.getId());
    }

    @Override
    public Result queryList(AdMessageDTO adMessage) {
        PageHelper.startPage(adMessage.getPageNum(), adMessage.getPageSize());
        return Result.success(new PageInfo(adMessageMapper.queryList(adMessage)));
    }

    @Override
    public Result queryDetail(Long id) {
        return Result.success(adMessageMapper.queryDetail(id));
    }

    @Override
    public Result userQueryDetail(Long id) {
        return Result.success(adMessageMapper.userQueryDetail(id));
    }

    @Override
    public ReleaseTypeEnum getCode() {
        return ReleaseTypeEnum.RELEASE_AD;
    }
}
