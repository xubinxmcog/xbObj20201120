package com.enuos.live.service.impl;

import com.enuos.live.error.ErrorCode;
import com.enuos.live.mapper.Thesaurus30051Mapper;
import com.enuos.live.pojo.Thesaurus30051;
import com.enuos.live.result.Result;
import com.enuos.live.service.Thesaurus30051Service;
import com.enuos.live.utils.StringUtils;
import com.google.common.collect.Maps;
import java.time.LocalDate;
import java.util.Map;
import javax.annotation.Resource;
import org.springframework.stereotype.Service;

/**
 * TODO 谁是卧底服务实现.
 *
 * @author WangCaiWen - missiw@163.com
 * @since 2020/7/1 12:34
 */

@Service("thesaurus30051Service")
public class Thesaurus30051ServiceImpl implements Thesaurus30051Service {

  @Resource
  private Thesaurus30051Mapper thesaurus30051Mapper;

  private static final int TODAY_NUM = 10;
  private static final int WORDS_NUM = 5;

  /**
   * TODO 获得谁是卧底词汇.
   *
   * @return words
   * @author WangCaiWen
   * @since 2020/7/1 - 2020/7/1
   */
  @Override
  public Result getWhoIsSpyWords() {
    return Result.success(this.thesaurus30051Mapper.getWhoIsSpyWords());
  }

  /**
   * TODO 提交谁是卧底题目.
   *
   * @param params 参数
   * @return 结果码
   * @author WangCaiWen
   * @since 2020/7/16 - 2020/7/16
   */
  @Override
  public Result submitWhoIsSpyQuestions(Map<String, Object> params) {
    if (params == null || params.isEmpty()) {
      return Result.error(ErrorCode.QUESTIONS_PARAM);
    }
    // 检查参数
    long userId = ((Number) params.get("userId")).longValue();
    if (userId <= 0) {
      return Result.error(ErrorCode.QUESTIONS_PARAM);
    }
    Integer labelCode = (Integer) params.get("labelCode");
    if (labelCode == null || labelCode <= 0) {
      return Result.error(ErrorCode.QUESTIONS_TYPE);
    }
    // 检查参数
    String massWords = StringUtils.nvl(params.get("massWords"));
    String spyWords = StringUtils.nvl(params.get("spyWords"));
    if (StringUtils.isEmpty(massWords) || StringUtils.isEmpty(spyWords)) {
      return Result.error(ErrorCode.QUESTIONS_SPY_WORD);
    }
    // 检查词字符数量
    if (massWords.length() > WORDS_NUM || spyWords.length() > WORDS_NUM) {
      return Result.error(ErrorCode.QUESTIONS_SPY_WORD_COUNT);
    }
    // 敏感内容检测 当前无敏感词库 搁置 2020-07-16 16:07:30
    // 判断是否存在
    Integer existOne = this.thesaurus30051Mapper.findWordsIsExist(massWords, spyWords);
    Integer existTwo = this.thesaurus30051Mapper.findWordsIsExist(spyWords, massWords);
    if (existOne != null || existTwo != null) {
      return Result.error(ErrorCode.QUESTIONS_REPEAT);
    }
    Map<String, Object> result = Maps.newHashMap();
    // 检查提交数量 默认 10
    LocalDate localDate = LocalDate.now();
    result.put("userId", userId);
    result.put("startTime", localDate + " 00:00:00");
    result.put("finishTime", localDate + " 23:59:59");
    Integer quantity = this.thesaurus30051Mapper.getTodaySubmitQuantity(result);
    if (quantity >= TODAY_NUM) {
      return Result.error(ErrorCode.QUESTIONS_SUBMIT);
    }
    // 添加词汇
    Thesaurus30051 thesaurus30051 = new Thesaurus30051();
    thesaurus30051.setLexiconMass(massWords);
    thesaurus30051.setLexiconSpy(spyWords);
    thesaurus30051.setLabelId(labelCode);
    thesaurus30051.setSubmitUser(userId);
    this.thesaurus30051Mapper.insertNewWords(thesaurus30051);
    return Result.success();
  }
}
