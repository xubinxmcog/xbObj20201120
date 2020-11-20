package com.enuos.live.mapper;

import com.enuos.live.dto.TipOffsDTO;
import com.enuos.live.pojo.TipOffs;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface TipOffRoomMapper {

    int insert(TipOffs tipOffRoom);

    int update(TipOffs tipOffRoom);

    List<TipOffs> queryTipOffs(TipOffsDTO dto);

    TipOffs queryByIdTipOff(@Param("id") Integer id);

    Long getRoomAdminId(@Param("roomId") Integer roomId);

}
