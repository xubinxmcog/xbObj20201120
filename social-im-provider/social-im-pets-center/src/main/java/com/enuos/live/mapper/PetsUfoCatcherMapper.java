package com.enuos.live.mapper;

import com.enuos.live.pojo.PetsUfoCatcher;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface PetsUfoCatcherMapper {

    List<PetsUfoCatcher> selectByCatcherId(@Param("catcherId") Integer catcherId);

    List<Map<String, Object>> previewPrize(@Param("catcherId") Integer catcherId);

    List<Map<String, Object>> getCatcherPrice(@Param("catcherId") Integer catcherId, @Param("drawNum") Integer drawNum);
}
