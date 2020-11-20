package com.enuos.live.mapper;

import com.enuos.live.pojo.PetsProductCategory;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface PetsProductCategoryMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(PetsProductCategory record);

    PetsProductCategory selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(PetsProductCategory record);

    List<PetsProductCategory> getCategoryList();

    Integer getProductParentId(@Param("backpackId") Long backpackId);

}