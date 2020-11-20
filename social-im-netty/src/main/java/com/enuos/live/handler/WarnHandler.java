package com.enuos.live.handler;

import com.enuos.live.result.Result;
import java.util.Map;

/**
 * TODO 通知处理.
 *
 * @author WangCaiWen - missiw@163.com
 * @version 1.0
 * @since 2020-04-09 11:07:22
 */

public interface WarnHandler {

  /**
   * TODO 聊天提醒.
   *
   * @param params 发送参数
   * @author WangCaiWen
   * @date 2020/7/28
   */
  void sendAloneNoticeMessage(Map<String, Object> params);

  /**
   * TODO 聊天提醒.
   *
   * @param params 发送参数
   * @author WangCaiWen
   * @date 2020/7/28
   */
  void sendGroupNoticeMessage(Map<String, Object> params);

  /**
   * TODO 互动通知.
   *
   * @param params 通知信息
   * @return 调用结果
   * @author WangCaiWen
   * @date 2020/7/28
   */
  Result interactNotice(Map<String, Object> params);

  /**
   * TODO 软件通知.
   *
   * @param params 通知信息
   * @return 调用结果
   * @author WangCaiWen
   * @date 2020/7/28
   */
  Result softwareNotice(Map<String, Object> params);

  /**
   * TODO 解散通知.
   *
   * @param params 通知信息
   * @return 调用结果
   * @author WangCaiWen
   * @date 2020/7/28
   */
  Result dissolveChatNotice(Map<String, Object> params);

  /**
   * TODO 添加通知.
   *
   * @param params 通知信息
   * @return 调用结果
   * @author WangCaiWen
   * @date 2020/7/28
   */
  Result newAddFriendNotice(Map<String, Object> params);

  /**
   * TODO 群聊通知.
   *
   * @param params [groupId, message]
   * @return [通知结果]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/11/11 15:44
   * @update 2020/11/11 15:44
   */
  Result groupNoticeMessage(Map<String, Object> params);

  /**
   * @MethodName: vipGradeNotice
   * @Description: TODO 会员升级通知
   * @Param: [params]
   * @Return: com.enuos.live.result.Result
   * @Author: xubin
   * @Date: 15:07 2020/8/25
  **/
  Result vipGradeNotice(Map<String, Object> params);
}
