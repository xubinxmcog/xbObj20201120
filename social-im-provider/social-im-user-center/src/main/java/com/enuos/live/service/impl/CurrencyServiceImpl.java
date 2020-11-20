package com.enuos.live.service.impl;

import com.enuos.live.error.ErrorCode;
import com.enuos.live.feign.ProducerFeign;
import com.enuos.live.manager.CurrencyEnum;
import com.enuos.live.mapper.CurrencyMapper;
import com.enuos.live.pojo.Currency;
import com.enuos.live.result.Result;
import com.enuos.live.service.CurrencyService;
import com.enuos.live.utils.BigDecimalUtil;
import com.enuos.live.utils.DateUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @Description 货币相关业务接口
 * @Author wangyingjie
 * @Date 2020/5/19
 * @Modified
 */
@Slf4j
@Service
public class CurrencyServiceImpl implements CurrencyService {

    @Autowired
    private ProducerFeign producerFeign;

    @Autowired
    private CurrencyMapper currencyMapper;

    /**
     * @Description: 计算金币
     * @Param: [userId, gold]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/6/9
     */
    @Override
    @Transactional
    public Result countGold(Long userId, Long gold) {
        if (gold == null || gold == 0) {
            return Result.success();
        }

        Currency current = currencyMapper.getCurrency(userId);
        if (Objects.isNull(current)) {
            return Result.error(ErrorCode.DATA_ERROR);
        }

        Long oGold = current.getGold();
        if (gold < 0 && Math.abs(gold) > oGold) {
            return Result.error(ErrorCode.NOT_ENOUGH_GOLD);
        }

        int result = currencyMapper.updateGold(current.userId, BigDecimalUtil.nAdd(oGold, gold));

        // 记录账单日志
        // logBill(userId, CurrencyEnum.GOLD, oGold, gold, result);

        return result > 0 ? Result.success() : Result.error();
    }

    /**
     * @Description: 修改金币或钻石
     * @Param: [currency]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/9/11
     */
    @Override
    public Result upUserCurrency(Currency currency) {
        log.info("更新用户账户入参=[{}]", currency.toString());
        int i = currencyMapper.upUserCurrency(currency);
        if (i == 1) {
            return Result.success();
        }
        log.warn("更新用户账户失败, userId=[{}]", currency.getUserId());
        return Result.error();
    }

    /**
     * @Description: 计算钻石
     * @Param: [userId, diamond]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/6/12
     */
    @Override
    @Transactional
    public Result countDiamond(Long userId, Long diamond) {
        if (diamond == null || diamond == 0) {
            return Result.success();
        }

        Currency current = currencyMapper.getCurrency(userId);
        if (Objects.isNull(current)) {
            return Result.error(ErrorCode.DATA_ERROR);
        }

        Long cDiamond = current.getDiamond();
        if (diamond < 0 && Math.abs(diamond) > cDiamond) {
            return Result.error(ErrorCode.NOT_ENOUGH_DIAMOND);
        }

        int result = currencyMapper.updateDiamond(current.userId, BigDecimalUtil.nAdd(cDiamond, diamond));

        return result > 0 ? Result.success() : Result.error();
    }

    /**
     * @Description: 计算扭蛋
     * @Param: [userId, gashapon]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/6/22
     */
    @Override
    @Transactional
    public Result countGashapon(Long userId, Integer gashapon) {
        if (gashapon == null || gashapon == 0) {
            return Result.success();
        }

        Currency current = currencyMapper.getCurrency(userId);
        if (Objects.isNull(current)) {
            return Result.error(ErrorCode.DATA_ERROR);
        }

        Integer cGashapon = current.getGashapon();
        if (gashapon < 0 && Math.abs(gashapon) > cGashapon) {
            return Result.error(ErrorCode.NOT_ENOUGH_GASHAPON);
        }

        int result = currencyMapper.updateGashapon(userId, BigDecimalUtil.nAdd(cGashapon, gashapon));

        return result > 0 ? Result.success() : Result.error();
    }

    /** 
     * @Description: 记录账单日志
     * @Param: [userId, type, oldCurrency, currency, result] 
     * @Return: void 
     * @Author: wangyingjie
     * @Date: 2020/9/4 
     */ 
    private void logBill(Long userId, CurrencyEnum currencyEnum, Long oldCurrency, Long currency, Integer result) {
        Map<String, Object> message = new HashMap<>();

        message.put("userId", userId);
        message.put("productCode", currencyEnum.CODE);
        message.put("oldCurrency", oldCurrency);
        message.put("newCurrency", result > 0 ? BigDecimalUtil.nAdd(oldCurrency, currency) : oldCurrency);
        message.put("price", currency);
        message.put("priceType", currencyEnum.TYPE);
        message.put("status", result);
        message.put("logTime", DateUtils.getCurrentDateTime());

        producerFeign.sendLog(message);
    }
}
