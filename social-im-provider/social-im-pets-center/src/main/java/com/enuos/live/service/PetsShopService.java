package com.enuos.live.service;

import com.enuos.live.dto.PaymentDTO;
import com.enuos.live.result.Result;

/**
 * @ClassName PetsProductService
 * @Description: TODO 宠物商城
 * @Author xubin
 * @Date 2020/8/31
 * @Version V2.0
 **/
public interface PetsShopService {

    /**
     * @MethodName: getCategoryList
     * @Description: TODO 查询商品类别
     * @Param: []
     * @Return: com.enuos.live.result.Result
     * @Author: xubin
     * @Date: 17:43 2020/8/31
     **/
    Result getCategoryList();

    /**
     * @MethodName: productList
     * @Description: TODO 商品信息列表
     * @Param: []
     * @Return: com.enuos.live.result.Result
     * @Author: xubin
     * @Date: 10:16 2020/8/31
     **/
    Result productList(Integer categoryId);

    /**
     * @MethodName: payment
     * @Description: TODO 商品兑换
     * @Param: [dto]
     * @Return: com.enuos.live.result.Result
     * @Author: xubin
     * @Date: 13:35 2020/9/1
     **/
    Result payment(PaymentDTO dto);

}
