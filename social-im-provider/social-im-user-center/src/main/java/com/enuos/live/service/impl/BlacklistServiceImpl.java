package com.enuos.live.service.impl;

import com.enuos.live.error.ErrorCode;
import com.enuos.live.feign.ChatFeign;
import com.enuos.live.mapper.BlacklistMapper;
import com.enuos.live.pojo.Blacklist;
import com.enuos.live.result.Result;
import com.enuos.live.service.BlacklistService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Objects;

/**
 * @Description 黑名单
 * @Author wangyingjie
 * @Date 2020/7/8
 * @Modified
 */
@Slf4j
@Service
public class BlacklistServiceImpl implements BlacklistService {

    @Autowired
    private ChatFeign chatFeign;

    @Autowired
    private BlacklistMapper blacklistMapper;

    /**
     * @Description: 黑名单/屏蔽单（不看某人动态）
     * @Param: [blacklist]
     * @Return: Result
     * @Author: wangyingjie
     * @Date: 2020/7/8
     */
    @Override
    public Result list(Blacklist blacklist) {
        if (blacklist == null || blacklist.userId == null) {
            return Result.empty();
        }

        return Result.success(blacklistMapper.getBlacklist(blacklist));
    }

    /**
     * @Description: 拉黑/屏蔽某人动态
     * @Param: [blacklist]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/7/8
     */
    @Override
    @Transactional
    public Result pullBlack(Blacklist blacklist) {
        if (Objects.equals(blacklist.userId, blacklist.getToUserId())) {
            return Result.error(ErrorCode.EXCEPTION_CODE, "不能黑自己哦");
        }

        if (blacklistMapper.isExists(blacklist) != null) {
            return Result.error(ErrorCode.BLACKLIST_IS_EXISTS);
        }

        int result = blacklistMapper.save(blacklist);

        return result > 0 ? Result.success() : Result.error();
    }

    /**
     * @Description: 解除黑名单/屏蔽
     * @Param: [blacklist]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/7/8
     */
    @Override
    @Transactional
    public Result unBlack(Blacklist blacklist) {
        int result = blacklistMapper.delete(blacklist);

        return result > 0 ? Result.success() : Result.error();
    }

    /**
     * @Description: [PRIVATE]聊天展示[拉黑取消拉黑调用]
     * @Param: [userId, targetId, show]
     * @Return: void
     * @Author: wangyingjie
     * @Date: 2020/7/8
     */
    private void updateExhibition(Long userId, Long targetId, Integer show) {
        chatFeign.updateExhibition(new HashMap<String, Object>() {
            {
                put("userId", userId);
                put("targetId", targetId);
                put("show", show);
            }
        });
    }

}
