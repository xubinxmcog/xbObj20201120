package com.enuos.live.service.impl;

import com.enuos.live.mapper.Thesaurus30071LabelMapper;
import com.enuos.live.result.Result;
import com.enuos.live.service.Thesaurus30071LabelService;
import javax.annotation.Resource;
import org.springframework.stereotype.Service;

/**
 * TODO 一站到底标签服务实现.
 *
 * @author WangCaiWen - missiw@163.com
 * @version 2.0
 * @since 2020/7/16 11:24
 */

@Service("thesaurus30071LabelService")
public class Thesaurus30071LabelServiceImpl implements Thesaurus30071LabelService {

  @Resource
  private Thesaurus30071LabelMapper thesaurus30071LabelMapper;

  /**
   * TODO 获得题目标签.
   *
   * @return 标签列表
   * @author WangCaiWen
   * @since 2020/7/16 - 2020/7/16
   */
  @Override
  public Result getQuestionsLabel() {
    return Result.success(this.thesaurus30071LabelMapper.getQuestionsLabel());
  }
}
