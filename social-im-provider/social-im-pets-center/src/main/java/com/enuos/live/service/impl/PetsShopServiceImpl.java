package com.enuos.live.service.impl;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ObjectUtil;
import com.enuos.live.error.ErrorCode;
import com.enuos.live.mapper.PetsProductCategoryMapper;
import com.enuos.live.mapper.PetsProductInfoMapper;
import com.enuos.live.dto.PaymentDTO;
import com.enuos.live.dto.PetsShopDTO;
import com.enuos.live.pojo.*;
import com.enuos.live.pojo.Currency;
import com.enuos.live.service.PetsBackpackService;
import com.enuos.live.service.PetsShopService;
import com.enuos.live.rest.OrderFeign;
import com.enuos.live.rest.UserRemote;
import com.enuos.live.result.Result;
import com.enuos.live.server.handler.PetsHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

/**
 * @ClassName PetsShopServiceImpl
 * @Description: TODO 宠物商城
 * @Author xubin
 * @Date 2020/8/31
 * @Version V2.0
 **/
@Slf4j
@Service
public class PetsShopServiceImpl implements PetsShopService {

    @Autowired
    private PetsBackpackService petsBackpackService;

    @Autowired
    private PetsProductInfoMapper petsProductInfoMapper;

    @Autowired
    private PetsProductCategoryMapper productCategoryMapper;

    @Autowired
    private UserRemote userRemote;

    @Autowired
    private OrderFeign orderFeign;

    @Autowired
    private PetsHandler petsHandler;

    /**
     * @MethodName: getCategoryList
     * @Description: TODO 查询商品类别
     * @Param: []
     * @Return: com.enuos.live.result.Result
     * @Author: xubin
     * @Date: 17:43 2020/8/31
     **/
    @Override
    public Result getCategoryList() {
        List<PetsProductCategory> categoryList = productCategoryMapper.getCategoryList();

        return Result.success(categoryList.stream()
                .filter(category -> category.getParentId() == 0)
                .map(category -> PetsProductCategory.builder()
                        .categoryId(category.getCategoryId())
                        .categoryName(category.getCategoryName())
                        .categoryCode(category.getCategoryCode())
                        .parentId(category.getParentId())
                        .children(
                                categoryList.stream()
                                        .filter(childchild -> childchild.getParentId() == category.getCategoryId())
                                        .map(childchild -> PetsProductCategory.builder()
                                                .categoryId(childchild.getCategoryId())
                                                .categoryName(childchild.getCategoryName())
                                                .categoryCode(childchild.getCategoryCode())
                                                .parentId(childchild.getParentId())
                                                .children(
                                                        categoryList.stream()
                                                                .filter(child -> child.getParentId() == childchild.getCategoryId())
                                                                .map(child -> PetsProductCategory.builder()
                                                                        .categoryId(child.getCategoryId())
                                                                        .categoryName(child.getCategoryName())
                                                                        .categoryName(child.getCategoryCode())
                                                                        .parentId(child.getParentId()).build()).collect(Collectors.toList())
                                                ).build()).collect(Collectors.toList())
                        ).build()).collect(Collectors.toList())

        );
    }

    /**
     * @MethodName: productList
     * @Description: TODO 商品信息列表
     * @Param: []
     * @Return: com.enuos.live.result.Result
     * @Author: xubin
     * @Date: 10:16 2020/8/31
     **/
    @Override
    public Result productList(Integer categoryId) {
        log.info("按照类别查询商品信息列表,categoryId=[{}]", categoryId);
        List<PetsShopDTO> petsShopDTOs = petsProductInfoMapper.findCategoryPetsProductInfo(categoryId);
        List<PetsShopDTO> tempList = new CopyOnWriteArrayList();
        for (PetsShopDTO petsShopDTO : petsShopDTOs) {
            List<PetsProductPrice> prices = petsShopDTO.getPrices();
            if (ObjectUtil.isNotEmpty(prices)) {
                for (PetsProductPrice price : prices) {
                    List list = new ArrayList();
                    Integer price1 = price.getPrice1();
                    Integer price2 = price.getPrice2();
                    Integer payType1 = price.getPayType1();
                    Integer payType2 = price.getPayType2();
                    if (!Objects.isNull(payType1) && !Objects.isNull(price1) && 0 != price1) {
                        Map map1 = new HashMap();
                        map1.put("payType", payType1);
                        map1.put("price", price1);
                        list.add(map1);
                    }
                    if (!Objects.isNull(payType2) && !Objects.isNull(price2) && 0 != price2) {
                        Map map2 = new HashMap();
                        map2.put("payType", payType2);
                        map2.put("price", price2);
                        list.add(map2);
                    }
                    price.setPriceList(list);
                    price.setPayType1(null);
                    price.setPayType2(null);
                    price.setPrice1(null);
                    price.setPrice2(null);
                }
                tempList.add(petsShopDTO);
                tempList.forEach(shopDTO -> petsHandler.petsDressUpQualityNON_NULL(shopDTO.getEffectQuality()));
            }
        }
        return Result.success(tempList);
    }

    /**
     * @MethodName: payment
     * @Description: TODO 商品兑换
     * @Param: [dto]
     * @Return: com.enuos.live.result.Result
     * @Author: xubin
     * @Date: 13:35 2020/9/1
     **/
    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public Result payment(PaymentDTO dto) {
        log.info("商品兑换入参=[{}]", dto);
        Long userId = dto.getUserId();
        Long productId = dto.getProductId();
        Integer amount = dto.getAmount();
        Integer payType = dto.getPayType();
        Integer priceId = dto.getPriceId();
        if (userId == 0 || productId == 0 || amount < 1) {
            return Result.error(ErrorCode.DATA_ERROR);
        }
        Result result = userRemote.getCurrency(userId);
        Map<String, Object> map = result.getCode().equals(0) ? (Map<String, Object>) result.getData() : null;
        if (ObjectUtil.isEmpty(map)) {
            log.error("1040, 未查询到用户金币和钻石数据userId={}", userId);
            return Result.error(ErrorCode.CONTENT_EMPTY);
        }
        final long gold = MapUtil.getLong(map, "gold"); // 用户金币
        final long diamond = MapUtil.getLong(map, "diamond"); // 用户钻石
        if (payType == 2 && diamond <= 0) {
            log.info("用户钻石不足, userId=[{}]", userId);
            return Result.error(ErrorCode.NOT_ENOUGH_DIAMOND);
        }
        if (payType == 3 && gold <= 0) {
            log.info("用户金币不足, userId=[{}]", userId);
            return Result.error(ErrorCode.NOT_ENOUGH_GOLD);
        }
        PetsProductPayInfo payInfo = petsProductInfoMapper.getPetsProductPayInfo(productId, priceId);
        if (ObjectUtils.isEmpty(payInfo)) {
            log.info("商品不存在，未查询到商品：productId={}, userId={}, priceId={}", productId, userId, priceId);
            return Result.error(6001, "未获取到有效商品");
        }
        long priceBasic = 0L;
        if (payInfo.getPayType1() == payType) {
            priceBasic = payInfo.getPrice1();
        } else if (payInfo.getPayType2() == payType) {
            priceBasic = payInfo.getPrice2();
        } else {
            log.info("不支持的付款类型");
            return Result.error(6003, "不支持的付款类型");
        }
        if (priceBasic == 0) {
            log.info("价格为零(100)");
            return Result.error(ErrorCode.DATA_ERROR);
        }
        long orderMoney = amount * priceBasic; //总价 = 商品数量*单价
        Currency currency = new Currency();
        currency.setUserId(userId);
        currency.setOriginalDiamond(diamond);
        currency.setOriginalGold(gold);
        Map<String, Object> billMap = new HashMap(); // 入账
        billMap.put("price", -orderMoney);
        switch (payType) {
            case 2:
                long surplusDiamond = diamond - orderMoney; // 扣除后钻石
                if (surplusDiamond < 0) {
                    log.info("用户钻石不足：userId={}, diamond={}, orderMoney={}", userId, diamond, orderMoney);
                    return Result.error(80011, "钻石余额不足，当前余额：" + diamond);
                }
                currency.setDiamond(surplusDiamond);
                // 保存账单
                billMap.put("priceType", 3); // 2钻石 3:金币
                billMap.put("productName", "宠物-" + payInfo.getProductName() + " 兑换");
                break;
            case 3:
                if (orderMoney > gold) {
                    log.info("用户金币不足：userId={}", userId);
                    return Result.error(ErrorCode.NOT_ENOUGH_GOLD);
                }
                final long surplusGold = gold - orderMoney; // 扣除后金币
                currency.setGold(surplusGold);
                // 保存账单
                billMap.put("priceType", 2); // 2钻石 3:金币
                billMap.put("productName", "宠物-" + payInfo.getProductName() + " 兑换");
                break;
            default:
                log.info("不支持的付款类型");
                return Result.error(6003, "不支持的付款类型");
        }

        PetsProductBackpack backpack = new PetsProductBackpack();
        backpack.setUserId(userId);
        backpack.setProductId(productId);
        backpack.setProductCode(payInfo.getProductCode());
        backpack.setCategoryId(payInfo.getCategoryId());
        backpack.setProductNum(amount);
        Integer timeLimit = payInfo.getTimeLimit();
        if (timeLimit == -1) {
            backpack.setTimeLimit(Long.valueOf(timeLimit));// 物品有效期限
        } else {
            backpack.setTimeLimit(Long.valueOf(timeLimit * 24 * 3600));// 物品有效期限 保存为秒值
        }

        Result result2 = petsBackpackService.upProductBackpack(backpack, payInfo.getUsingPro());
        if (result2.getCode() != 0) {
            return result;
        }

        Result result1 = userRemote.upUserCurrency(currency);
        if ((result1.getCode() != 0)) {
            log.warn("物品兑换扣款失败, userId=[{}]", userId);
            throw new DataIntegrityViolationException("数据异常");
//            return Result.error(ErrorCode.NETWORK_ERROR);
        }

        billMap.put("status", 1);
        billMap.put("userId", userId);
        orderFeign.entryBill(billMap);
        if (payType == 2) {
            new Thread(() -> {
                userRemote.addGrowth(userId, Integer.parseInt(String.valueOf(orderMoney)));
            }).start();
        }
        return Result.success();
    }
}
