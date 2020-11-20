package com.enuos.live.service.impl;

import com.enuos.live.assets.GameInviteAssets;
import com.enuos.live.mapper.GameUserRecordMapper;
import com.enuos.live.pojo.GameUserRecord;
import com.enuos.live.result.Result;
import com.enuos.live.service.GameUserRecordService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * TODO 游玩记录(GameUserRecord)服务实现类.
 *
 * @author WangCaiWen - missiw@163.com
 * @version 1.0
 * @since 2020-05-19 10:45:13
 */

@Service("gameUserRecordService")
public class GameUserRecordServiceImpl implements GameUserRecordService {

  @Resource
  private GameUserRecordMapper gameUserRecordMapper;

  @Override
  public Result getRecentlyGameList(Map<String, Object> params) {
    if (params == null || params.isEmpty()) {
      return Result.error();
    }
    // long userId = ((Number) params.get("userId")).longValue();

    Long userId = MapUtils.getLong(params, "userId");

    if (userId <= 0) {
      return Result.error();
    }
    List<Map<String, Object>> recentlyGameList = this.gameUserRecordMapper.getRecentlyGameList(userId);
    return Result.success(recentlyGameList);
  }

  @Override
  public Result getFrequentlyGameList(Map<String, Object> params) {
    if (params == null || params.isEmpty()) {
      return Result.error();
    }
    Long userId = MapUtils.getLong(params, "targetUserId");
    if (userId <= 0) {
      return Result.error();
    }
    int isChat = 0;
    if (params.containsKey("isChat")) {
      isChat = (Integer) params.get("isChat");
    }
    List<Map<String, Object>> frequentlyGameList = this.gameUserRecordMapper.getFrequentlyGameList(userId);
    if (CollectionUtils.isNotEmpty(frequentlyGameList) && isChat == 1) {
      frequentlyGameList = frequentlyGameList.stream()
          .filter(objectMap -> GameInviteAssets.getErrorInvite(((Number)objectMap.get("gameCode")).longValue()) == null)
          .collect(Collectors.toList());
    }
    return Result.success(frequentlyGameList);
  }

  @Override
  public Result refreshUserRecord(Map<String, Object> params) {
    Long gameCode = ((Number) params.get("gameId")).longValue();
    Long userId = ((Number) params.get("userId")).longValue();
    if (gameCode <= 0 || userId <= 0) {
      return Result.error();
    }
    Integer numberOfData = this.gameUserRecordMapper.getNumberOfData(gameCode, userId);
    if (numberOfData > 0) {
      this.gameUserRecordMapper.updateUserRecord(gameCode, userId);
    } else {
      GameUserRecord gameUserRecord = new GameUserRecord();
      gameUserRecord.setUserId(userId);
      gameUserRecord.setGamePlay(1);
      gameUserRecord.setGameCode(gameCode);
      this.gameUserRecordMapper.insertUserRecord(gameUserRecord);
    }
    return Result.success();
  }

}