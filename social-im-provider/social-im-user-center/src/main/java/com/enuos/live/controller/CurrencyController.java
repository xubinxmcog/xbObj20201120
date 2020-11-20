package com.enuos.live.controller;

import com.enuos.live.pojo.Currency;
import com.enuos.live.result.Result;
import com.enuos.live.service.CurrencyService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * @Description 货币操作
 * @Author wangyingjie
 * @Date 2020/5/19
 * @Modified
 */
@Slf4j
@Api("虚拟货币相关接口")
@RestController
@RequestMapping("/currency")
public class CurrencyController {

    @Autowired
    private CurrencyService currencyService;

    /**
     * ==========[内部服务]==========
     */

    /**
     * @Description: 金币加减
     * @Param: [params]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/9/17
     */
    @ApiOperation(value = "金币加减", notes = "金币加减")
    @PostMapping("/countGold")
    public Result countGold(@RequestBody Map<String, Long> params) {
        return currencyService.countGold(params.get("userId"), params.get("gold"));
    }

    /**
     * @Description: 钻石加减
     * @Param: [params]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/9/17
     */
    @ApiOperation(value = "钻石加减", notes = "钻石加减")
    @PostMapping("/countDiamond")
    public Result countDiamond(@RequestBody Map<String, Long> params) {
        return currencyService.countDiamond(params.get("userId"), params.get("diamond"));
    }

    /** 
     * @Description: 金币钻石加减
     * @Param: [currency] 
     * @Return: com.enuos.live.result.Result 
     * @Author: wangyingjie
     * @Date: 2020/9/11 
     */ 
    @ApiOperation(value = "金币加减", notes = "金币加减")
    @PostMapping("/upUserCurrency")
    public Result upUserCurrency(@RequestBody Currency currency) {
        return currencyService.upUserCurrency(currency);
    }

}
