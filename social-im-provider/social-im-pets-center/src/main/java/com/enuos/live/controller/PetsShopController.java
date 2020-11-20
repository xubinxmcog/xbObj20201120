package com.enuos.live.controller;

import com.enuos.live.error.ErrorCode;
import com.enuos.live.dto.PaymentDTO;
import com.enuos.live.service.PetsShopService;
import com.enuos.live.result.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * @ClassName PetsShopController
 * @Description: TODO  商店
 * @Author xubin
 * @Date 2020/8/31
 * @Version V2.0
 **/
@RestController
@RequestMapping("/pets/shop")
public class PetsShopController {

    @Autowired
    private PetsShopService petsShopService;

    /**
     * @MethodName: getCategoryList
     * @Description: TODO 查询商品类别
     * @Param: []
     * @Return: com.enuos.live.result.Result
     * @Author: xubin
     * @Date: 17:42 2020/8/31
     **/
    @GetMapping("/getCategoryList")
    public Result getCategoryList() {

        return petsShopService.getCategoryList();
    }

    /**
     * @MethodName: productList
     * @Description: TODO 商店商品信息列表
     * @Param: []
     * @Return: com.enuos.live.result.Result
     * @Author: xubin
     * @Date: 15:47 2020/8/31
     **/
    @GetMapping("/productList")
    public Result productList(@RequestParam(value = "categoryId") Integer categoryId) {
        return petsShopService.productList(categoryId);
    }

    /**
     * @MethodName: payment
     * @Description: TODO 商品兑换
     * @Param: [dto]
     * @Return: com.enuos.live.result.Result
     * @Author: xubin
     * @Date: 13:31 2020/9/1
     **/
    @PostMapping("/payment")
    public Result payment(@Validated @RequestBody PaymentDTO dto, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return Result.error(ErrorCode.EXCEPTION_CODE, bindingResult.getAllErrors().get(0).getDefaultMessage());
        }
        return petsShopService.payment(dto);
    }
}
