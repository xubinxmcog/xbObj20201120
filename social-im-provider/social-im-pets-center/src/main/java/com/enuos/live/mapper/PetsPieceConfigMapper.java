package com.enuos.live.mapper;

import com.enuos.live.pojo.PetsPieceConfig;

public interface PetsPieceConfigMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(PetsPieceConfig record);


    PetsPieceConfig selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(PetsPieceConfig record);

}