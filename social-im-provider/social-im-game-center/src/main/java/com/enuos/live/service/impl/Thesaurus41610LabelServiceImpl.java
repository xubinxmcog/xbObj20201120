package com.enuos.live.service.impl;

import com.enuos.live.mapper.Thesaurus41610LabelMapper;
import com.enuos.live.result.Result;
import com.enuos.live.service.Thesaurus41610LabelService;
import javax.annotation.Resource;
import org.springframework.stereotype.Service;

/**
 * TODO 你画我猜/你说我猜标签服务实现.
 *
 * @author WangCaiWen - missiw@163.com
 * @version 2.0
 * @since 2020/7/16 11:25
 */

@Service("thesaurus41610LabelService")
public class Thesaurus41610LabelServiceImpl implements Thesaurus41610LabelService {

  @Resource
  private Thesaurus41610LabelMapper thesaurus41610LabelMapper;

  /**
   * TODO 获得题目标签.
   *
   * @return 标签列表
   * @author WangCaiWen
   * @since 2020/7/16 - 2020/7/16
   */
  @Override
  public Result getQuestionsLabel() {
    return Result.success(this.thesaurus41610LabelMapper.getQuestionsLabel());
  }
}
