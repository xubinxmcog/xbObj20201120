package com.enuos.live.service.impl;

import com.enuos.live.mapper.ActivityMapper;
import com.enuos.live.result.Result;
import com.enuos.live.service.ActivityService;
import com.enuos.live.utils.DateUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * @Description 活动中心
 * @Author wangyingjie
 * @Date 2020/8/12
 * @Modified
 */
@Slf4j
@Service
public class ActivityServiceImpl implements ActivityService {

    @Autowired
    private ActivityMapper activityMapper;

    /**
     * @Description: 活动列表
     * @Param: []
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/8/12
     */
    @Override
    public Result list() {
        List<Map<String, Object>> list = activityMapper.getList();
        if (CollectionUtils.isNotEmpty(list)) {
            LocalDate current = DateUtils.getCurrentDate();
            list.removeIf(m -> current.isBefore(LocalDate.parse(MapUtils.getString(m, "startTime"))) || current.isAfter(LocalDate.parse(MapUtils.getString(m, "endTime"))));
        }

        return Result.success(list);
    }

}
