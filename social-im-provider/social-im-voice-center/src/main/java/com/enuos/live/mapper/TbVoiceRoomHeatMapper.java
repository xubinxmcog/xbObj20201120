package com.enuos.live.mapper;

import com.enuos.live.dto.RoomHeatDTO;
import com.enuos.live.pojo.TbVoiceRoomHeat;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface TbVoiceRoomHeatMapper {

    int insert(TbVoiceRoomHeat record);

    TbVoiceRoomHeat selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(TbVoiceRoomHeat record);

    int insertBatchRoomHeat(@Param("list") List<TbVoiceRoomHeat> list);

    List<String> getRoomIds();

    List<RoomHeatDTO> getRoomHeat();

}