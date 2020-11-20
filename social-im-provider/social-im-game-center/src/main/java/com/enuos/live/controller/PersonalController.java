package com.enuos.live.controller;

import com.enuos.live.annotations.Cipher;
import com.enuos.live.result.Result;
import com.enuos.live.service.GameService;
import com.enuos.live.service.GameUserRecordService;
import java.util.Map;
import javax.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * TODO 个人游戏(Personal)控制层.
 *
 * @author WangCaiWen - missiw@163.com
 * @version 1.0
 * @since 2020/7/20 19:38
 */

@RestController
@RequestMapping("/personal")
public class PersonalController {

  @Resource
  private GameService gameService;
  @Resource
  private GameUserRecordService gameUserRecordService;

  private static final int PAGE_NUM = 1;
  private static final int PAGE_SIZE = 20;

  private static final String STRING_PAGE_NUM = "pageNum";
  private static final String STRING_PAGE_SIZE = "pageSize";

  /**
   * TODO 邀请游戏列表.
   *
   * @param params 参数
   * @return 游戏列表
   * @author WangCaiWen
   * @since 2020/7/16 - 2020/7/16
   */
  @Cipher
  @RequestMapping(value = "/chatGameList", method = RequestMethod.POST)
  public Result chatGameList(@RequestBody Map<String, Object> params) {
    // 设置默认页数
    if (!params.containsKey(STRING_PAGE_NUM)) {
      params.put(STRING_PAGE_NUM, PAGE_NUM);
    }
    // 设置默认条数
    if (!params.containsKey(STRING_PAGE_SIZE)) {
      params.put(STRING_PAGE_SIZE, PAGE_SIZE);
    }
    return this.gameService.getChatGameList(params);
  }

  /**
   * TODO 常玩游戏.
   *
   * @param params 目标用户ID
   * @return 游戏列表
   * @author WangCaiWen
   * @since 2020/7/16 - 2020/7/16
   */
  @Cipher
  @PostMapping(value = "/oftenGameList")
  public Result getFrequentlyGameList(@RequestBody Map<String, Object> params) {
    return this.gameUserRecordService.getFrequentlyGameList(params);
  }

}
