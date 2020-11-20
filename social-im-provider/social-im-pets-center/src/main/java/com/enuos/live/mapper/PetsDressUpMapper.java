package com.enuos.live.mapper;

import com.enuos.live.dto.PetsDressUpDTO;
import com.enuos.live.pojo.PetsDressUp;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface PetsDressUpMapper {
    int deleteByPrimaryKey(Long id);

    int insert(PetsDressUp record);

    PetsDressUp selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(PetsDressUp record);

    Long getIsDressUp(@Param("userId") Long userId, @Param("petsId") Long petsId, @Param("backpackId") Long backpackId);

    List<PetsDressUpDTO> getDressUp(@Param("userId") Long userId, @Param("petsId") Long petsId);
}