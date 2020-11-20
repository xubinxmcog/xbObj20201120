package com.enuos.live.service;

import com.enuos.live.result.Result;
import java.util.Map;

/**
 * TODO 你画我猜服务接口.
 *
 * @author WangCaiWen - missiw@163.com
 * @version 2.0
 * @since 2020/7/15 15:36
 */
public interface Thesaurus30041Service {

  /**
   * TODO 提交你画我猜题目.
   *
   * @param params 参数
   * @return 结果码
   * @author WangCaiWen
   * @since 2020/7/16 - 2020/7/16
   */
  Result submitDrawingQuestions(Map<String, Object> params);
}
