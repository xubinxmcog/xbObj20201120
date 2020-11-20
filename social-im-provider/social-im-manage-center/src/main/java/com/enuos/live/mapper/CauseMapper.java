package com.enuos.live.mapper;

import com.enuos.live.pojo.Cause;
import java.util.List;

public interface CauseMapper {
    int deleteByPrimaryKey(Long id);

    int insert(Cause record);

    Cause selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(Cause record);

    List<Cause> getCauseList(Integer type);

}