package com.enuos.live.service.impl;

import com.enuos.live.mapper.Thesaurus30051LabelMapper;
import com.enuos.live.result.Result;
import com.enuos.live.service.Thesaurus30051LabelService;
import javax.annotation.Resource;
import org.springframework.stereotype.Service;

/**
 * TODO 谁是卧底标签服务实现.
 *
 * @author WangCaiWen - missiw@163.com
 * @version 2.0
 * @since 2020/7/16 11:23
 */

@Service("thesaurus30051LabelService")
public class Thesaurus30051LabelServiceImpl implements Thesaurus30051LabelService {

  @Resource
  private Thesaurus30051LabelMapper thesaurus30051LabelMapper;

  /**
   * TODO 获得题目标签.
   *
   * @return 标签列表
   * @author WangCaiWen
   * @since 2020/7/16 - 2020/7/16
   */
  @Override
  public Result getQuestionsLabel() {
    return Result.success(this.thesaurus30051LabelMapper.getQuestionsLabel());
  }
}
