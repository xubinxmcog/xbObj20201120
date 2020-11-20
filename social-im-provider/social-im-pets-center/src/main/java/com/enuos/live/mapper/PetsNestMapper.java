package com.enuos.live.mapper;

import com.enuos.live.pojo.PetsNestConfig;
import com.enuos.live.pojo.PetsNestUnlock;
import org.apache.ibatis.annotations.Param;

public interface PetsNestMapper {

    PetsNestConfig selectByIdNestConfig(@Param("id") Integer id);

    int insertNestUnlock(PetsNestUnlock record);

    int updatePetsNestUnlock(PetsNestUnlock record);

    int updateCleanOneNest(@Param("userId") Long userId, @Param("nestId") Integer nestId);

    PetsNestUnlock selectByIdNestUnlock(@Param("userId") Long userId, @Param("nestId") Integer nestId);

    Integer getIsNestUnlock(@Param("userId") Long userId, @Param("nestId") Integer nestId);

    Integer getIsNestPets(@Param("userId") Long userId, @Param("petsId") Long petsId);

    PetsNestUnlock getUserNullNest(@Param("userId") Long userId);

}