package com.enuos.live.service;

import java.util.Map;

/**
 * @author WangCaiWen Created on 2020/4/29 10:30
 */
public interface NoticeService {

  /**
   * 获得通知详情
   *
   * @param publishNo the 发布编号
   * @return is Notice_Info
   */
  Map<String, Object> getNoticeInfo(Long publishNo);

}
