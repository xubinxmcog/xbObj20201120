package com.enuos.live.handle.game.f30051.dddd;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.Objects;
import lombok.Data;

/**
 * TODO 投票数据.
 *
 * @author wangcaiwen|1443****11@qq.com
 * @version v1.0.0
 * @since 2020/7/7 12:47
 */

@Data
@SuppressWarnings("WeakerAccess")
public class FindUndercoverVote {
  /** 用户ID. */
  private Long userId;
  /** 投票数量. */
  private Integer voteSize = 0;
  /** 投票列表(头像列表). */
  private List<String> voteList = Lists.newCopyOnWriteArrayList();
  /** 投票玩家(用户ID). */
  private List<Long> userIds = Lists.newCopyOnWriteArrayList();

  FindUndercoverVote(Long userId) {
    this.userId = userId;
  }

  public boolean isEquals(Long userId) {
    return Objects.equals(this.userId, userId);
  }

  /**
   * TODO 被投票.
   *
   * @param userId [玩家ID]
   * @param iconUrl [头像路径]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/7/7 21:29
   * @update 2020/7/7 21:29
   */
  public void wasVoted(Long userId, String iconUrl) {
    this.userIds.add(userId);
    this.voteList.add(iconUrl);
    this.voteSize = voteList.size();
  }

}
