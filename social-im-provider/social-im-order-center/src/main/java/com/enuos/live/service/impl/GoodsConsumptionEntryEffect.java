package com.enuos.live.service.impl;

import com.enuos.live.dto.ProductBackpackDetail;
import com.enuos.live.mapper.ProductBackpackMapper;
import com.enuos.live.pojo.ProductBackpack;
import com.enuos.live.result.Result;
import com.enuos.live.service.GoodsConsumption;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @ClassName GoodsConsumptionEntryEffect
 * @Description: TODO 进场特效使用
 * @Author xubin
 * @Date 2020/10/26
 * @Version V2.0
 **/
@Slf4j
@Component
public class GoodsConsumptionEntryEffect implements GoodsConsumption {

    @Autowired
    private ProductBackpackMapper productBackpackMapper;

    @Override
    public Result consumption(ProductBackpackDetail productBackpackDetail, ProductBackpack productBackpack) {

        if (productBackpackDetail.getProductStatus() == 1) {
            productBackpackDetail.setProductStatus(2);
            productBackpack.setProductStatus(2);
        } else if (productBackpackDetail.getProductStatus() == 2) {
            productBackpackDetail.setProductStatus(1);
            productBackpack.setProductStatus(1);
        }
        int i = productBackpackMapper.updateByPrimaryKeySelective(productBackpack);
        if (i > 0 && 2 == productBackpack.getProductStatus()) {
            productBackpackMapper.updateProductStatus(productBackpackDetail.getCategoryId(), productBackpackDetail.getId(), null, productBackpackDetail.getUserId());
        }

        return Result.success(productBackpackDetail);
    }

    @Override
    public String getCode() {
        return "MARCH";
    }
}
