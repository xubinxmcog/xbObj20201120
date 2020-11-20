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
    public Result vipGradeNotice(Map<String, Object> params) {
        log.error("会员升级通知失败, userId=[{}]", params.get("userId"));
        return Result.error();
    }
}
