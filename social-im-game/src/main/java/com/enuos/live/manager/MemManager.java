package com.enuos.live.manager;

import com.google.common.collect.Lists;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import org.springframework.stereotype.Component;

/**
 * TODO 会员管理.
 *
 * @author wangcaiwen|1443****11@qq.com
 * @version v1.0.0
 * @since 2020/9/16 13:37
 */

@Component
public class MemManager {

  /** 会员记录. */
  private static CopyOnWriteArrayList<Long> MEMBER_REC = Lists.newCopyOnWriteArrayList();

  /**
   * TODO 新增会员.
   *
   * @param memberId [会员ID]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/9/16 13:55
   * @update 2020/9/16 13:55
   */
  public static void addMemberRec(Long memberId) {
    if (!MEMBER_REC.contains(memberId)) {
      MEMBER_REC.add(memberId);
    }
  }

  /**
   * TODO 删除会员.
   *
   * @param memberId [会员ID]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/9/16 13:56
   * @update 2020/9/16 13:56
   */
  public static void delMemberRec(Long memberId) {
    if (MEMBER_REC.contains(memberId)) {
      MEMBER_REC.removeIf(member -> Objects.equals(member, memberId));
    }
  }

  /**
   * TODO 是否存在.
   *
   * @param memberId [会员ID]
   * @return boolean [是否存在]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/9/16 13:57
   * @update 2020/9/16 13:57
   */
  public static boolean isExists(Long memberId) {
    return MEMBER_REC.contains(memberId);
  }
}
