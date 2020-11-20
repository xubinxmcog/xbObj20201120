package com.enuos.live.mapper;

import com.enuos.live.pojo.ProductFullGift;

public interface ProductFullGiftMapper {
    int deleteByPrimaryKey(Long id);

    int insert(ProductFullGift record);

    ProductFullGift selectByPrimaryKey(Long id);

    ProductFullGift selectByProductId(Long productId);

    int updateByPrimaryKeySelective(ProductFullGift record);

}