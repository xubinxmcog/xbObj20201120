package com.enuos.live.service;

import com.enuos.live.result.Result;
import java.util.Map;

/**
 * TODO 你说我猜服务接口.
 *
 * @author WangCaiWen - missiw@163.com
 * @version 2.0
 * @since 2020/7/15 19:03
 */
public interface Thesaurus30061Service {

  /**
   * TODO 提交你说我猜题目.
   *
   * @param params 参数
   * @return 结果码
   * @author WangCaiWen
   * @since 2020/7/16 - 2020/7/16
   */
  Result submitSpeakGuessQuestions(Map<String, Object> params);

  /**
   * TODO 获得你说我猜词汇.
   *
   * @return words 词汇列表
   * @author WangCaiWen - 1443710411@qq.com
   * @date 2020/8/4 13:13
   * @since 2020/8/4 13:13
   */
  Result getGuessedSaidWords();

}
