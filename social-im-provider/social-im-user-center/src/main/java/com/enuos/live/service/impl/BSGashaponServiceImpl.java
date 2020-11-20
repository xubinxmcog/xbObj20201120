package com.enuos.live.service.impl;

import com.enuos.live.mapper.BSGashaponMapper;
import com.enuos.live.pojo.BSGashapon;
import com.enuos.live.result.Result;
import com.enuos.live.service.BSGashaponService;
import com.enuos.live.utils.TimeUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

/**
 * @Description
 * @Author wangyingjie
 * @Date 2020/6/28
 * @Modified
 */
@Slf4j
@Service
public class BSGashaponServiceImpl implements BSGashaponService {

    @Autowired
    private BSGashaponMapper bsGashaponMapper;

    /**
     * @Description: 保存
     * @Param: [bsGashapon]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/6/28
     */
    @Override
    @Transactional
    public Result save(BSGashapon bsGashapon) {

        // 校验code是否可用
        if (bsGashaponMapper.isExists(bsGashapon.getCode()) > 0) {
            return Result.error(201, "code already exists");
        }

        // 1保存任务
        bsGashaponMapper.save(bsGashapon);

        String taskCode = bsGashapon.getCode();
        String startTime = bsGashapon.getStartTime();
        String endTime = bsGashapon.getEndTime();
        int space = bsGashapon.getSpace();
        // 时分秒单位H m s
        String spaceUnit = bsGashapon.getSpaceUnit();

        int rate;
        if ("s".equals(spaceUnit)) {
            rate = 1;
        } else if ("m".equals(spaceUnit)) {
            rate = 60;
        } else {
            rate = 3600;
        }

        LocalDate localDate = LocalDate.now();
        LocalDateTime startDateTime = LocalDateTime.of(localDate, LocalTime.parse(startTime));
        LocalDateTime endDateTime = LocalDateTime.of(localDate, LocalTime.parse(endTime));

        List<String> timeList = TimeUtils.getBetween(startDateTime, endDateTime, space, spaceUnit);
        if (CollectionUtils.isEmpty(timeList)) {
            return Result.error(201, "get between time error");
        }

        List<Map<String, Object>> childList = new ArrayList<>();

        timeList.stream().forEach(dt -> {
            String[] dts = dt.split(" ");
            long start = LocalTime.parse(dts[1]).toSecondOfDay();
            long end = start + space * rate;

            childList.add(new HashMap<String, Object>() {
                {
                    put("type", 0);
                    put("task_code", taskCode);
                    put("task_start_time", start);
                    put("start_time", start);
                    put("end_time", end);
                }
            });

            // 开奖
            childList.add(new HashMap<String, Object>() {
                {
                    put("type", 1);
                    put("task_code", taskCode);
                    put("task_start_time", start);
                    put("start_time", end);
                    // 默认15秒开奖时间
                    put("end_time", end + 15);
                }
            });
        });

        // 2初始化子任务
        bsGashaponMapper.initGashapon(childList);
        // 3初始化奖品
        bsGashaponMapper.initReward(bsGashapon);
        // 4初始化job
        bsGashaponMapper.initJob(bsGashapon);

        return Result.success();
    }
}