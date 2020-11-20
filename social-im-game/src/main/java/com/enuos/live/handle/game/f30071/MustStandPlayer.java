package com.enuos.live.handle.game.f30071;

import com.enuos.live.codec.Packet;
import io.netty.channel.Channel;
import java.time.Duration;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * TODO 参与者信息.
 *
 * @author WangCaiWen - missiw@163.com
 * @since 2020/7/8 14:35
 */

@Data
@SuppressWarnings("WeakerAccess")
public class MustStandPlayer {

  /**
   * 用户ID「例如：1234567890」.
   */
  private Long userId = 0L;
  /**
   * 玩家昵称「例如：Lake」.
   */
  private String userName;
  /**
   * 玩家头像「例如：http://pic1.zhimg.com/50/v2-fbe356c74816b9b3cfcf80ff1758fbd4_hd.jpg」.
   */
  private String userIcon;
  /**
   * 玩家性别 1「男」 2「女」.
   */
  private Integer userSex;
  /**
   * 通讯通道「例如：[id: 0x2a4d4e79, L:/192.168.0.122:9199 - R:/192.168.0.122:52620]」.
   */
  private Channel channel;
  /**
   * 用户身份 1「红」 2「黑」.
   */
  private Integer identity;
  /**
   * 操作时间「例如：3(s)」.
   */
  private Integer actionTime = 0;
  /**
   * 用户选择「例如：2」.
   */
  private Integer selectIndex = 0;
  /**
   * 用户得分「例如：25」.
   */
  private Integer userScore = 0;
  /**
   * 双倍卡「默认 3 张」.
   */
  private Integer doubleCard = 3;
  /**
   * 使用加倍.
   */
  private Integer useDouble = 0;
  /**
   * 使用加倍答对.
   */
  private Integer useDoubleAnswer = 0;
  /**
   * 答对全部.
   */
  private Integer correctAll = 0;
  /**
   * 答对全部并且小于两秒.
   */
  private Integer correctAllAndTime = 0;
  /**
   * 回答错误.
   */
  private Integer wrongAnswer = 0;
  /**
   * 连续答对.
   */
  private Integer continuousAnswer = 0;


  /**
   * TODO 用户ID判断.
   *
   * @param userId 用户ID
   * @author WangCaiWen
   * @since 2020/7/8 - 2020/7/8
   */
  public boolean isBoolean(Long userId) {
    return this.userId.equals(userId);
  }

  /**
   * TODO 发送数据.
   *
   * @param packet 数据包
   * @author WangCaiWen
   * @since 2020/7/8 - 2020/7/8
   */
  public void sendPacket(Packet packet) {
    if (channel.isActive()) {
      channel.writeAndFlush(packet);
    }
  }

  /**
   * TODO 使用加倍.
   *
   * @author WangCaiWen
   * @since 2020/7/8 - 2020/7/8
   */
  public void useDoubleCard() {
    this.doubleCard = doubleCard - 1;
    this.useDouble = 1;
  }

  /**
   * TODO 初始信息.
   *
   * @author WangCaiWen
   * @since 2020/7/9 - 2020/7/9
   */
  public void initUserInfo() {
    this.actionTime = 0;
    this.selectIndex = 0;
    this.useDouble = 0;
  }

  /**
   * TODO 选择答案.
   *
   * @param nds 回合开始时间
   * @param index 答案标记
   * @author WangCaiWen
   * @since 2020/7/9 - 2020/7/9
   */
  public int selectAnswer(LocalDateTime nds, Integer index) {
    this.selectIndex = index;
    LocalDateTime udt = LocalDateTime.now();
    Duration duration = Duration.between(nds, udt);
    this.actionTime = Math.toIntExact(duration.getSeconds());
    return actionTime;
  }

  /**
   * TODO 刷新分数.
   *
   * @param score 获得分数
   * @author WangCaiWen
   * @since 2020/7/9 - 2020/7/9
   */
  public void refreshScore(Integer score) {
    this.userScore = userScore + score;
  }

}
