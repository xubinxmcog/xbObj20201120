package com.enuos.live.mapper;

import com.enuos.live.teens.pojo.TeensModel;
import org.apache.ibatis.annotations.Param;

import java.util.Map;

public interface TeensModelMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(TeensModel record);

    TeensModel selectByPrimaryKey(Long userId);

    int update(TeensModel record);

    String getSalt(@Param("id") Long id);

    int delTeen(@Param("userId") Long userId);

    Boolean isPasswordRight(Map<String, Object> map);

}