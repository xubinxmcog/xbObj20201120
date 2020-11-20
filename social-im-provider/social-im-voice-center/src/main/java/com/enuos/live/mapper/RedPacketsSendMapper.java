package com.enuos.live.mapper;

import com.enuos.live.pojo.RedPacketsSend;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface RedPacketsSendMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(RedPacketsSend record);

    int insertSelective(RedPacketsSend record);

    RedPacketsSend selectByPrimaryKey(Integer id);

    List<RedPacketsSend> selectOverdueRp(@Param("cycle") String cycle, @Param("type") String type);

    int updateByPrimaryKeySelective(RedPacketsSend record);

    int updateBatch(List<RedPacketsSend> record);

    int updateByPrimaryKey(RedPacketsSend record);
}