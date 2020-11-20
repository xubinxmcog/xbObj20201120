package com.enuos.live.mapper;

import com.enuos.live.dto.TipOffsDTO;
import com.enuos.live.pojo.TipOffs;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface TipOffGroupMapper {

    int insert(TipOffs tipOffGroup);

    int update(TipOffs tipOffGroup);

    List<TipOffs> queryTipOffs(TipOffsDTO dto);

    TipOffs queryByIdTipOff(@Param("id") Integer id);

    Long getGroupAdminId(@Param("groupId") Long groupId);

}
