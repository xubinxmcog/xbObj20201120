package com.enuos.live.controller;

import cn.hutool.core.map.MapUtil;
import com.enuos.live.annotations.NoRepeatSubmit;
import com.enuos.live.annotations.Cipher;
import com.enuos.live.dto.PaymentDTO;
import com.enuos.live.error.ErrorCode;
import com.enuos.live.mapper.UserBillMapper;
import com.enuos.live.pojo.Page;
import com.enuos.live.pojo.UserBill;
import com.enuos.live.result.Result;
import com.enuos.live.service.PaymentService;
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
 * @ClassName PaymentController
 * @Description: TODO 商城兑换
 * @Author xubin
 * @Date 2020/4/7
 * @Version V1.0
 **/
@Api("商城兑换")
@RestController
@RequestMapping("/payment")
@Slf4j
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private UserBillMapper userBillMapper;

    @NoRepeatSubmit(lockTime = 1)
    @ApiOperation(value = "商城兑换", notes = "用户兑换商品")
    @Cipher
    @PostMapping("/pricePayment")
    public Result payment(@RequestBody PaymentDTO dto) {
        final Long userId = dto.getUserId();
        final Long productId = dto.getProductId();
        final Integer amount = dto.getAmount();
        final Integer payType = dto.getPayType();
        final Integer priceId = dto.getPriceId();
        log.info("商城兑换, userId=[{}], productId=[{}], amount=[{}], payType=[{}], priceId=[{}]", userId, productId, amount, payType, priceId);
        if (userId == null || userId == 0 || productId == null || productId == 0 || amount == null || amount < 1) {
            return Result.error(ErrorCode.DATA_ERROR);
        }
        if (payType == null || payType == 0) {
            return Result.error(2010, "请选择支付方式");
        }
        // 判断如果是现金支付则返回商品信息
        if (1 == payType) {
            return paymentService.cashPayment(userId, productId, amount, payType, priceId);
        }
        return paymentService.payment(userId, productId, amount, payType, priceId);
    }


    /**
     * @MethodName: paymentVIP
     * @Description: TODO 会员充值订单生成
     * @Param: [params -> userId:用户ID productId:套餐编码]
     * @Return: com.enuos.live.result.Result
     * @Author: xubin
     * @Date: 14:14 2020/7/10
     **/
    @NoRepeatSubmit(lockTime = 1)
    @ApiOperation(value = "会员充值")
    @Cipher
    @PostMapping("/paymentVIP")
    public Result paymentVIP(@RequestBody Map<String, Object> params) {
        final Long userId = MapUtil.getLong(params, "userId");
        final String productCode = MapUtil.getStr(params, "productCode");
        if (userId == null || userId == 0 || null == productCode) {
            return Result.error(ErrorCode.DATA_ERROR);
        }
        return paymentService.paymentVIP(userId, productCode);
    }

    @ApiOperation(value = "查询兑换记录")
    @Cipher
    @PostMapping("/paymentLog")
    public Result paymentLog(@RequestBody Page page) {
        return paymentService.paymentLog(page.userId, page.pageNum, page.pageSize);
    }

    @ApiOperation(value = "入账")
    @PostMapping("/entryBill")
    public Result entryBill(@RequestBody Map<String, Object> params) {
        params.get("");
        UserBill bill = new UserBill();
        bill.setUserId(MapUtil.getLong(params, "userId"));
        bill.setPrice(MapUtil.getLong(params, "price"));
        bill.setPriceType(MapUtil.getInt(params, "priceType"));
        bill.setProductName(MapUtil.getStr(params, "productName"));
        bill.setStatus(MapUtil.getInt(params, "status"));
        return Result.success(userBillMapper.insert(bill));
    }

    @ApiOperation(value = "自定义钻石兑换金币接口")
    @Cipher
    @PostMapping("/diamondExGold")
    public Result diamondExGold(@RequestBody Map<String, Object> params) {
        Long userId = MapUtil.getLong(params, "userId");
        Long gold = MapUtil.getLong(params, "gold");

        return paymentService.diamondExGold(userId, gold);
    }

}
