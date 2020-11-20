package com.enuos.live.service.impl;

import com.enuos.live.error.ErrorCode;
import com.enuos.live.mapper.Thesaurus30041Mapper;
import com.enuos.live.pojo.Thesaurus30041;
import com.enuos.live.result.Result;
import com.enuos.live.service.Thesaurus30041Service;
import com.enuos.live.utils.StringUtils;
import com.google.common.collect.Maps;
import java.time.LocalDate;
import java.util.Map;
import javax.annotation.Resource;
import org.springframework.stereotype.Service;

/**
 * TODO 你画我猜/你说我猜服务接口.
 *
 * @author WangCaiWen - missiw@163.com
 * @version 2.0
 * @since 2020/7/15 15:37
 */

@Service("thesaurus30040Service")
public class Thesaurus30041ServiceImpl implements Thesaurus30041Service {

  @Resource
  private Thesaurus30041Mapper thesaurus30041Mapper;

  private static final int TODAY_NUM = 10;
  private static final int WORDS_NUM = 10;

  /**
   * TODO 提交你画我猜题目.
   *
   * @param params 参数
   * @return 结果码
   * @author WangCaiWen
   * @since 2020/7/16 - 2020/7/16
   */
  @Override
  public Result submitDrawingQuestions(Map<String, Object> params) {
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
    String newWords = StringUtils.nvl(params.get("newWords"));
    if (StringUtils.isEmpty(newWords)) {
      return Result.error(ErrorCode.QUESTIONS_GUESS_WORD);
    }
    String wordsHint = StringUtils.nvl(params.get("wordsHint"));
    if (StringUtils.isEmpty(wordsHint)) {
      return Result.error(ErrorCode.QUESTIONS_GUESS_HINT);
    }
    // 检查词字符数量
    if (newWords.length() > WORDS_NUM || wordsHint.length() > WORDS_NUM) {
      return Result.error(ErrorCode.QUESTIONS_GUESS_WORD_COUNT);
    }
    // 敏感内容检测 当前无敏感词库 搁置 2020-07-16 14:17:30
    // 判断是否存在
    Integer exist = this.thesaurus30041Mapper.findWordsIsExist(newWords);
    if (exist != null) {
      return Result.error(ErrorCode.QUESTIONS_REPEAT);
    }
    Map<String, Object> result = Maps.newHashMap();
    // 检查提交数量 默认 10
    LocalDate localDate = LocalDate.now();
    result.put("userId", userId);
    result.put("startTime", localDate + " 00:00:00");
    result.put("finishTime", localDate + " 23:59:59");
    Integer quantity = this.thesaurus30041Mapper.getTodaySubmitQuantity(result);
    if (quantity >= TODAY_NUM) {
      return Result.error(ErrorCode.QUESTIONS_SUBMIT);
    }
    // 添加词汇
    Thesaurus30041 thesaurus30041 = new Thesaurus30041();
    thesaurus30041.setLexicon(newWords);
    thesaurus30041.setLexiconHint(wordsHint);
    thesaurus30041.setLexiconWords(newWords.length());
    thesaurus30041.setLabelId(labelCode);
    thesaurus30041.setSubmitUser(userId);
    this.thesaurus30041Mapper.insertNewWords(thesaurus30041);
    return Result.success();
  }
}
