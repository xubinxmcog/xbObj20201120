package com.enuos.live.service.impl;

import com.enuos.live.mapper.GuoQingMapper;
import com.enuos.live.pojo.GuoQing;
import com.enuos.live.result.Result;
import com.enuos.live.service.GuoQingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * @Description 欢度国庆[ACT0002]
 * @Author wangyingjie
 * @Date 2020/9/17
 * @Modified
 */
@Slf4j
@Service
public class GuoQingServiceImpl implements GuoQingService {

    @Autowired
    private GuoQingMapper guoQingMapper;
    /**
     * @Description: 详情
     * @Param: [guoQing]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/9/17
     */
    @Override
    public Result detail(GuoQing guoQing) {
        if (Objects.isNull(guoQing)) {
            return Result.empty();
        }

        return null;
    }
}
