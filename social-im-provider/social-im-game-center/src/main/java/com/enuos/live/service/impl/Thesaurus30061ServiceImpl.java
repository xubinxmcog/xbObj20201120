package com.enuos.live.service.impl;

import com.enuos.live.error.ErrorCode;
import com.enuos.live.mapper.Thesaurus30061Mapper;
import com.enuos.live.pojo.Thesaurus30061;
import com.enuos.live.result.Result;
import com.enuos.live.service.Thesaurus30061Service;
import com.enuos.live.utils.StringUtils;
import com.google.common.collect.Maps;
import java.time.LocalDate;
import java.util.Map;
import javax.annotation.Resource;
import org.springframework.stereotype.Service;

/**
 * TODO 你说我猜服务实现.
 *
 * @author WangCaiWen - missiw@163.com
 * @version 2.0
 * @since 2020/7/16 12:36
 */

@Service("thesaurus30061Service")
public class Thesaurus30061ServiceImpl implements Thesaurus30061Service {

  @Resource
  private Thesaurus30061Mapper thesaurus30061Mapper;

  private static final int TODAY_NUM = 10;
  private static final int WORDS_NUM = 10;

  /**
   * TODO 提交你说我猜题目.
   *
   * @param params 参数
   * @return 结果码
   * @author WangCaiWen
   * @since 2020/7/16 - 2020/7/16
   */
  @Override
  public Result submitSpeakGuessQuestions(Map<String, Object> params) {
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
    // 敏感内容检测 当前无敏感词库 搁置 2020-07-16 15:59:30
    // 判断是否存在
    Integer exist = this.thesaurus30061Mapper.findWordsIsExist(newWords);
    if (exist != null) {
      return Result.error(ErrorCode.QUESTIONS_REPEAT);
    }
    Map<String, Object> result = Maps.newHashMap();
    // 检查提交数量 默认 10
    LocalDate localDate = LocalDate.now();
    result.put("userId", userId);
    result.put("startTime", localDate + " 00:00:00");
    result.put("finishTime", localDate + " 23:59:59");
    Integer quantity = this.thesaurus30061Mapper.getTodaySubmitQuantity(result);
    if (quantity >= TODAY_NUM) {
      return Result.error(ErrorCode.QUESTIONS_SUBMIT);
    }
    // 添加词汇
    Thesaurus30061 thesaurus30061 = new Thesaurus30061();
    thesaurus30061.setLexicon(newWords);
    thesaurus30061.setLexiconHint(wordsHint);
    thesaurus30061.setLexiconWords(newWords.length());
    thesaurus30061.setLabelId(labelCode);
    thesaurus30061.setSubmitUser(userId);
    this.thesaurus30061Mapper.insertNewWords(thesaurus30061);
    return Result.success();
  }

  /**
   * TODO 获得你说我猜词汇.
   *
   * @return words 词汇列表
   * @author WangCaiWen - 1443710411@qq.com
   * @date 2020/8/4 13:13
   * @since 2020/8/4 13:13
   */
  @Override
  public Result getGuessedSaidWords() {
    return Result.success(this.thesaurus30061Mapper.getGuessedSaidWords());
  }
}
