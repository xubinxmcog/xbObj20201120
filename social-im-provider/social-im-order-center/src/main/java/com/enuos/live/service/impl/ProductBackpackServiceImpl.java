package com.enuos.live.service.impl;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.enuos.live.dto.*;
import com.enuos.live.error.ErrorCode;
import com.enuos.live.mapper.GiftCouponMapper1;
import com.enuos.live.mapper.ProductBackpackMapper;
import com.enuos.live.mapper.ProductInfoMapper;
import com.enuos.live.mapper.ProductServiceMapper;
import com.enuos.live.pojo.GiftCoupon1;
import com.enuos.live.pojo.ProductBackpack;
import com.enuos.live.pojo.ProductCategory;
import com.enuos.live.result.Result;
import com.enuos.live.service.GoodsConsumption;
import com.enuos.live.service.ProductBackpackService;
import com.enuos.live.service.factory.GoodsConsumptionFactory;
import com.enuos.live.utils.RedisUtils;
import com.enuos.live.utils.TimeUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.HashBiMap;
import lombok.extern.slf4j.Slf4j;
import org.omg.CORBA.LongHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ExecutorService;

/**
 * @ClassName ProductBackpackServiceImpl
 * @Description: TODO
 * @Author xubin
 * @Date 2020/4/9
 * @Version V1.0
 **/
@Service
@Slf4j
public class ProductBackpackServiceImpl implements ProductBackpackService {

    @Autowired
    private ProductBackpackMapper productBackpackMapper;

    @Autowired
    private ProductInfoMapper productInfoMapper;

    @Autowired
    private ProductServiceMapper productServiceMapper;

    @Autowired
    private RedisUtils redisUtils;

    @Autowired
    private GiftCouponMapper1 giftCouponMapper;

//    @Resource(name = "eAsyncServiceExecutor")
//    @Qualifier("eAsyncServiceExecutor")
//    private ExecutorService executorService;

    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public Result upProductBackpack(ProductBackpack productBackpack) {
        log.info("背包入参信息：【{}】", productBackpack.toString());
        List<ProductBackpack> productBackpacks = productBackpackMapper.selectByPrimaryKey(productBackpack);
        // 背包没有相同物品则新增
        if (ObjectUtils.isEmpty(productBackpacks)) {
            Date date = new Date();
            if (productBackpack.getTimeLimit() != -1) {
                long milliSecond = System.currentTimeMillis() + productBackpack.getTimeLimit() * 1000;
                date.setTime(milliSecond);
            } else {
                date.setTime(System.currentTimeMillis() + 3162240000000L); // 100年
            }
            productBackpack.setUseTime(date);
            if (productBackpack.getCategoryId() == 13 || productBackpack.getCategoryId() == 14) {
                // 更新用户头像框或聊天框
                upUserFrame(productBackpack);
                productBackpack.setProductStatus(2);
            }
            // 新增进场特效
            if (productBackpack.getCategoryId() == 17) {
                productBackpack.setProductStatus(2);
                productBackpackMapper.updateProductStatus(17, null, null, productBackpack.getUserId());
            }

            int i = productBackpackMapper.insert(productBackpack);
            if (i > 0) {
                log.info("背包添加成功id={}", productBackpack.getId());
                return Result.success(productBackpack.getId());
            } else {
                log.error("背包添加失败:{}", productBackpack.toString());
                return Result.error(ErrorCode.ERROR_OPERATION);
            }
        } else { // 有相同物品
            ProductBackpack productBackpackTemp = productBackpacks.get(0);
            Long currentTimeLimit = productBackpackTemp.getTimeLimit();// 背包中现有物品有效期时间
            if (-1 == productBackpackTemp.getUseTime().compareTo(new Date()) && currentTimeLimit != -1) {
                currentTimeLimit = 0L;
            }
            Integer productStatus = productBackpackTemp.getProductStatus();
            if (-1 == currentTimeLimit) {
                // 有相同永久物品，不可再次购买
                return Result.error(200, "有相同永久物品不可再次购买");
            } else {
                Long newProductTimeLimit = productBackpack.getTimeLimit(); // 用户购买时长
                Date date = new Date();
                if (-1 == newProductTimeLimit) { // -1:永久
                    productBackpackTemp.setTimeLimit(newProductTimeLimit);
                    date.setTime(System.currentTimeMillis() + 3153600000000L); // 100年
                } else {
                    Long totalTimeLimit = currentTimeLimit + newProductTimeLimit;
                    long milliSecond = System.currentTimeMillis() + totalTimeLimit * 1000;
                    date.setTime(milliSecond);
                    productBackpackTemp.setTimeLimit(totalTimeLimit);
                }
                productBackpackTemp.setUseTime(date);
                if (productStatus == 2 && (productBackpackTemp.getCategoryId() == 13 || productBackpackTemp.getCategoryId() == 14)) {
                    // 更新用户头像框或聊天框
                    upUserFrame(productBackpackTemp);
                }

                productBackpackTemp.setCategoryId(null);
                if (currentTimeLimit == 0) {
                    productBackpackTemp.setCreateTime(new Date());
                } else {
                    productBackpackTemp.setCreateTime(null);
                }
            }
            if (productBackpackMapper.updateByPrimaryKeySelective(productBackpackTemp) > 0) {
                log.info("背包修改成功id={}", productBackpackTemp.getId());
                return Result.success();
            } else {
                log.error("背包修改失败：{}", productBackpackTemp.toString());
                return Result.error(ErrorCode.ERROR_OPERATION);
            }
        }
    }

    public static void main(String[] args) {
        Calendar c = Calendar.getInstance();

        c.set(2020, 11, 2);
        Date before = c.getTime();

        c.set(2020, 11, 1);
        Date useTime = c.getTime();


        //before早于now，返回负数，可用于判断活动开始时间是否到了
        int compareToBefore = useTime.compareTo(before);
        System.out.println("compareToBefore = " + compareToBefore);

    }

    /**
     * @MethodName: queryBackpack
     * @Description: TODO 查看我的饰品
     * @Param: [userId, productId]
     * @Return: com.enuos.live.result.Result
     * @Author: xubin
     * @Date: 2020/7/29
     **/
    public Result getUserOrnaments(Long userId) {
        productBackpackMapper.updateUserTimeLimit(userId);
        List<CategoryBackpackDTO> categoryBackpackDTOs = productBackpackMapper.getUserOrnaments(userId);
        if (ObjectUtil.isNotEmpty(categoryBackpackDTOs)) {
            for (CategoryBackpackDTO categoryBackpackDTO : categoryBackpackDTOs) {
                List<BackpackDTO> backpackDTOs = categoryBackpackDTO.getList();
                for (BackpackDTO backpackDTO : backpackDTOs) {
                    long timeLimit = backpackDTO.getTimeLimit();//有效时长
                    if (-1 == timeLimit) {
                        backpackDTO.setTermDescribe("永久");
                    } else {
                        long createTime = backpackDTO.getCreateTime().getTime() / 1000; // 购买时间
                        long expirationTime = timeLimit + createTime; // 到期时间
                        long currentTime = System.currentTimeMillis() / 1000; // 当前时间
                        long surplusTime = expirationTime - currentTime;// 剩余时间
                        String expire = TimeUtil.getExpire(surplusTime);
                        backpackDTO.setTermDescribe(expire + "后到期");
                    }
                }
                categoryBackpackDTO.setList(backpackDTOs);
            }
        }
        return Result.success(categoryBackpackDTOs);
    }

    /**
     * @MethodName: consumption
     * @Description: TODO 使用背包物品
     * @Param: [userId, productId]
     * @Return: com.enuos.live.result.Result
     * @Author: xubin
     * @Date: 2020/4/9
     **/
    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public Result use(Integer id, Long userId, Integer amount) {
        ProductBackpack productBackpack = new ProductBackpack();
        productBackpack.setId(id);
        productBackpack.setUserId(userId);
//        List<ProductBackpack> productBackpackTemps = productBackpackMapper.selectByPrimaryKey(productBackpack);
        ProductBackpackDetail productBackpackDetail = productBackpackMapper.getProductBackpackDetail(id, userId);
        if (ObjectUtil.isEmpty(productBackpackDetail)) {
            log.info("背包物品使用查询结果为空");
            return Result.error(7002, "暂时没有可以使用的东东哦, 可以去商城买买买哦");
        }
//        ProductBackpack productBackpackTemp = productBackpackTemps.get(0);
        Long timeLimit = productBackpackDetail.getTimeLimit(); // 商品有效期限：-1：永久 0：已过期 7：7天
        if (0 == timeLimit) {
            return Result.success("已过期");
        }
        String factoryCode = productBackpackDetail.getCategoryCode().split("_")[0];
        GoodsConsumption goodsConsumption = GoodsConsumptionFactory.getGoodsConsumption(factoryCode);

        if (ObjectUtil.isEmpty(goodsConsumption)) {
            log.info("获取实例失败, 暂时不可使用, categoryCode=[{}]", productBackpackDetail.getCategoryCode());
            return Result.error(ErrorCode.DATA_ERROR);
        }
        // 背包物品使用
        return goodsConsumption.consumption(productBackpackDetail, productBackpack);
    }

    /**
     * @MethodName: gameDecorate
     * @Description: TODO 游戏物品加载查询
     * @Param: [userId, gameCode]
     * @Return: com.enuos.live.result.Result
     * @Author: xubin
     * @Date: 2020/6/8
     **/
    @Override
    public List gameDecorate(Long userId, Integer gameCode) {
        productBackpackMapper.updateUserTimeLimit(userId);
        return productBackpackMapper.getGameOrnament(userId, gameCode);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public Result addBackpack(Map<String, Object> param) {
        Long userId = MapUtil.getLong(param, "userId"); // 用户ID
        List<Map<String, Object>> list = (List<Map<String, Object>>) param.get("list");
        if (ObjectUtil.isNotEmpty(list)) {
            int i = 0;
            for (Map<String, Object> objectMap : list) {
                String rewardCode = MapUtil.getStr(objectMap, "rewardCode"); // 奖品ID
                long life = Long.valueOf(objectMap.get("life").toString()); // 奖品时效 [单位s]
                if (rewardCode.startsWith("T")) { // 券
                    log.info("保存券");
                    GiftCoupon1 giftCoupon = new GiftCoupon1();
                    giftCoupon.setUserId(userId);
                    giftCoupon.setGiftCouponId(rewardCode);
                    long timeLimit = life == 0 ? 3162240000L : life;
                    giftCoupon.setLife(timeLimit);
                    giftCouponMapper.insert(giftCoupon);

                } else {
                    Map<String, Object> idMap = productInfoMapper.getProductCoreById(rewardCode);
                    Integer categoryId = MapUtil.getInt(objectMap, "categoryId"); // 类别ID
                    if (ObjectUtil.isEmpty(categoryId)) {
                        categoryId = MapUtil.getInt(idMap, "categoryId");
                    }
                    Long productId = MapUtil.getLong(idMap, "id");
                    Integer number = MapUtil.getInt(objectMap, "number");

                    long timeLimit = life == 0 ? -1L : life;
                    ProductBackpack productBackpack = new ProductBackpack();
                    productBackpack.setUserId(userId);
                    productBackpack.setProductId(productId);
                    productBackpack.setProductNum(number);
                    productBackpack.setTimeLimit(timeLimit);
                    productBackpack.setCategoryId(categoryId);
                    productBackpack.setProductCode(rewardCode);
                    productBackpack.setProductStatus(1);
                    Result result = upProductBackpack(productBackpack);
                    if (0 == result.getCode()) {
                        i++;
                    }
                }
            }
            return Result.success(i);
        }
        return Result.error();
    }

    private void upUserFrame(ProductBackpack productBackpack) {
        UserFrameDTO dto = new UserFrameDTO();
        int i = 0;
        switch (productBackpack.getCategoryId()) {
            case 13: // 头像框
                log.info("更新用户头像框, userId=[{}]", productBackpack.getUserId());
                dto.setIfTime(productBackpack.getUseTime());
                Map<String, Object> productIf = productBackpackMapper.getProductUrl(productBackpack.getProductCode());
                dto.setIconFrame(MapUtil.getStr(productIf, "picUrl"));
                dto.setUserId(productBackpack.getUserId());
                i = productBackpackMapper.updateUserFrame(dto);
                break;
            case 14: // 聊天框
                log.info("更新用户聊天框, userId=[{}]", productBackpack.getUserId());
                dto.setCfTime(productBackpack.getUseTime());
                Map<String, Object> productC = productBackpackMapper.getProductUrl(productBackpack.getProductCode());
                dto.setChatFrame(MapUtil.getStr(productC, "picUrl"));
                dto.setUserId(productBackpack.getUserId());
                dto.setChatFrameAttribute(productC.get("attribute4"));
                i = productBackpackMapper.updateUserFrame(dto);
                break;
            default:
                log.warn("ProductBackpackServiceImpl.upUserFrame,无效分类");
        }
        if (i > 0) {
            log.info("更新用户头像框或聊天框使用状态, userId=[{}]", productBackpack.getUserId());
            productBackpackMapper.updateProductStatus(productBackpack.getCategoryId(), productBackpack.getId(),
                    null, productBackpack.getUserId());
        }
    }

}
