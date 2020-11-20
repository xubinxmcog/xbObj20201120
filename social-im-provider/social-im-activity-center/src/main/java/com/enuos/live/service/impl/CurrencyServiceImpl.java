package com.enuos.live.service.impl;

import com.enuos.live.error.ErrorCode;
import com.enuos.live.mapper.CurrencyMapper;
import com.enuos.live.mapper.UserBillMapper;
import com.enuos.live.pojo.Bill;
import com.enuos.live.result.Result;
import com.enuos.live.service.CurrencyService;
import com.enuos.live.utils.DateUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

/**
 * @Description 货币加减
 * @Author wangyingjie
 * @Date 2020/10/13
 * @Modified
 */
@Slf4j
@Service
public class CurrencyServiceImpl implements CurrencyService {

    @Autowired
    private CurrencyMapper currencyMapper;

    @Autowired
    private UserBillMapper userBillMapper;

    /**
     * @Description: 计算钻石
     * @Param: [userId, diamond, productName]
     * @Return: java.lang.Integer
     * @Author: wangyingjie
     * @Date: 2020/10/13
     */
    @Override
    @Transactional
    public Result countDiamond(Long userId, Long diamond, String productName) {
        if (Objects.isNull(userId)) {
            return Result.empty();
        }

        if (Objects.isNull(diamond)) {
            return Result.success();
        }

        Long currentDiamond = currencyMapper.getDiamond(userId);

        if (diamond < 0 && Math.abs(diamond) > currentDiamond) {
            return Result.error(ErrorCode.NOT_ENOUGH_DIAMOND);
        }

        currentDiamond += diamond;

        int result = currencyMapper.updateDiamond(userId, currentDiamond);

        if (result > 0) {
            userBillMapper.save(new Bill(userId, productName, diamond, 2, 1, DateUtils.getCurrentDateTime()));
            return Result.success();
        } else {
            return Result.error();
        }
    }

    /**
     * @Description: 计算金币
     * @Param: [userId, diamond, productName]
     * @Return: java.lang.Integer
     * @Author: wangyingjie
     * @Date: 2020/10/13
     */
    @Override
    @Transactional
    public Result countGold(Long userId, Long gold, String productName) {
        if (Objects.isNull(userId)) {
            return Result.empty();
        }

        if (Objects.isNull(gold)) {
            return Result.success();
        }

        Long currentGold = currencyMapper.getGold(userId);

        if (gold < 0 && Math.abs(gold) > currentGold) {
            return Result.error(ErrorCode.NOT_ENOUGH_GOLD);
        }

        currentGold += gold;

        int result = currencyMapper.updateGold(userId, currentGold);

        if (result > 0) {
            userBillMapper.save(new Bill(userId, productName, gold, 3, 1, DateUtils.getCurrentDateTime()));
            return Result.success();
        } else {
            return Result.error();
        }
    }
}
