package com.enuos.live.service.impl;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ObjectUtil;
import com.enuos.live.mapper.PetsUserTaskMapper;
import com.enuos.live.pojo.Currency;
import com.enuos.live.pojo.PetsProductBackpack;
import com.enuos.live.service.PetsBackpackService;
import com.enuos.live.service.PetsTaskService;
import com.enuos.live.rest.OrderFeign;
import com.enuos.live.rest.UserRemote;
import com.enuos.live.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @ClassName PetsTaskServiceImpl
 * @Description: TODO 任务中心
 * @Author xubin
 * @Date 2020/10/22
 * @Version V2.0
 **/
@Slf4j
@Service
public class PetsTaskServiceImpl implements PetsTaskService {

    @Autowired
    private PetsUserTaskMapper petsUserTaskMapper;

    @Autowired
    private PetsBackpackService petsBackpackService;

    @Autowired
    private UserRemote userRemote;

    @Autowired
    private OrderFeign orderFeign;

    @Override
    public Result getTaskList(Map<String, Object> params) {
        log.info("获取宠物任务列表入参,params=[{}]", params);
        Long userId = MapUtil.getLong(params, "userId");
        if (null == userId) {
            return Result.error(201, "用户ID不能为空");
        }

        return Result.success(petsUserTaskMapper.getTaskList(userId));
    }

    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public Result receiveReward(Map<String, Object> params) {
        log.info("领取任务奖励入参,params=[{}]", params);
        Long userId = MapUtil.getLong(params, "userId");
        Integer id = MapUtil.getInt(params, "id");
        if (null == userId || null == id) {
            return Result.error(201, "必填参数不能为空");
        }
        List<Map<String, Object>> userDoneTask = petsUserTaskMapper.getUserDoneTask(userId, id);
        if (ObjectUtil.isEmpty(userDoneTask)) {
            return Result.error(200, "没有可以领取的奖励");
        }

        List<Map<String, Object>> resultList = new ArrayList<>();
        long newGold = 0;
        long newDiamond = 0;
        Long utId = 0L;
        for (Map<String, Object> map : userDoneTask) {
            Map<String, Object> productMap = new HashMap<>();
            utId = MapUtil.getLong(map, "utId");
            String productCode = MapUtil.getStr(map, "productCode");
            String productName = MapUtil.getStr(map, "productName");
            Long productId = MapUtil.getLong(map, "productId");
            Integer num = MapUtil.getInt(map, "num");
            Integer categoryId = MapUtil.getInt(map, "categoryId");
            String categoryCode = MapUtil.getStr(map, "categoryCode"); // 商品分类编码 金币和钻石单独处理
            Integer usingPro = MapUtil.getInt(map, "usingPro"); // 类别使用属性 1:消耗品 2:装饰品 分别进行处理

            if ("DIAMOND".equals(categoryCode)) { // 金币
                newDiamond = newDiamond + num;
                productMap.put("num", num);
            } else if ("GOLDCOIN".equals(categoryCode)) { // 钻石
                newGold = newGold + num;
                productMap.put("num", num);
            } else { // 物品
                PetsProductBackpack backpack = new PetsProductBackpack();
                backpack.setUserId(userId);
                backpack.setProductId(productId);
                backpack.setProductCode(productCode);
                backpack.setCategoryId(categoryId);
                switch (usingPro) {
                    case 1: // 消耗品
                        backpack.setProductNum(num);
                        backpack.setTimeLimit(-1L);
                        productMap.put("num", num);
                        break;
                    case 2: // 装饰品
                        backpack.setProductNum(1);
                        backpack.setTimeLimit(Long.valueOf(num * 24 * 3600));// 物品有效期限 保存为秒值
                        productMap.put("num", num + " 天");
                        break;
                    default:
                        log.warn("未知的商品类别使用属性setPetsBackpack,usingPro=[{}]", usingPro);
                }

                Result result2 = petsBackpackService.upProductBackpack(backpack, usingPro);
                if (result2.getCode() != 0) {
                    log.info("抽奖结果放入背包异常结果=[{}]", result2);
                }
            }
            if (!(0 == newDiamond && 0 == newGold)) {
                Result result = userRemote.getCurrency(userId);
                Map<String, Object> userMap = result.getCode().equals(0) ? (Map<String, Object>) result.getData() : null;
                if (ObjectUtil.isEmpty(userMap)) {
                    log.error("宠物任务奖励,未查询到用户金币和钻石数据,userId=[{}],newDiamond=[{}],newGold=[{}]", userId, newDiamond, newGold);
                    throw new DataIntegrityViolationException("数据异常,未查询到用户金币和钻石数据");
                }
                final long gold = MapUtil.getLong(userMap, "gold"); // 用户金币
                final long diamond = MapUtil.getLong(userMap, "diamond"); // 用户钻石

                Map<String, Object> billMap = new HashMap(); // 入账
                billMap.put("productName", "宠物任务奖励");

                Currency currency = new Currency();
                currency.setUserId(userId);
                currency.setOriginalDiamond(diamond);
                currency.setOriginalGold(gold);
                if (newDiamond != 0) {
                    long surplusDiamond = diamond + newDiamond; // 新增后钻石
                    currency.setDiamond(surplusDiamond); // 2钻石 3:金币
                    billMap.put("price", newDiamond);
                    billMap.put("priceType", 2); // 2钻石 3:金币
                } else if (newGold != 0) {
                    long surplusGold = gold + newGold; // 新增后金币
                    currency.setGold(surplusGold);
                    billMap.put("price", newGold);
                    billMap.put("priceType", 3); // 2钻石 3:金币
                }
                Result result1 = userRemote.upUserCurrency(currency);
                if ((result1.getCode() != 0)) {
                    log.warn("宠物任务奖励钻石或金币失败, userId=[{}], newDiamond=[{}], newGold=[{}]", userId, newDiamond, newGold);
                    throw new DataIntegrityViolationException("数据异常");
                }
                billMap.put("status", 1);
                billMap.put("userId", userId);
                orderFeign.entryBill(billMap);
            }

            // 更新任务奖励状态
            if (utId != 0) {
                petsUserTaskMapper.updateIsReceive(utId);
            }

            productMap.put("productName", productName);
            resultList.add(productMap);
        }

        return Result.success(resultList);
    }
}
