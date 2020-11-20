package com.enuos.live.mapper;

import com.enuos.live.pojo.SolveProblem;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface SolveProblemMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(SolveProblem record);

    SolveProblem selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(SolveProblem record);

    List<SolveProblem> selectByKeyword(@Param("keyword") Integer keyword);

    List<SolveProblem> findAllSolveProblem();

}