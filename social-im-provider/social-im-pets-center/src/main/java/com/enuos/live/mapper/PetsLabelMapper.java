package com.enuos.live.mapper;

import com.enuos.live.pojo.PetsLabel;
import org.apache.ibatis.annotations.Param;

public interface PetsLabelMapper {

    int insert(PetsLabel record);

    PetsLabel selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(PetsLabel record);

    String getLabel(@Param("labelCode") String labelCode, @Param("label") String label);

}