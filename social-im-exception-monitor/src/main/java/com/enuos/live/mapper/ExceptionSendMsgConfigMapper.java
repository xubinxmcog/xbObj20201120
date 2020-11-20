package com.enuos.live.mapper;

import com.enuos.live.pojo.ExceptionSendMsgConfig;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface ExceptionSendMsgConfigMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(ExceptionSendMsgConfig record);

    ExceptionSendMsgConfig selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(ExceptionSendMsgConfig record);

    List<String> selectUserEmail(@Param("eType") Integer eType);

}