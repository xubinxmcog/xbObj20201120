package com.enuos.live.service.impl;

import com.enuos.live.error.ErrorCode;
import com.enuos.live.mapper.Thesaurus30071Mapper;
import com.enuos.live.pojo.Thesaurus30071;
import com.enuos.live.result.Result;
import com.enuos.live.service.Thesaurus30071Service;
import com.enuos.live.utils.StringUtils;
import com.google.common.collect.Maps;
import java.time.LocalDate;
import java.util.Map;
import javax.annotation.Resource;
import org.springframework.stereotype.Service;

/**
 * TODO 一站到底服务实现.
 *
 * @author WangCaiWen - missiw@163.com
 * @version 2.0
 * @since 2020/7/8 15:55
 */

@Service("thesaurus30071Service")
public class Thesaurus30071ServiceImpl implements Thesaurus30071Service {

  @Resource
  private Thesaurus30071Mapper thesaurus30071Mapper;

  private static final int TODAY_NUM = 10;
  private static final int ANSWER_NUM = 15;
  private static final int PROBLEM_NUM = 80;
  private static final String WRONG_ONE = "wrongOne";
  private static final String WRONG_TWO = "wrongTwo";
  private static final String WRONG_THREE = "wrongThree";

  /**
   * TODO 获得一站到底题目.
   *
   * @return problem
   * @author WangCaiWen
   * @since 2020/7/8 - 2020/7/8
   */
  @Override
  public Result getMustStandProblem() {
    return Result.success(this.thesaurus30071Mapper.getMustStandProblem());
  }

  /**
   * TODO 提交一站到底题目.
   *
   * @param params 参数
   * @return 结果码
   * @author WangCaiWen
   * @since 2020/7/16 - 2020/7/16
   */
  @Override
  public Result submitMustStandQuestions(Map<String, Object> params) {
    Thesaurus30071 thesaurus30071 = new Thesaurus30071();
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
    // 检查题目
    String newProblem = StringUtils.nvl(params.get("newProblem"));
    if (StringUtils.isEmpty(newProblem)) {
      return Result.error(ErrorCode.QUESTIONS_STAND_WORD);
    }
    // 检查词字符数量
    if (newProblem.length() > PROBLEM_NUM) {
      return Result.error(ErrorCode.QUESTIONS_STAND_COUNT);
    }
    // 检查答案
    String problemAnswer = StringUtils.nvl(params.get("problemAnswer"));
    if (StringUtils.isEmpty(newProblem)) {
      return Result.error(ErrorCode.QUESTIONS_STAND_ANSWER);
    }
    // 检查词字符数量
    if (problemAnswer.length() > ANSWER_NUM) {
      return Result.error(ErrorCode.QUESTIONS_STAND_WORD_COUNT);
    }
    // 检查错误答案
    if (params.containsKey(WRONG_ONE)) {
      String wrongOne = StringUtils.nvl(params.get(WRONG_ONE));
      if (StringUtils.isEmpty(wrongOne)) {
        return Result.error(ErrorCode.QUESTIONS_STAND_ERROR);
      }
      // 检查词字符数量
      if (wrongOne.length() > ANSWER_NUM) {
        return Result.error(ErrorCode.QUESTIONS_STAND_WORD_COUNT);
      }
      thesaurus30071.setWrongA(wrongOne);
    } else {
      return Result.error(ErrorCode.QUESTIONS_STAND_ERROR);
    }
    if (params.containsKey(WRONG_TWO)) {
      String wrongTwo = StringUtils.nvl(params.get(WRONG_TWO));
      // 检查词字符数量
      if (wrongTwo.length() > ANSWER_NUM) {
        return Result.error(ErrorCode.QUESTIONS_STAND_WORD_COUNT);
      }
      if (StringUtils.isNotEmpty(wrongTwo) && wrongTwo.length() > 0) {
        thesaurus30071.setWrongB(wrongTwo);
      }
    }
    if (params.containsKey(WRONG_TWO) && params.containsKey(WRONG_THREE)) {
      String wrongThree = StringUtils.nvl(params.get(WRONG_THREE));
      // 检查词字符数量
      if (wrongThree.length() > ANSWER_NUM) {
        return Result.error(ErrorCode.QUESTIONS_STAND_WORD_COUNT);
      }
      if (StringUtils.isNotEmpty(wrongThree) && wrongThree.length() > 0) {
        thesaurus30071.setWrongC(wrongThree);
      }
    }
    // 敏感内容检测 当前无敏感词库 搁置 2020-07-16 16:07:30
    // 判断是否存在
    Integer exist = this.thesaurus30071Mapper.findProblemIsExist(newProblem);
    if (exist != null) {
      return Result.error(ErrorCode.QUESTIONS_STAND_PROBLEM);
    }
    Map<String, Object> result = Maps.newHashMap();
    // 检查提交数量 默认 10
    LocalDate localDate = LocalDate.now();
    result.put("userId", userId);
    result.put("startTime", localDate + " 00:00:00");
    result.put("finishTime", localDate + " 23:59:59");
    Integer quantity = this.thesaurus30071Mapper.getTodaySubmitQuantity(result);
    if (quantity >= TODAY_NUM) {
      return Result.error(ErrorCode.QUESTIONS_SUBMIT);
    }
    thesaurus30071.setTitle(newProblem);
    thesaurus30071.setAnswer(problemAnswer);
    thesaurus30071.setLabelId(labelCode);
    thesaurus30071.setSubmitUser(userId);
    this.thesaurus30071Mapper.insertNewProblem(thesaurus30071);
    return Result.success();
  }
}
