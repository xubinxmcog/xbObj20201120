package com.enuos.live.service.impl;

import com.enuos.live.mapper.NoticeMapper;
import com.enuos.live.service.NoticeService;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.sql.Timestamp;
import java.time.format.DateTimeFormatter;
import java.util.Map;

/**
 * @author WangCaiWen Created on 2020/4/29 10:31
 */
@Slf4j
@Service("noticeService")
public class NoticeServiceImpl implements NoticeService {

  @Resource
  private NoticeMapper noticeMapper;

  @Override
  public Map<String, Object> getNoticeInfo(Long publishNo) {
    Map<String, Object> notice = this.noticeMapper.getNoticeInfo(publishNo);
    if (Objects.nonNull(notice)) {
      DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
      Timestamp time = (Timestamp) notice.get("createTime");
      notice.put("createTime", dtf.format(time.toLocalDateTime()));
    }
    return notice;
  }
}
