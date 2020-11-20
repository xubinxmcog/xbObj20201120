package com.enuos.live.mapper;

import com.enuos.live.pojo.NoticeMember;
import org.apache.ibatis.annotations.Param;
import java.util.List;
import java.util.Map;

/**
 * @author wangcaiwen|1443****11@qq.com
 * @version v2.0.0
 * @since 2020/4/27 13:47
 */

public interface NoticeMemberMapper {

  void saveMemberNotice(NoticeMember noticeMember);

  void saveMemberNoticeList(@Param("list") List<Map<String, Object>> list);

  void saveGroupDissolveNotice(@Param("list") List<Map<String, Object>> list);

  void updateUnreadNotice(Long userId);

  void deleteMemberNoticeById(Long noticeId);

  Integer getUnreadNum(Long userId);

  String getLastMemberNoticeTitle(Long userId);

  NoticeMember getNoticeMemberInfo(Long noticeId);

  List<Map<String, Object>> memberNoticeList(Long userId);
}
