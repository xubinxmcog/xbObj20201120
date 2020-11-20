package com.enuos.live.mapper;

import com.enuos.live.pojo.ProductPicInfo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ProductPicInfoMapper {

    Integer insert(ProductPicInfo productPicInfo);

    Integer update(ProductPicInfo productPicInfo);

    List<ProductPicInfo> getProductPicInfo(@Param("id") Integer id, @Param("productId") Integer productId);
}
