package com.enuos.live.service;

import com.enuos.live.dto.ProductBackpackDetail;
import com.enuos.live.pojo.ProductBackpack;
import com.enuos.live.result.Result;

public interface GoodsConsumption {

    Result consumption(ProductBackpackDetail productBackpackDetail, ProductBackpack productBackpack);

    String getCode();
}
