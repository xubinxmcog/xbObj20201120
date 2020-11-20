package com.enuos.live.controller;

import com.enuos.live.annotations.Cipher;
import com.enuos.live.dto.ProductInfoDTO;
import com.enuos.live.mapper.PayTypeMapper;
import com.enuos.live.result.Result;
import com.enuos.live.service.ProductService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * @ClassName ProductController
 * @Description: TODO 商品管理
 * @Author xubin
 * @Date 2020/4/3
 * @Version V1.0
 **/
@Api(description = "商品管理Controller")
@RestController
@Slf4j
@RequestMapping("/product")
public class ProductController {

    @Autowired
    private ProductService productService;

    @Autowired
    private PayTypeMapper payTypeMapper;

    @ApiOperation(value = "查询商品类别列表", notes = "用于商品浏览")
    @Cipher
    @PostMapping("/getCategoryList")
    public Result getCategoryList(/*Integer pageNum, Integer pageSize*/) {

        return productService.getCategoryList(/*pageNum, pageSize*/);
    }

    @ApiOperation(value = "查询商品分类详情", notes = "用于商品管理和商品浏览")
    @Cipher
    @PostMapping("/detailCategory/{id}")
    public Result detailCategory(@RequestBody Map<String, Integer> params) {
        return productService.detailCategory(params.get("id"));
    }

    /**
     * @Description: 会员充值套餐查询
     * @Param: []
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/6/30
     */
    @ApiOperation(value = "会员充值套餐查询")
    @Cipher
    @PostMapping("/rechargePackage")
    public Result rechargePackage() {
        return productService.rechargePackage();
    }


    @ApiOperation(value = "商品信息查询")
    @Cipher
    @PostMapping("/getProductInfo")
    public Result getProductInfo(@RequestBody ProductInfoDTO dto) {
        log.info("商品信息查询入参=[{}]", dto);
        return productService.getProductInfo(dto);
    }

    @Cipher
    @PostMapping("/getPayType")
    public Result getPayType() {
        return Result.success(payTypeMapper.selectByList());
    }

}
