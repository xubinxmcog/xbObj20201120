package com.enuos.live.service.impl;

import com.enuos.live.mapper.AmpaignMessageMapper;
import com.enuos.live.mapper.HomeBusinessEntryMapper;
import com.enuos.live.pojo.HomeBusinessEntry;
import com.enuos.live.result.Result;
import com.enuos.live.service.AmpaignMessageService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * @ClassName AmpaignMessageServiceImpl
 * @Description: TODO
 * @Author xubin
 * @Date 2020/4/21
 * @Version V1.0
 **/
@Service
@Slf4j
public class AmpaignMessageServiceImpl implements AmpaignMessageService {

    @Autowired
    private AmpaignMessageMapper ampaignMessageMapper;

    @Autowired
    private HomeBusinessEntryMapper homeBusinessEntryMapper;

    @Override
    public Result selectByList(Integer pageNum, Integer pageSize) {

        PageHelper.startPage(pageNum, pageSize);

        return Result.success(new PageInfo(ampaignMessageMapper.selectByList(new Date())));
    }

    @Override
    public Result getBusinessEntry() {
        return Result.success(homeBusinessEntryMapper.getHomeBusinessEntry());
    }
}
