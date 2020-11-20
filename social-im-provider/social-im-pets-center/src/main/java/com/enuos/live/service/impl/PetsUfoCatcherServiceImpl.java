package com.enuos.live.service.impl;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ObjectUtil;
import com.enuos.live.error.ErrorCode;
import com.enuos.live.mapper.PetsUfoCatcherMapper;
import com.enuos.live.pojo.Currency;
import com.enuos.live.pojo.PetsProductBackpack;
import com.enuos.live.pojo.PetsUfoCatcher;
import com.enuos.live.service.PetsBackpackService;
import com.enuos.live.service.PetsUfoCatcherService;
import com.enuos.live.rest.OrderFeign;
import com.enuos.live.rest.UserRemote;
import com.enuos.live.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @ClassName PetsUfoCatcherServiceImpl
 * @Description: TODO 抽奖
 * @Author xubin
 * @Date 2020/9/29
 * @Version V2.0
 **/
@Slf4j
@Service
public class PetsUfoCatcherServiceImpl implements PetsUfoCatcherService {

    @Autowired
    private PetsUfoCatcherMapper petsUfoCatcherMapper;

    @Autowired
    private PetsBackpackService petsBackpackService;

    @Autowired
    private UserRemote userRemote;

    @Autowired
    private OrderFeign orderFeign;

    /**
     * @MethodName: catcherPrice
     * @Description: TODO 获取抽奖价格
     * @Param: [catcherId]
     * @Return: com.enuos.live.result.Result
     * @Author: xubin
     * @Date: 14:30 2020/10/9
     **/
    @Override
    public Result catcherPrice(Integer catcherId) {
        log.info("获取抽奖价格入参,catcherId=[{}]", catcherId);

        return Result.success(petsUfoCatcherMapper.getCatcherPrice(catcherId, null));
    }

    /**
     * @MethodName: getPetsUfoCatcher
     * @Description: TODO 开始抽奖
     * @Param: [catcherId:娃娃机编号,drawNum:抽奖次数 userId:用户ID]
     * @Return: com.enuos.live.result.Result 返回结果
     * @Author: xubin
     * @Date: 16:19 2020/9/29
     **/
    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public Result getPetsUfoCatcher(Integer catcherId, Integer drawNum, Long userId) {
        log.info("抽奖入参=[catcherId={}, userId={}, num={}]", catcherId, userId, drawNum);

        Result result = userIdDeduction(catcherId, drawNum, userId);
        if (result.getCode() != 0) {
            return result;
        }

        List<PetsUfoCatcher> petsUfoCatchers = petsUfoCatcherMapper.selectByCatcherId(catcherId);
        List<PetsUfoCatcher> results = new ArrayList<>();

        for (int i = 0; i < drawNum; i++) {
            results.add(getPrizeIndex(petsUfoCatchers));
        }
        if (10 == drawNum) {
            int index = getRandom(1, results.size() - 1);
            results.remove(index);
            PetsUfoCatcher prizeMust = getPrizeMust(petsUfoCatchers);
            results.add(prizeMust);
        }
        return ufoCatcherHandle(results, userId);
    }

    /**
     * @MethodName: previewPrize
     * @Description: TODO 奖品预览
     * @Param: [catcherId]
     * @Return: com.enuos.live.result.Result
     * @Author: xubin
     * @Date: 16:56 2020/9/30
     **/
    @Override
    public Result previewPrize(Integer catcherId) {
        log.info("奖品预览入参,catcherId=[{}]", catcherId);
        List<Map<String, Object>> maps = petsUfoCatcherMapper.previewPrize(catcherId);
        if (ObjectUtil.isNotEmpty(maps)) {
            for (Map<String, Object> map : maps) {
                Integer usingPro = MapUtil.getInt(map, "usingPro");
                switch (usingPro) {
                    case 1: // 消耗品
                        map.put("productName", MapUtil.getStr(map, "productName") + "×" + MapUtil.getInt(map, "unit"));
                        break;
                    case 2:
                        map.put("productName", MapUtil.getStr(map, "productName") + "（" + MapUtil.getInt(map, "unit") + "天）");
                        break;
                    default:
                        log.warn("未知的商品类别使用属性dataViewHandle,usingPro=[{}]", usingPro);
                }
            }
        }
        return Result.success(maps);
    }

    /**
     * @MethodName: userIdDeduction
     * @Description: TODO 用户扣款
     * @Param: [catcherId, drawNum, userId]
     * @Return: com.enuos.live.result.Result
     * @Author: xubin
     * @Date: 16:33 2020/10/9
     **/
    private Result userIdDeduction(Integer catcherId, Integer drawNum, Long userId) {
        List<Map<String, Object>> catcherPrices = petsUfoCatcherMapper.getCatcherPrice(catcherId, drawNum);
        Integer payType;
        Integer drawPrice;
        if (ObjectUtil.isEmpty(catcherPrices)) {
            log.info("暂时不可抽奖,未配置对应抽奖价格, [{}]", catcherPrices);
            return Result.error(ErrorCode.CONTENT_EMPTY);
        }
        Map<String, Object> catcherPriceMap = catcherPrices.get(0);
        payType = MapUtil.getInt(catcherPriceMap, "priceType");
        drawPrice = MapUtil.getInt(catcherPriceMap, "drawPrice");

        Result result = userRemote.getCurrency(userId);
        Map<String, Object> map = result.getCode().equals(0) ? (Map<String, Object>) result.getData() : null;
        if (ObjectUtil.isEmpty(map)) {
            log.error("1040, 未查询到用户金币和钻石数据userId={}", userId);
            return Result.error(ErrorCode.CONTENT_EMPTY);
        }
        final long gold = MapUtil.getLong(map, "gold"); // 用户金币
        final long diamond = MapUtil.getLong(map, "diamond"); // 用户钻石
        if (payType == 2 && diamond < drawPrice) {
            log.info("用户钻石不足, userId=[{}]", userId);
            return Result.error(ErrorCode.NOT_ENOUGH_DIAMOND);
        }
        if (payType == 3 && gold < drawPrice) {
            log.info("用户金币不足, userId=[{}]", userId);
            return Result.error(ErrorCode.NOT_ENOUGH_GOLD);
        }

        com.enuos.live.pojo.Currency currency = new Currency();
        currency.setUserId(userId);
        currency.setOriginalDiamond(diamond);
        currency.setOriginalGold(gold);
        Map<String, Object> billMap = new HashMap(); // 入账
        billMap.put("price", -drawPrice);
        billMap.put("productName", "娃娃机抽奖");
        switch (payType) {
            case 2:
                long surplusDiamond = diamond - drawPrice; // 扣除后钻石或金币
                currency.setDiamond(surplusDiamond); // 2钻石 3:金币
                billMap.put("priceType", 2); // 2钻石 3:金币
                break;
            case 3:
                long surplusGold = gold - drawPrice; // 扣除后钻石或金币
                currency.setGold(surplusGold);
                billMap.put("priceType", 3); // 2钻石 3:金币
                break;
            default:
                log.info("不支持的付款类型");
        }
        Result result1 = userRemote.upUserCurrency(currency);
        if ((result1.getCode() != 0)) {
            log.warn("娃娃机抽奖扣款失败, userId=[{}]", userId);
            throw new DataIntegrityViolationException("数据异常");
        }
        billMap.put("status", 1);
        billMap.put("userId", userId);
        orderFeign.entryBill(billMap);
        if (payType == 2) {
            new Thread(() -> {
                userRemote.addGrowth(userId, Integer.parseInt(String.valueOf(drawPrice)));
            }).start();
        }

        return Result.success();
    }

    /**
     * @MethodName: ufoCatcherHandle
     * @Description: TODO 中奖数据处理
     * @Param: [results, userId]
     * @Return: java.util.List
     * @Author: xubin
     * @Date: 13:20 2020/9/30
     **/
    private Result ufoCatcherHandle(List<PetsUfoCatcher> results, Long userId) {

        if (results.size() > 1) {
//            System.out.println(results);
            List<PetsUfoCatcher> resultList = new ArrayList<>();
            Map<String, List<PetsUfoCatcher>> map = results.stream().collect(Collectors.groupingBy(obj -> obj.getProductCode()));

            Integer sum = 0;
            for (Map.Entry<String, List<PetsUfoCatcher>> entry : map.entrySet()) {
                List<PetsUfoCatcher> value = entry.getValue();
                sum = value.stream().mapToInt(m -> m.getUnit()).sum();
                value.get(0).setUnit(sum);
                resultList.add(value.get(0));
            }
//            System.out.println(resultList);
            setPetsBackpack(resultList, userId);
            return Result.success(dataViewHandle(resultList));
        } else {
            setPetsBackpack(results, userId);
            return Result.success(dataViewHandle(results));
        }
    }


    private List<PetsUfoCatcher> dataViewHandle(List<PetsUfoCatcher> results) {

        for (PetsUfoCatcher result : results) {
            int usingPro = result.getUsingPro();
            switch (usingPro) {
                case 1: // 消耗品
                    result.setProductName(result.getProductName() + "×" + result.getUnit());
                    break;
                case 2:
                    result.setProductName(result.getProductName() + "（" + result.getUnit() + "天）");
                    break;
                default:
                    log.warn("未知的商品类别使用属性dataViewHandle,usingPro=[{}]", usingPro);
            }

            result.setCategoryId(null);
            result.setPrizeWeigth(null);
            result.setIsMust(null);
        }
        return results;
    }

    /**
     * @MethodName: setPetsBackpack
     * @Description: TODO 抽中的奖品保存到宠物背包
     * @Param: [results, userId]
     * @Return: java.util.List
     * @Author: xubin
     * @Date: 12:48 2020/9/30
     **/
    private void setPetsBackpack(List<PetsUfoCatcher> results, Long userId) {


        for (PetsUfoCatcher ufoCatcher : results) {
            PetsProductBackpack backpack = new PetsProductBackpack();
            backpack.setUserId(userId);
            backpack.setProductId(ufoCatcher.getProductId());
            backpack.setProductCode(ufoCatcher.getProductCode());
            backpack.setCategoryId(ufoCatcher.getCategoryId());
            int usingPro = ufoCatcher.getUsingPro();
            switch (usingPro) {
                case 1: // 消耗品
                    backpack.setProductNum(ufoCatcher.getUnit());
                    backpack.setTimeLimit(-1L);
                    break;
                case 2: // 装饰品
                    backpack.setProductNum(1);
                    backpack.setTimeLimit(Long.valueOf(ufoCatcher.getUnit() * 24 * 3600));// 物品有效期限 保存为秒值
                    break;
                default:
                    log.warn("未知的商品类别使用属性setPetsBackpack,usingPro=[{}]", usingPro);
            }

            Result result2 = petsBackpackService.upProductBackpack(backpack, ufoCatcher.getUsingPro());
            if (result2.getCode() != 0) {
                log.info("抽奖结果放入背包异常结果=[{}]", result2);
            }
        }
    }


    /**
     * @MethodName: getPrizeIndex
     * @Description: TODO 必出奖品算法
     * @Param: [prizes]
     * @Return: com.enuos.live.pets.pojo.PetsUfoCatcher
     * @Author: xubin
     * @Date: 11:02 2020/9/30
     **/
    private PetsUfoCatcher getPrizeMust(List<PetsUfoCatcher> prizes) {
        List<PetsUfoCatcher> list = new ArrayList<>();
        for (int i = 0; i < prizes.size(); i++) {
            Integer isMust = prizes.get(i).getIsMust();
            if (1 == isMust) {
                list.add(prizes.get(i));
            }
        }
        Integer random = getRandom(1, list.size()) - 1;
        return list.get(random);
    }


    /**
     * @MethodName: getPrizeIndex
     * @Description: TODO 抽奖算法
     * @Param: [prizes]
     * @Return: com.enuos.live.pets.pojo.PetsUfoCatcher
     * @Author: xubin
     * @Date: 16:18 2020/9/29
     **/
    private PetsUfoCatcher getPrizeIndex(List<PetsUfoCatcher> prizes) {

        PetsUfoCatcher random = null;

        //计算总权重
        double sumWeight = 0;
        for (PetsUfoCatcher prize : prizes) {
            sumWeight += prize.getPrizeWeigth();
        }

        //产生随机数
        double randomNumber;
        randomNumber = Math.random();

        //根据随机数在所有奖品分布的区域并确定所抽奖品
        double d1 = 0;
        double d2 = 0;
        for (int i = 0; i < prizes.size(); i++) {
            d2 += Double.parseDouble(String.valueOf(prizes.get(i).getPrizeWeigth())) / sumWeight;
            if (i == 0) {
                d1 = 0;
            } else {
                d1 += Double.parseDouble(String.valueOf(prizes.get(i - 1).getPrizeWeigth())) / sumWeight;
            }
            if (randomNumber >= d1 && randomNumber <= d2) {
                random = prizes.get(i);
                break;
            }
        }
        return random;
    }

    /**
     * @MethodName: getRandom
     * @Description: TODO  产生min-max之间的随机数
     * @Param: [min:, max]
     * @Return: java.lang.Integer
     * @Author: xubin
     * @Date: 11:11 2020/9/30
     **/
    private static Integer getRandom(Integer min, Integer max) {
        int number = new Random().nextInt(max) % (max - min + 1) + min;
        return number;
    }

    ///测试代码
    public static void main(String[] args) {
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>() {
            {
                add(new HashMap<String, Object>() {{
                    put("A", 1);
                }});
                add(new HashMap<String, Object>() {{
                    put("B", 2);
                }});
                add(new HashMap<String, Object>() {{
                    put("B", 1);
                }});
                add(new HashMap<String, Object>() {{
                    put("A", 6);
                }});
                add(new HashMap<String, Object>() {{
                    put("C", 1);
                }});
            }
        };
        System.out.println(list);
        Map<String, Object> resultMap = new HashMap<String, Object>();


        List<Map<String, Object>> resultList = new ArrayList<>();

        Map<String, List<Map<String, Object>>> map = list.stream().collect(Collectors.groupingBy(ma -> MapUtils.getString(ma, "CODE")));
        Integer sum;
        for (Map.Entry<String, List<Map<String, Object>>> entry : map.entrySet()) {
            List<Map<String, Object>> value = entry.getValue();
            sum = value.stream().mapToInt(m -> MapUtils.getIntValue(m, "NUM")).sum();
            value.get(0).put("NUM", sum);
            resultList.add(value.get(0));
        }

        for (Map<String, Object> listMap : list) {

            for (Map.Entry<String, Object> entry : listMap.entrySet()) {
                String name = entry.getKey();
                Object value = entry.getValue();
                Object all = resultMap.get(entry.getKey());
                if (all == null) {
                    resultMap.put(name, value);
                } else {
                    all = new Integer((((Integer) all).intValue() + ((Integer) value).intValue()));
                    resultMap.put(name, all);
                }

            }
        }
//        List<Map<String, Object>> resultList = new ArrayList<Map<String, Object>>();
//        resultList.add(resultMap);
//        System.out.println(resultList);

    }
}
