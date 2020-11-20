package com.enuos.live.service;

import com.enuos.live.dto.ProductInfoDTO;
import com.enuos.live.pojo.OrderMsg;
import com.enuos.live.result.Result;

public interface ProductService {

    Result getCategoryList();

    Result detailCategory(Integer id);

    Result getProductInfo(ProductInfoDTO dto);

    /**
     * @Description: 充值套餐
     * @Param: []
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/6/30
     */
    Result rechargePackage();

    /**
     * @Description: 充值结果
     * @Param: [orderMsg]
     * @Return: void
     * @Author: wangyingjie
     * @Date: 2020/7/10
     */
    void rechargeResult(OrderMsg orderMsg);

}
