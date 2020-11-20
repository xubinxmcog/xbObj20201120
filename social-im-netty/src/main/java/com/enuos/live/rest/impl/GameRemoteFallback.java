package com.enuos.live.rest.impl;

import com.enuos.live.error.ErrorCode;
import com.enuos.live.rest.GameRemote;
import com.enuos.live.result.Result;

import java.util.Map;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * TODO 熔断处理.
 *
 * @author WangCaiWen - missiw@163.com
 * @version 1.0
 * @since 2020/4/16 - 2020/7/28
 */
@Slf4j
@Component
public class GameRemoteFallback implements GameRemote {

    @Override
    public Result insertRoom(Map<String, Object> params) {
        return Result.error(ErrorCode.NETWORK_ERROR);
    }

    @Override
    public Result getGameInfo(Long gameId) {
        return Result.error(ErrorCode.NETWORK_ERROR);
    }

    @Override
    public Result getPetsInfo(Long userId) {
        return Result.error(ErrorCode.NETWORK_ERROR);
    }

    @Override
    public Result getOperation(Long userId, Integer operation) {
        log.error("异常, 接口[/pets/getOperation], userId=[{}], operation= [{}]", userId, operation);
        return Result.error(ErrorCode.NETWORK_ERROR);
    }

}
