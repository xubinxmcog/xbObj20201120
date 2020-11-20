package com.enuos.live.service.impl;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.enuos.live.constants.Constants;
import com.enuos.live.constants.TaskConstants;
import com.enuos.live.dto.ProductInfoDTO;
import com.enuos.live.error.ErrorCode;
import com.enuos.live.feign.UserFeign;
import com.enuos.live.mapper.OrderMapper;
import com.enuos.live.mapper.ProductInfoMapper;
import com.enuos.live.mapper.ProductServiceMapper;
import com.enuos.live.pojo.*;
import com.enuos.live.result.Result;
import com.enuos.live.service.ProductService;
import com.enuos.live.utils.BeanUtils;
import com.enuos.live.utils.RedisUtils;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * @ClassName ProductServiceImpl
 * @Description: TODO 商品管理 操作
 * @Author xubin
 * @Date 2020/4/3
 * @Version V1.0
 **/
@Service
@Slf4j
public class ProductServiceImpl implements ProductService {

    @Autowired
    private UserFeign userFeign;

    @Autowired
    private ProductServiceMapper productServiceMapper;

    @Autowired
    private ProductInfoMapper productInfoMapper;

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private RedisUtils redisUtils;

    /**
     * @MethodName: getCategoryList
     * @Description: TODO 查询商品分类列表
     * @Param: [pageNum, pageSize]
     * @Return: com.enuos.live.result.Result
     * @Author: xubin
     * @Date: 2020/4/7
     **/
    @Override
    public Result getCategoryList(/*Integer pageNum, Integer pageSize*/) {
//        PageHelper.startPage(pageNum, pageSize);
        List<ProductCategory> categoryList;
        categoryList = (List<ProductCategory>) redisUtils.get(Constants.KEY_ORDER_CATEGORY_LIST);
        if (ObjectUtil.isEmpty(categoryList)) {
            categoryList = productServiceMapper.getCategoryList();
            if (ObjectUtil.isNotEmpty(categoryList)) {
                redisUtils.set(Constants.KEY_ORDER_CATEGORY_LIST, categoryList, 3600);
            }
        }
        if (ObjectUtil.isEmpty(categoryList)) {
            log.info("商品分类列表为空");
            return Result.error(ErrorCode.CONTENT_EMPTY);
        }
        return Result.success(categoryList);

//        return Result.success(categoryList.stream()
//                .filter(distinctByKey(ProductCategory::getParentId))
//                .map(category -> new SelectOptionsDTO(
//                        category.getParentId(),category.getParentName(),
//                        categoryList.stream()
//                                .filter(gory -> gory.getParentId()==category.getParentId())
//                                .map(gory -> new SelectOptionsDTO(gory.getId(),gory.getCategoryName(),null))
//                                .collect(Collectors.toList())
//                )).collect(Collectors.toList()));
    }

    /**
     * @MethodName: detailCategory
     * @Description: TODO 查询商品分类详情
     * @Param: [id]
     * @Return: com.enuos.live.result.Result
     * @Author: xubin
     * @Date: 2020/4/7
     **/
    @Override
    public Result detailCategory(Integer id) {
        ProductCategory productCategory = productServiceMapper.detailCategory(id);
        if (ObjectUtil.isEmpty(productCategory)) {
            log.info("商品分类列表为空");
            return Result.error(ErrorCode.CONTENT_EMPTY);
        }
        return Result.success(productCategory);
    }

    /**
     * @MethodName: getProductInfo
     * @Description: TODO 商品信息查询
     * @Param: [dto]
     * @Return: com.enuos.live.result.Result
     * @Author: xubin
     * @Date: 2020/4/7
     **/
    @Override
    public Result getProductInfo(ProductInfoDTO dto) {

        if (null != dto.getPageNum() && null != dto.getPageSize() && dto.getPageNum() > 0 && dto.getPageSize() > 0) {
            PageHelper.startPage(dto.getPageNum(), dto.getPageSize());
            List<ProductInfo> productInfo = productInfoMapper.getProductInfo(dto);
            return Result.success(new PageInfo(assFormat(productInfo)));
        }
        List<ProductInfo> productInfo = productInfoMapper.getProductInfo(dto);

        return Result.success(assFormat(productInfo));
    }

    @Override
    public Result rechargePackage() {
        return Result.success(productInfoMapper.getRechargePackage(null));
    }

    /**
     * @Description: 充值结果
     * @Param: [orderMsg]
     * @Return: void
     * @Author: wangyingjie
     * @Date: 2020/7/10
     */
    @Override
    @Async
    @Transactional
    public void rechargeResult(OrderMsg orderMsg) {
        // 获取充值套餐
        Map<String, Object> map = productInfoMapper.getRechargePackageByCode(orderMsg.getProductCode());
        if (MapUtils.isEmpty(map)) {
            throw new RuntimeException("can not get member package");
        }

        String durationUnit = MapUtils.getString(map, "durationUnit");
        int duration = MapUtils.getIntValue(map, "duration");

        int result = 0;
        if ("M".equals(durationUnit)) {
            // 获取用户会员信息
            Member member = productInfoMapper.getMember(orderMsg.getUserId());
            if (member == null) {
                throw new RuntimeException("can not get member package");
            }

            int vip = member.getVip();
            LocalDateTime expirationTime = member.getExpirationTime();
            LocalDateTime currentTime = LocalDateTime.now();

            // 会员时限=当前时限，考虑延后性，为非会员
            expirationTime = expirationTime.isAfter(currentTime) ? expirationTime : currentTime;

            member.setVip(vip == 0 ? 1 : null);
            // 31天
            member.setExpirationTime(expirationTime.plusDays(duration * 31));
            result = productInfoMapper.updateExpirationTime(member);

            // 首次开通会员达成成就
            if (vip == 0) {
                achievementHandlers(orderMsg.getUserId());
            }
        }

        orderMsg.setIsHandle(result > 0 ? 1 : 2);
        int i = orderMapper.updateHandle(orderMsg.getOrderSn(), orderMsg.getIsHandle());
        log.info("会员充值处理结果,handle=[{}]", i);
    }

    // 数据格式化
    private List<ProductInfo> assFormat(List<ProductInfo> productInfo) {
        if (ObjectUtil.isEmpty(productInfo)) {
            return new ArrayList<>();
        }
        List<ProductInfo> tempList = new ArrayList<>();
        for (ProductInfo info : productInfo) {
            List<ProductPrice> prices = info.getPrices();
            if (ObjectUtil.isNotEmpty(prices)) {
                for (ProductPrice price : prices) {
                    List list = new ArrayList();
                    String price1 = price.getPrice1();
                    String price2 = price.getPrice2();
                    Integer payType1 = price.getPayType1();
                    Integer payType2 = price.getPayType2();
                    if (!Objects.isNull(payType1) && StrUtil.isNotEmpty(price1) && (!"0".equals(price1))) {
                        Map map1 = new HashMap();
                        map1.put("payType", payType1);
                        map1.put("price", price1);
                        list.add(map1);
                    }
                    if (!Objects.isNull(payType2) && StrUtil.isNotEmpty(price2) && (!"0".equals(price2))) {
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
                tempList.add(info);
            }
        }
        return tempList;
    }

    public static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
        Set<Object> seen = ConcurrentHashMap.newKeySet();
        return t -> seen.add(keyExtractor.apply(t));
    }

    /**
     * @Description: 成就处理
     * @Param: [userId]
     * @Return: void
     * @Author: wangyingjie
     * @Date: 2020/7/13
     */
    private void achievementHandlers(Long userId) {
        userFeign.achievementHandlers(new HashMap<String, Object>() {
            {
                put("userId", userId);
                put("list", BeanUtils.deepCopyByJson(TaskConstants.MEMBERLIST, ArrayList.class));
            }
        });
    }

}
