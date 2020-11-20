package com.enuos.live.mapper;

import com.enuos.live.dto.PetsShopDTO;
import com.enuos.live.pojo.PetsProductInfo;
import com.enuos.live.pojo.PetsProductPayInfo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface PetsProductInfoMapper {
    int deleteByPrimaryKey(Long id);

    int insert(PetsProductInfo record);

    PetsProductInfo selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(PetsProductInfo record);

    // 查询商品所有信息
    List<PetsShopDTO> findCategoryPetsProductInfo(@Param("categoryId") Integer categoryId);

    PetsProductPayInfo getPetsProductPayInfo(@Param("productId") Long productId, @Param("priceId") Integer priceId);
}