package com.enuos.live.service.impl;

import com.enuos.live.feign.UserFeign;
import com.enuos.live.mapper.GashaponMapper;
import com.enuos.live.pojo.GashaponUser;
import com.enuos.live.result.Result;
import com.enuos.live.service.GashaponService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.math.RandomUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Description
 * @Author wangyingjie
 * @Date 2020/6/24
 * @Modified
 */
@Slf4j
@Service
public class GashaponServiceImpl implements GashaponService {

    @Autowired
    private UserFeign userFeign;

    @Autowired
    private GashaponMapper gashaponMapper;

    /** 
     * @Description: 幸运用户
     * @Param: [code, gashaponUserList] 
     * @Return: void 
     * @Author: wangyingjie
     * @Date: 2020/6/24 
     */ 
    @Override
    @Async
    @Transactional(rollbackFor = Exception.class)
    public void luckyUser (String code, List<GashaponUser> gashaponUserList) {
        // 获取奖励
        List<Map<String, Object>> list = gashaponMapper.getReward(code);
        if (CollectionUtils.isEmpty(list)) {
            log.error("Can not get reward param [code:{}]", code);
            return;
        }

        int index = RandomUtils.nextInt(gashaponUserList.size() * 2);
        // 0未中奖 1中奖
        gashaponUserList.forEach(u -> u.setResult(0));

        boolean isWin = false;
        if (index < gashaponUserList.size()) {
            isWin = true;
            gashaponUserList.get(index).setResult(1);
        }

        gashaponMapper.batchUpdate(gashaponUserList);

        if (isWin) {
            Long userId = gashaponUserList.get(index).getUserId();

            log.info("Code is [{}], lucky user is [{}]", code, userId);

            Result result = userFeign.rewardHandler(new HashMap<String, Object>() {
                {
                    put("userId", userId);
                    put("list", list);
                }
            });

            if (result.getCode() != 0) {
                throw new RuntimeException(result.getMsg());
            }
        }
    }

}
