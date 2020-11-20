package com.enuos.live.controller;

import com.enuos.live.annotations.Cipher;
import com.enuos.live.result.Result;
import com.enuos.live.service.GameService;
import com.enuos.live.service.GameUserRecordService;
import com.enuos.live.utils.StringUtils;
import java.util.Objects;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * TODO 游戏管理(Game)控制层.
 *
 * @author WangCaiWen - missiw@163.com
 * @version 2.0
 * @since 2020-05-11 09:35:02
 */

@RestController
@RequestMapping("/home")
public class GameController {

  @Resource
  private GameService gameService;
  @Resource
  private GameUserRecordService gameUserRecordService;

  private static final int PAGE_NUM = 1;
  private static final int PAGE_SIZE = 20;
  private static final int GAME_ACTIVE_PARAM = 1;
  private static final int GAME_DEVICE_IOS_PARAM = 1;
  private static final int GAME_DEVICE_ANDROID_PARAM = 2;
  private static final String STRING_PAGE_NUM = "pageNum";
  private static final String STRING_PAGE_SIZE = "pageSize";
  private static final String DEVICE_IOS = "iOS";
  private static final String DEVICE_ANDROID = "Android";

  /**
   * TODO 主页游戏.
   *
   * @param params 参数
   * @return 游戏列表
   * @author WangCaiWen
   * @since 2020/7/16 - 2020/7/16
   */
  @Cipher
  @RequestMapping(value = "/getHomeGameList", method = RequestMethod.POST)
  public Result getHomeGameList(@RequestBody Map<String, Object> params, HttpServletRequest request) {
    String version = request.getHeader("version");
    if (StringUtils.isNotEmpty(version)) {
      params.put("version", version);
    }
    String device = request.getHeader("platform");
    if (StringUtils.isNotEmpty(device)) {
      if (Objects.equals(DEVICE_IOS, device)) {
        params.put("gameDevice", GAME_DEVICE_IOS_PARAM);
      } else if (Objects.equals(DEVICE_ANDROID, device)) {
        params.put("gameDevice", GAME_DEVICE_ANDROID_PARAM);
      }
    }
    // 设置默认页数
    if (!params.containsKey(STRING_PAGE_NUM)) {
      params.put(STRING_PAGE_NUM, PAGE_NUM);
    }
    // 设置默认条数
    if (!params.containsKey(STRING_PAGE_SIZE)) {
      params.put(STRING_PAGE_SIZE, PAGE_SIZE);
    }
    params.put("gameActive", GAME_ACTIVE_PARAM);
    return this.gameService.getHomeGameList(params);
  }

  /**
   * TODO 最近游戏.
   *
   * @param params 用户ID
   * @return 游戏列表
   * @author WangCaiWen
   * @since 2020/7/16 - 2020/7/16
   */
  @Cipher
  @PostMapping(value = "/getRecentlyGameList")
  public Result getRecentlyGameList(@RequestBody Map<String, Object> params) {
    return this.gameUserRecordService.getRecentlyGameList(params);
  }

  /**
   * TODO 获得游戏详情.
   *
   * @param params 游戏编码
   * @return 游戏详情
   * @author WangCaiWen
   * @since 2020/7/16 - 2020/7/16
   */
  @Cipher
  @PostMapping(value = "/getGameInformation")
  public Result getGameInformation(@RequestBody Map<String, Object> params, HttpServletRequest request) {
    String device = request.getHeader("platform");
    if (StringUtils.isNotEmpty(device)) {
      if (Objects.equals(DEVICE_IOS, device)) {
        params.put("gameDevice", GAME_DEVICE_IOS_PARAM);
      } else if (Objects.equals(DEVICE_ANDROID, device)) {
        params.put("gameDevice", GAME_DEVICE_ANDROID_PARAM);
      }
    }
    return this.gameService.getGameInfo(params);
  }

}