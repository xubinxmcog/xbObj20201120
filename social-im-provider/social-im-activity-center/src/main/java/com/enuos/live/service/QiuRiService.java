package com.enuos.live.service;

import com.enuos.live.pojo.QiuRi;
import com.enuos.live.pojo.Jackpot;
import com.enuos.live.result.Result;

import java.util.Map;

/**
 * @Description 丹枫迎秋[ACT0001]
 * @Author wangyingjie
 * @Date 2020/9/17
 * @Modified
 */
public interface QiuRiService {

    /**
     * @Description: 详情
     * @Param: [qiuRi]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/9/17
     */
    Result detail(QiuRi qiuRi);

    /**
     * @Description: 领奖
     * @Param: [qiuRi]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/8/14
     */
    Result toGet(QiuRi qiuRi);

    /**
     * @Description: 任务处理
     * @Param: [params]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/8/14
     */
    Result handler(Map<String, Object> params);

    /**
     * @Description: 丹枫迎秋选牌
     * @Param: [params]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/8/13
     */
    Result choose(Map<String, Object> params);

    /**
     * @Description: 初始化丹枫迎秋奖池
     * @Param: [jackpot]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/8/13
     */
    Result initJackpot(Jackpot jackpot);

    /**
     * @Description: 获取奖池配置
     * @Param: [jackpot]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/8/20
     */
    Result getJackpot(Jackpot jackpot);

}