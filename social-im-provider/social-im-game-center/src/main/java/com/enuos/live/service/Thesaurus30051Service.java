package com.enuos.live.service;

import com.enuos.live.result.Result;
import java.util.Map;

/**
 * TODO 谁是卧底服务接口.
 *
 * @author WangCaiWen - missiw@163.com
 * @since 2020/7/1 11:23
 */

public interface Thesaurus30051Service {

  /**
   * TODO 获得谁是卧底词汇.
   *
   * @return words
   * @author WangCaiWen
   * @since 2020/7/1 - 2020/7/1
   */
  Result getWhoIsSpyWords();

  /**
   * TODO 提交谁是卧底题目.
   *
   * @param params 参数
   * @return 结果码
   * @author WangCaiWen
   * @since 2020/7/16 - 2020/7/16
   */
  Result submitWhoIsSpyQuestions(Map<String, Object> params);
}
