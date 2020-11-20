package com.enuos.live.mapper;

import java.util.List;
import java.util.Map;

/**
 * TODO 你画我猜/你说我猜(Label)数据库访问层.
 *
 * @author WangCaiWen - missiw@163.com
 * @version 2.0
 * @since 2020/7/16 11:29
 */

public interface Thesaurus41610LabelMapper {

  /**
   * TODO 获得题目标签.
   *
   * @return 标签列表
   * @author WangCaiWen
   * @since 2020/7/16 - 2020/7/16
   */
  List<Map<String, Object>> getQuestionsLabel();
}
