package com.enuos.live.mapper;

import com.enuos.live.dto.TipOffsDTO;
import com.enuos.live.pojo.TipOffs;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface TipOffUserMapper {

    Integer insert(TipOffs tipOffUser);

    Integer update(TipOffs tipOffUser);

    List<TipOffs> queryTipOffs(TipOffsDTO dto);

    TipOffs queryByIdTipOff(@Param("id") Integer id);
}
