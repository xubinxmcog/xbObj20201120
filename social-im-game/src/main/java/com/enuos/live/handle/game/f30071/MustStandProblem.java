package com.enuos.live.handle.game.f30071;

import com.enuos.live.utils.StringUtils;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import lombok.Data;

/**
 * TODO 一站到底.问题实体.
 *
 * @author WangCaiWen - missiw@163.com
 * @since 2020/7/8 16:39
 */

@Data
@SuppressWarnings("WeakerAccess")
public class MustStandProblem {

  /**
   * 标签类型「例如：常识」.
   */
  private String label;
  /**
   * 游戏问题「例如：于2012年提出破产保护的原世界最大的照相机，胶卷生产供应商是哪家？」.
   */
  private String title;
  /**
   * 问题答案「例如：柯达」.
   */
  private String answer;
  /**
   * 错误的A「例如：佳能」.
   */
  private String wrongA;
  /**
   * 错误的B「例如：尼康」.
   */
  private String wrongB;
  /**
   * 错误的C「例如：索尼」.
   */
  private String wrongC;
  /**
   * 问题数据.
   */
  private Map<Integer, String> problemMap = Maps.newHashMap();

  /**
   * TODO 打乱选择.
   *
   * @author WangCaiWen
   * @since 2020/7/8 - 2020/7/8
   */
  public void disruptChoose() {
    List<String> tempList = Lists.newLinkedList();
    tempList.add(answer);
    if (StringUtils.isNotEmpty(wrongA)) {
      tempList.add(wrongA);
    }
    if (StringUtils.isNotEmpty(wrongB)) {
      tempList.add(wrongB);
    }
    if (StringUtils.isNotEmpty(wrongC)) {
      tempList.add(wrongC);
    }
    Collections.shuffle(tempList);
    int index = 1;
    for (String choose : tempList) {
      this.problemMap.put(index, choose);
      index++;
    }
  }

  /**
   * TODO 获得选择内容.
   *
   * @param seatKey 选择位置
   * @return 描述内容
   * @author WangCaiWen
   * @since 2020/7/8 - 2020/7/8
   */
  public String getChooseDesc(Integer seatKey) {
    return problemMap.get(seatKey);
  }

  /**
   * TODO 获得正确答案位置.
   *
   * @return 答案位置
   * @author WangCaiWen
   * @since 2020/7/9 - 2020/7/9
   */
  public Integer getCorrectAnswerNo() {
    Integer indexKey = 0;
    for (Integer integer : problemMap.keySet()) {
      if (answer.equals(problemMap.get(integer))) {
        indexKey = integer;
        break;
      }
    }
    return indexKey;
  }

}
