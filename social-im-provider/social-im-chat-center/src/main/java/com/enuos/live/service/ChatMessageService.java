package com.enuos.live.service;

import com.enuos.live.result.Result;

import java.util.Map;

/**
 * TODO 单聊消息服务.
 *
 * @author WangCaiWen - missiw@163.com
 * @version 1.0
 * @since 2020/4/15 - 2020/7/28
 */
public interface ChatMessageService {

  /**
   * TODO 新增聊天记录.
   *
   * @param userId 用户ID
   * @param targetId 目标ID
   * @param type 消息标记
   * @return long 记录
   * @author wangcaiwen|1443710411@qq.com
   * @date 2020/8/12 16:34
   * @update 2020/8/12 16:34
   */
  long newAddMessage(Long userId, Long targetId, Integer type);

  /**
   * TODO 新增聊天记录.
   *
   * @param params 聊天信息
   * @return 新增结果
   * @author WangCaiWen
   * @date 2020/7/28
   */
  Result newChatMessage(Map<String, Object> params);

  /**
   * TODO 获得聊天记录.
   *
   * @param params 分页参数等
   * @return 聊天记录
   * @author WangCaiWen
   * @date 2020/7/28
   */
  Result getChatMessage(Map<String, Object> params);

  /**
   * TODO 聆听语音.
   *
   * @param params 消息参数
   * @return 聆听结果
   * @author WangCaiWen
   * @date 2020/7/28
   */
  Result listenToVoice(Map<String, Object> params);
}
