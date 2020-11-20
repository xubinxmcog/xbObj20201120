package com.enuos.live.service.impl;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ObjectUtil;
import com.enuos.live.mapper.TbChatEmoticonMapper;
import com.enuos.live.pojo.TbChatEmoticon;
import com.enuos.live.result.Result;
import com.enuos.live.service.ChatEmoticonService;
import com.enuos.live.utils.RedisUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * @ClassName ChatEmoticonServiceImpl
 * @Description: TODO
 * @Author xubin
 * @Date 2020/11/9
 * @Version V2.0
 **/
@Slf4j
@Service
public class ChatEmoticonServiceImpl implements ChatEmoticonService {

    @Autowired
    private TbChatEmoticonMapper chatEmoticonMapper;

    @Autowired
    private RedisUtils redisUtils;

    @Override
    public Result getEmoticon(Map<String, Object> params) {
        log.info("查询聊天表情入参=[{}]", params);

        Integer categoryId = MapUtil.getInt(params, "categoryId");
        if (null == categoryId) {
            return Result.error(201, "categoryId不能为空");
        }

        String key = "CHAT_EMOJI:" + categoryId;

        List<TbChatEmoticon> tbChatEmoticons;

        tbChatEmoticons = (List<TbChatEmoticon>) redisUtils.get(key);
        if (ObjectUtil.isEmpty(tbChatEmoticons)) {
            tbChatEmoticons = chatEmoticonMapper.selectEmoticon(categoryId);
            if (ObjectUtil.isNotEmpty(tbChatEmoticons)) {
                redisUtils.set(key, tbChatEmoticons, 60 * 60 * 24 * 3); // 缓存三天
            }
        }
        return Result.success(tbChatEmoticons);
    }
}
