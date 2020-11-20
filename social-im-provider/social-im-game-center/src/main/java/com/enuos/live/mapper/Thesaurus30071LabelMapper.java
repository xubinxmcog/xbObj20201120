package com.enuos.live.mapper;

import java.util.List;
import java.util.Map;

/**
 * TODO 一战到底(Label)数据库访问层.
 *
 * @author WangCaiWen - missiw@163.com
 * @version 2.0
 * @since 2020/7/16 12:34
 */
public interface Thesaurus30071LabelMapper {

  /**
   * TODO 获得题目标签.
   *
   * @return 标签列表
   * @author WangCaiWen
   * @since 2020/7/16 - 2020/7/16
   */
  List<Map<String, Object>> getQuestionsLabel();
}
