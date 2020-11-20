package com.enuos.live.service.impl;

import cn.hutool.core.map.MapUtil;
import com.alibaba.fastjson.JSONObject;
import com.enuos.live.error.ErrorCode;
import com.enuos.live.mapper.PetsInfoMapper;
import com.enuos.live.mapper.PetsProductBackpackMapper;
import com.enuos.live.pojo.PetsInfo;
import com.enuos.live.result.Result;
import com.enuos.live.service.PetsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @ClassName PetsServiceImpl
 * @Description: TODO 宠物service
 * @Author xubin
 * @Date 2020/8/24
 * @Version V2.0
 **/
@Slf4j
@Service
public class PetsServiceImpl implements PetsService {

    @Autowired
    private PetsInfoMapper petsInfoMapper;

    @Autowired
    private PetsProductBackpackMapper petsProductBackpackMapper;

    /**
     * @MethodName: getInfo
     * @Description: TODO 获取用户宠物信息
     * @Param: [userId]
     * @Return: com.enuos.live.result.Result
     * @Author: xubin
     * @Date: 14:56 2020/8/24
     **/
    @Override
    public Result getInfo(Long userId) {
        return Result.success(petsInfoMapper.selectUserPetss(userId));
    }

    /**
     * @MethodName: getOperation
     * @Description: TODO 操作
     * @Param: [userId, operation]
     * @Return: com.enuos.live.result.Result
     * @Author: xubin
     * @Date: 17:46 2020/8/26
     **/
    @Override
    public Result getOperation(Long userId, Integer operation) {
        return Result.success(petsProductBackpackMapper.getUserCategory(userId, operation));
    }

    /**
     * @MethodName: foodOrToys
     * @Description: TODO 喂食或玩具
     * @Param: [userId, petCode宠物ID, id, operation]
     * @Return: com.enuos.live.result.Result
     * @Author: xubin
     * @Date: 14:35 2020/8/28
     **/
    @Transactional
    @Override
    public Result foodOrToys(Long userId, Long targetUserId, String petCode, String id, Integer operation) {
        String labelValue = "";
        switch (operation) {
            case 1:
                labelValue = "food"; // 喂食
                break;
            case 2:
                labelValue = "toys"; // 玩具
                break;
            default:
                log.warn("无操作项PetsServiceImpl.foodOrToys,operation=[{}]", operation);
                return Result.error(ErrorCode.ERROR_OPERATION);
        }
        // 判断用户宠物物品数量
        Map<String, Object> userPetsProductInfo = petsProductBackpackMapper.getUserPetsProductInfo(userId, id, labelValue);
        int productNum = MapUtil.getInt(userPetsProductInfo, "productNum");// 物品数量
        int backpackId = MapUtil.getInt(userPetsProductInfo, "id");
        if (Objects.isNull(productNum) || productNum < 1) {
            return Result.error(ErrorCode.NUM_NOT_ENOUGH);
        }
        // 扣减物品
        int result = petsProductBackpackMapper.updateProductNum(backpackId, productNum);
        if (result < 1) {
            return Result.error(ErrorCode.EXCEPTION_CODE, "更新物品数量失败");
        }

        // 获取物品属性
        JSONObject msg = JSONObject.parseObject(MapUtil.getStr(userPetsProductInfo, "attribute"));

        Long uId;
        if (targetUserId != null) {
            uId = targetUserId; // 给好友喂食
        } else {
            uId = userId;
        }
        // 获取宠物
        PetsInfo petsInfo = petsInfoMapper.selectByPrimaryKey(null, uId, petCode);
        if (null == petsInfo.getNestId()) {
            return Result.error(ErrorCode.EXCEPTION_CODE, "宠物不在小窝内");
        }
        Map<String, Object> resultMap = new HashMap<>();
        switch (operation) {
            case 1: // 喂食
                int beFull = msg.getInteger("beFull");
                Double allSaturat = petsInfo.getAllSaturat(); // 总饱食
                Double currentSaturat = petsInfo.getCurrentSaturat();//当前饱食
                if ((beFull + currentSaturat) > allSaturat) {
                    currentSaturat = allSaturat;
                } else {
                    currentSaturat = currentSaturat + beFull;
                }
                petsInfo.setCurrentSaturat(currentSaturat);

                resultMap.put("type", "beFull");
                resultMap.put("intNum", beFull);
                break;
            case 2: // 玩具
                int mood = msg.getInteger("mood");
                Double allMoodNum = petsInfo.getAllMoodNum(); // 总心情
                Double currentMoodNum = petsInfo.getCurrentMoodNum();//当前心情
                if ((mood + currentMoodNum) > allMoodNum) {
                    currentMoodNum = allMoodNum;
                } else {
                    currentMoodNum = currentMoodNum + mood;
                }
                petsInfo.setCurrentMoodNum(currentMoodNum);

                resultMap.put("type", "mood");
                resultMap.put("intNum", mood);
                break;
            default:
                log.warn("无操作项PetsServiceImpl.foodOrToys,operation:[{}]", operation);
                return Result.error(ErrorCode.ERROR_OPERATION);
        }
        int i = petsInfoMapper.updateByPrimaryKeySelective(petsInfo);
        resultMap.put("status", i);
        resultMap.put("petsNick", petsInfo.getPetNick());
        return Result.success(resultMap);
    }

    @Override
    public Object getPetsInfoAndDressUp(Map<String, Object> params) {
        return petsInfoMapper.getPetsInfoAndDressUp(MapUtil.getLong(params, "userId"), MapUtil.getLong(params, "petsId"));
    }


}
