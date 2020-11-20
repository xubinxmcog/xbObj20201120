package com.enuos.live.controller;

import com.enuos.live.annotations.Cipher;
import com.enuos.live.error.ErrorCode;
import com.enuos.live.result.Result;
import com.enuos.live.service.Thesaurus30041Service;
import com.enuos.live.service.Thesaurus30051LabelService;
import com.enuos.live.service.Thesaurus30051Service;
import com.enuos.live.service.Thesaurus30061Service;
import com.enuos.live.service.Thesaurus30071LabelService;
import com.enuos.live.service.Thesaurus30071Service;
import com.enuos.live.service.Thesaurus41610LabelService;
import java.util.Map;
import javax.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * TODO 题目管理(Questions)控制层.
 *
 * @author WangCaiWen - missiw@163.com
 * @version 2.0
 * @since 2020/7/15 15:36
 */

@RestController
@RequestMapping("/questions")
public class QuestionsController {

  /**
   * 词库.
   */
  @Resource
  private Thesaurus30041Service thesaurus30041Service;
  @Resource
  private Thesaurus30051Service thesaurus30051Service;
  @Resource
  private Thesaurus30061Service thesaurus30061Service;
  @Resource
  private Thesaurus30071Service thesaurus30071Service;
  /**
   * 标签.
   */
  @Resource
  private Thesaurus41610LabelService thesaurus41610LabelService;
  @Resource
  private Thesaurus30051LabelService thesaurus30051LabelService;
  @Resource
  private Thesaurus30071LabelService thesaurus30071LabelService;

  /**
   * TODO 获得题目标签.
   *
   * @param params 游戏编码
   * @return 标签列表
   * @author WangCaiWen
   * @since 2020/7/16 - 2020/7/16
   */
  @Cipher
  @PostMapping(value = "/getQuestionsLabel")
  public Result getQuestionsLabel(@RequestBody Map<String, Object> params) {
    Integer gameCode = (Integer) params.get("gameCode");
    if (gameCode > 0) {
      Result result;
      switch (gameCode) {
        // 你画我猜
        case 30041:
          result = this.thesaurus41610LabelService.getQuestionsLabel();
          break;
        // 谁是卧底
        case 30051:
          result = this.thesaurus30051LabelService.getQuestionsLabel();
          break;
        // 你说我猜
        case 30061:
          result = this.thesaurus41610LabelService.getQuestionsLabel();
          break;
        // 一战到底
        case 30071:
          result = this.thesaurus30071LabelService.getQuestionsLabel();
          break;
        // 错误编码
        default:
          result = Result.error(ErrorCode.QUESTIONS_CODE);
          break;
      }
      return result;
    }
    return Result.error(ErrorCode.QUESTIONS_PARAM);
  }

  /**
   * TODO 提交你画我猜题目.
   *
   * @param params 参数
   * @return 结果码
   * @author WangCaiWen
   * @since 2020/7/16 - 2020/7/16
   */
  @Cipher
  @RequestMapping(value = "/submitDrawingQuestions", method = RequestMethod.POST)
  public Result submitDrawingQuestions(@RequestBody Map<String, Object> params) {
    return this.thesaurus30041Service.submitDrawingQuestions(params);
  }

  /**
   * TODO 提交谁是卧底题目.
   *
   * @param params 参数
   * @return 结果码
   * @author WangCaiWen
   * @since 2020/7/16 - 2020/7/16
   */
  @Cipher
  @RequestMapping(value = "/submitWhoIsSpyQuestions", method = RequestMethod.POST)
  public Result submitWhoIsSpyQuestions(@RequestBody Map<String, Object> params) {
    return this.thesaurus30051Service.submitWhoIsSpyQuestions(params);
  }

  /**
   * TODO 提交你说我猜题目.
   *
   * @param params 参数
   * @return 结果码
   * @author WangCaiWen
   * @since 2020/7/16 - 2020/7/16
   */
  @Cipher
  @RequestMapping(value = "/submitSpeakGuessQuestions", method = RequestMethod.POST)
  public Result submitSpeakGuessQuestions(@RequestBody Map<String, Object> params) {
    return this.thesaurus30061Service.submitSpeakGuessQuestions(params);
  }

  /**
   * TODO 提交一站到底题目.
   *
   * @param params 参数
   * @return 结果码
   * @author WangCaiWen
   * @since 2020/7/16 - 2020/7/16
   */
  @Cipher
  @RequestMapping(value = "/submitMustStandQuestions", method = RequestMethod.POST)
  public Result submitMustStandQuestions(@RequestBody Map<String, Object> params) {
    return this.thesaurus30071Service.submitMustStandQuestions(params);
  }
}
