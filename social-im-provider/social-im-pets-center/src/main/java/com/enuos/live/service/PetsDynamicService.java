package com.enuos.live.service;

import com.enuos.live.dto.PetsDynamicDTO;
import com.enuos.live.result.Result;

import java.util.Map;

public interface PetsDynamicService {

    /**
     * 获取动态
     *
     * @param dto
     * @return
     */
    Result get(PetsDynamicDTO dto);

    /**
     * 标记动态
     *
     * @param dto
     * @return
     */
    Result sign(PetsDynamicDTO dto);

    /**
     * 获取好友列表
     *
     * @param userId
     * @return
     */
    Result goodFriends(Long userId);

    /**
     * 给好友喂食
     *
     * @param params
     * @return
     */
    Result feed(Map<String, Object> params);

    /**
     * 给好友互动
     *
     * @param params
     * @return
     */
    Result toys(Map<String, Object> params);

    /**
     * 打招呼
     *
     * @param params
     * @return
     */
    Result hello(Map<String, Object> params);
}
