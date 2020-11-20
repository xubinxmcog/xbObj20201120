package com.enuos.live.feign.impl;

import com.enuos.live.feign.NettyFeign;
import com.enuos.live.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @ClassName NettyFeignFallback
 * @Description: TODO
 * @Author xubin
 * @Date 2020/8/25
 * @Version V2.0
 **/
@Slf4j
@Component
public class NettyFeignFallback implements NettyFeign {
    @Override
    public Result giveGiftNotice(Map<String, Object> params) {
        log.error("赠送礼物通知失败, userId=[{}]", params.get("userId"));
        return Result.error();
    }

    @Override
    public Result exceptionEndBroadcast(Long roomId, Long userId) {
        log.error("调用异常下播异常,roomId=[{}],userId=[{}]", roomId, userId);
        return Result.error();
    }
}
