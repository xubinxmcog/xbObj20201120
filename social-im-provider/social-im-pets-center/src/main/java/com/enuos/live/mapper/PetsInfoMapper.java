package com.enuos.live.mapper;

import com.enuos.live.dto.PetsInfoAndDressUpDTO;
import com.enuos.live.dto.PetsNestDTO;
import com.enuos.live.pojo.PetsInfo;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface PetsInfoMapper {
    int deleteByPrimaryKey(Long id);

    int insert(PetsInfo record);

    PetsInfo selectByPrimaryKey(@Param("id") Long id, @Param("userId") Long userId, @Param("petCode") String petCode);

    List<PetsInfo> selectAllUserPetss(Long userId);

    List<PetsInfo> selectUserPetss(Long userId);

    List<PetsNestDTO> selectUserNestPetss(@Param("userId") Long userId);

    int updateByPrimaryKeySelective(PetsInfo record);

    Map<String, Object> getPetIdName(@Param("id") Long id);

    Integer getIsPets(@Param("petsId") Long petsId, @Param("userId") Long userId);

    PetsInfoAndDressUpDTO getPetsInfoAndDressUp(@Param("userId") Long userId, @Param("petsId") Long petsId);

}