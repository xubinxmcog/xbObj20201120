package com.enuos.live.service;

import com.enuos.live.dto.EmoticonDTO;
import com.enuos.live.dto.GiftGiveDTO;
import com.enuos.live.result.Result;

/**
 * @ClassName GiftService
 * @Description: TODO
 * @Author xubin
 * @Date 2020/6/17
 * @Version V2.0
 **/
public interface GiftService {

    Result getList();

    Result getUserCouponList(Long userId);

    Result give(GiftGiveDTO dto);

    Result giveEmoticon(EmoticonDTO dto);

    /**
     * @MethodName: getGiveNumList
     * @Description: TODO 礼物数量字典
     * @Param: [giftId]
     * @Return: com.enuos.live.result.Result
     * @Author: xubin
     * @Date: 14:54 2020/8/27
    **/
    Result getGiveNumList(Long giftId);
}
