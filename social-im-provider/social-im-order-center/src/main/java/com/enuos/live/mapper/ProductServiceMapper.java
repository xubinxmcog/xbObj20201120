package com.enuos.live.mapper;

import com.enuos.live.pojo.ProductCategory;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ProductServiceMapper {

    List<ProductCategory> getCategoryList();

    ProductCategory detailCategory(Integer id);

//    Integer selectAttributeStatus(Integer id);

}
