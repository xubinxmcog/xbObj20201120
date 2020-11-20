package com.enuos.live.mapper;

import com.enuos.live.dto.PetsTaskDTO;
import com.enuos.live.pojo.PetsUserTask;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface PetsUserTaskMapper {
    int deleteByPrimaryKey(Long id);

    // 删除过期任务
    int deleteExpireTask();

    int insert(PetsUserTask record);

    // 不重复插入
    int insertNotExists(PetsUserTask record);

    // 根据ID查询
    PetsUserTask selectByPrimaryKey(Long id);

    // 根据ID更新
    int updateByPrimaryKeySelective(PetsUserTask record);

    int updateIsReceive(@Param("id") Long id);

    int updateFinishValue(PetsUserTask record);

    Integer getPetsTaskType(@Param("id") Integer id, @Param("userId") Long userId);

    List<PetsTaskDTO> getTaskList(@Param("userId") Long userId);

    List<Map<String, Object>> getUserDoneTask(@Param("userId") Long userId, @Param("id") Integer id);

}