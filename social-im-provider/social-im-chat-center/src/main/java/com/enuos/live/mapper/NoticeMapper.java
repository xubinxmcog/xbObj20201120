package com.enuos.live.mapper;

import java.util.Map;

/**
 * @author WangCaiWen Created on 2020/4/29 10:31
 */
public interface NoticeMapper {

  /**
   * 获得通知详情
   *
   * @param publishNo the 发布编号
   * @return is Notice_Info
   */
  Map<String, Object> getNoticeInfo(Long publishNo);
}
