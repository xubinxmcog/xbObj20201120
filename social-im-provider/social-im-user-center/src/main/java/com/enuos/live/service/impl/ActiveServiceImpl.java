package com.enuos.live.service.impl;

import com.enuos.live.result.Result;
import com.enuos.live.service.ActiveService;
import com.enuos.live.task.Key;
import com.enuos.live.utils.DateUtils;
import com.enuos.live.utils.RedisUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.TimeUnit;

/**
 * @Description
 * @Author wangyingjie
 * @Date 2020/6/12
 * @Modified
 */
@Slf4j
@Service
public class ActiveServiceImpl implements ActiveService {

    @Autowired
    private RedisUtils redisUtils;

    /**
     * @Description: 计算活跃度
     * @Param: [userId, active]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/6/12
     */
    @Override
    @Transactional
    public Result countActive(Long userId, Integer active) {
        if (active == null || active == 0) {
            return Result.success();
        }

        String rKey = Key.getTaskActive(userId);
        String iKey = DateUtils.getLocalDateOfPattern();

        if (redisUtils.hasHashKey(rKey, iKey)) {
            redisUtils.incrHash(rKey, iKey, active);
        } else {
            redisUtils.setHash(rKey, iKey, active, 7, TimeUnit.DAYS);
        }

        return Result.success();
    }
}
