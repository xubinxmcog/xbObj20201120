package com.enuos.live.service.impl;

import com.enuos.live.mapper.GameMapper;
import com.enuos.live.result.Result;
import com.enuos.live.service.GameService;
import com.enuos.live.utils.StringUtils;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * TODO 游戏管理实现类.
 *
 * @author WangCaiWen - missiw@163.com
 * @version 1.0
 * @since 2020-05-19 09:25:46
 */

@Service("gameService")
public class GameServiceImpl implements GameService {

  @Resource
  private GameMapper gameMapper;

  private static final String GAME_RULES_ONE = "gameRulesOne";
  private static final String GAME_RULES_TWO = "gameRulesTwo";
  private static final String DEVICE = "gameDevice";
  private static final String DEVICE_IOS_MIN = "ios";
  private static final String DEVICE_ANDROID_MIN = "android";

  /**
   * TODO 主页游戏列表.
   *
   * @param params 参数
   * @return 游戏列表
   * @author WangCaiWen
   * @since 2020/7/20 - 2020/7/20
   */
  @Override
  public Result getHomeGameList(Map<String, Object> params) {
    Map<String, Object> result = Maps.newHashMap();
    PageHelper.startPage((Integer) params.get("pageNum"), (Integer) params.get("pageSize"));
    List<Map<String, Object>> getHomeGameList = this.gameMapper.getHomeGameList(params);
    if (CollectionUtils.isNotEmpty(getHomeGameList)) {
      getHomeGameList.forEach(objectMap -> {
        long randomNum = ((Number) (Math.random() * 4000 + 4999)).longValue();
        long playGameNum = ((Number) objectMap.get("gameNum")).longValue();
        objectMap.put("gameNum", randomNum + playGameNum);
      });
    }
    PageInfo<Map<String, Object>> gamePageInfo = new PageInfo<>(getHomeGameList);
    result.put("list", gamePageInfo.getList());
    result.put("total", gamePageInfo.getTotal());
    result.put("pageNum", gamePageInfo.getPageNum());
    result.put("pageSize", gamePageInfo.getPageSize());
    result.put("pages", gamePageInfo.getPages());
    return Result.success(result);
  }

  /**
   * TODO 邀请游戏列表.
   *
   * @param params 参数
   * @return 游戏列表
   * @author WangCaiWen
   * @since 2020/7/20 - 2020/7/20
   */
  @Override
  public Result getChatGameList(Map<String, Object> params) {
    Map<String, Object> result = Maps.newHashMap();
    PageHelper.startPage((Integer) params.get("pageNum"), (Integer) params.get("pageSize"));
    List<Map<String, Object>> gameList = this.gameMapper.getGameList();
    PageInfo<Map<String, Object>> gamePageInfo = new PageInfo<>(gameList);
    result.put("list", gamePageInfo.getList());
    result.put("total", gamePageInfo.getTotal());
    result.put("pageNum", gamePageInfo.getPageNum());
    result.put("pageSize", gamePageInfo.getPageSize());
    result.put("pages", gamePageInfo.getPages());
    return Result.success(result);
  }

  /**
   * TODO 获得游戏详情.
   *
   * @param gameId 游戏ID
   * @return 游戏详情
   * @author WangCaiWen
   * @since 2020/7/20 - 2020/7/20
   */
  @Override
  public Result getGameInfo(Long gameId) {
    Map<String, Object> gameInfo = this.gameMapper.getGameInfo(gameId);
    if (Objects.nonNull(gameInfo)) {
      List<String> rulesList = Lists.newLinkedList();
      if (StringUtils.isNotEmpty(StringUtils.nvl(gameInfo.get(GAME_RULES_ONE)))) {
        rulesList.add(StringUtils.nvl(gameInfo.get(GAME_RULES_ONE)));
      }
      if (StringUtils.isNotEmpty(StringUtils.nvl(gameInfo.get(GAME_RULES_TWO)))) {
        rulesList.add(StringUtils.nvl(gameInfo.get(GAME_RULES_TWO)));
      }
      gameInfo.put("rulesList", rulesList.size() > 0 ? rulesList : Collections.emptyList());
      gameInfo.remove("gameRulesOne");
      gameInfo.remove("gameRulesTwo");
    }
    return Result.success(gameInfo);
  }

  @Override
  public Result getGameInfo(Map<String, Object> params) {
    if (params == null || params.isEmpty()) {
      return Result.error();
    }
    long gameId = ((Number) params.get("gameCode")).longValue();
    if (gameId <= 0) {
      return Result.error();
    }
    Integer gameDevice = 0;
    if (params.containsKey(DEVICE)) {
      gameDevice = (Integer) params.get(DEVICE);
    }
    Map<String, Object> gameInfo = this.gameMapper.getGameInfo(gameId);
    if (Objects.nonNull(gameInfo)) {
      if (gameDevice == 1) {
        gameInfo.put("assetsURL", gameInfo.get(DEVICE_IOS_MIN));
        gameInfo.remove(DEVICE_IOS_MIN);
        gameInfo.remove(DEVICE_ANDROID_MIN);
      } else if (gameDevice > 1) {
        gameInfo.put("assetsURL", gameInfo.get(DEVICE_ANDROID_MIN));
        gameInfo.remove(DEVICE_IOS_MIN);
        gameInfo.remove(DEVICE_ANDROID_MIN);
      }
      List<String> rulesList = Lists.newLinkedList();
      if (StringUtils.isNotEmpty(StringUtils.nvl(gameInfo.get(GAME_RULES_ONE)))) {
        rulesList.add(StringUtils.nvl(gameInfo.get(GAME_RULES_ONE)));
      }
      if (StringUtils.isNotEmpty(StringUtils.nvl(gameInfo.get(GAME_RULES_TWO)))) {
        rulesList.add(StringUtils.nvl(gameInfo.get(GAME_RULES_TWO)));
      }
      gameInfo.put("rulesList", rulesList.size() > 0 ? rulesList : Collections.emptyList());
      gameInfo.remove(GAME_RULES_ONE);
      gameInfo.remove(GAME_RULES_TWO);
    }
    return Result.success(gameInfo);
  }


}