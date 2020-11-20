package com.enuos.live.rest.fallback;

import com.enuos.live.pojo.Currency;
import com.enuos.live.rest.UserRemote;
import com.enuos.live.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * TODO 熔断处理.
 *
 * @author WangCaiWen - missiw@163.com
 * @version 1.0
 * @since 2020/4/14 - 2020/7/28
 */
@Slf4j
@Component
public class UserRemoteFallback implements UserRemote {

    @Override
    public Map<String, Object> getUserBase(Long userId, Long friendId) {
        return null;
    }

    @Override
    public Map<String, Object> getUserMsg(Long userId) {
        return null;
    }

    @Override
    public Result getCurrency(Long userId) {
        log.error("获取钻石金币失败, userId=[{}]", userId);
        return Result.error();
    }

    @Override
    public Result addGrowth(Long userId, Integer growth) {
        return null;
    }

    @Override
    public Result upUserCurrency(Currency currency) {
        log.error("更新用户账户失败, userId=[{}]", currency.getUserId());
        return Result.error();
    }

}
