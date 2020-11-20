package com.enuos.live.mapper;

import com.enuos.live.pojo.GameUserRecord;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 用户游玩记录(GameUserRecord)表数据库访问层
 *
 * @author makejava
 * @since 2020-05-19 10:45:13
 */
public interface GameUserRecordMapper {

  List<Map<String, Object>> getRecentlyGameList(Long userId);

  List<Map<String, Object>> getFrequentlyGameList(Long userId);

  void updateUserRecord(@Param("gameCode") Long gameCode, @Param("userId") Long userId);

  void insertUserRecord(GameUserRecord gameUserRecord);

  Integer getNumberOfData(@Param("gameCode") Long gameCode, @Param("userId") Long userId);
}
