package com.enuos.live.service;

import com.enuos.live.result.Result;
import java.util.Map;

/**
 * TODO 一站到底服务接口.
 *
 * @author WangCaiWen - missiw@163.com
 * @version 2.0
 * @since 2020/7/8 15:51
 */

public interface Thesaurus30071Service {

  /**
   * TODO 获得一站到底题目.
   *
   * @return problem
   * @author WangCaiWen
   * @since 2020/7/8 - 2020/7/8
   */
  Result getMustStandProblem();

  /**
   * TODO 提交一站到底题目.
   *
   * @param params 参数
   * @return 结果码
   * @author WangCaiWen
   * @since 2020/7/16 - 2020/7/16
   */
  Result submitMustStandQuestions(Map<String, Object> params);
}
