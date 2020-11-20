package com.enuos.live.service.impl;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ObjectUtil;
import com.enuos.live.error.ErrorCode;
import com.enuos.live.mapper.PetsInfoMapper;
import com.enuos.live.mapper.PetsNestMapper;
import com.enuos.live.mapper.PetsProductBackpackMapper;
import com.enuos.live.pojo.Currency;
import com.enuos.live.pojo.PetsInfo;
import com.enuos.live.pojo.PetsNestConfig;
import com.enuos.live.pojo.PetsNestUnlock;
import com.enuos.live.rest.OrderFeign;
import com.enuos.live.rest.UserRemote;
import com.enuos.live.result.Result;
import com.enuos.live.service.NestService;
import com.enuos.live.service.UpPetsHandleService;
import com.enuos.live.service.factory.UpPetsHandleFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @ClassName CaveolaeServiceImpl
 * @Description: TODO 小窝
 * @Author xubin
 * @Date 2020/10/9
 * @Version V2.0
 **/
@Slf4j
@Service
public class NestServiceImpl implements NestService {

    @Autowired
    private PetsInfoMapper petsInfoMapper;

    @Autowired
    private PetsProductBackpackMapper petsProductBackpackMapper;

    @Autowired
    private PetsNestMapper petsNestMapper;

    @Autowired
    private UserRemote userRemote;

    @Autowired
    private OrderFeign orderFeign;

    /**
     * @MethodName: me
     * @Description: TODO 我的小窝
     * @Param: [userId]
     * @Return: com.enuos.live.result.Result
     * @Author: xubin
     * @Date: 17:51 2020/10/9
     **/
    @Override
    public Result me(Long userId) {
        log.info("我的小窝入参, userId=[{}]", userId);
        return Result.success(petsInfoMapper.selectUserNestPetss(userId));
    }

    /**
     * @MethodName: list
     * @Description: TODO 我的宠物列表
     * @Param: [userId]
     * @Return: com.enuos.live.result.Result
     * @Author: xubin
     * @Date: 15:43 2020/10/10
     **/
    @Override
    public Result list(Long userId) {
        log.info("我的宠物列表入参, userId=[{}]", userId);
        return Result.success(petsInfoMapper.selectAllUserPetss(userId));
    }

    /**
     * @MethodName: upPetsInfo
     * @Description: TODO 修改宠物信息
     * @Param: [petsInfo]
     * @Return: com.enuos.live.result.Result
     * @Author: xubin
     * @Date: 10:05 2020/10/12
     **/
    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public Result upPetsInfo(PetsInfo petsInfo) {
        log.info("修改宠物信息入参,PetsInfo=[{}]", petsInfo);
        if (Objects.isNull(petsInfo.getId()) ||
                Objects.isNull(petsInfo.getUserId()) ||
                Objects.isNull(petsInfo.getUpTarget())) {
            return Result.error(ErrorCode.EXCEPTION_CODE, "必填参数不可为空");
        }

        UpPetsHandleService upPetsHandleService = UpPetsHandleFactory.getUpPetsHandleSevice(petsInfo.getUpTarget());
        if (ObjectUtil.isNotEmpty(upPetsHandleService)) {
            Result result = upPetsHandleService.updatePetsHandle(petsInfo);
            if (result.getCode() != 0) {
                return result;
            }
        }

        int i = petsInfoMapper.updateByPrimaryKeySelective(petsInfo);
        if (i != 1) {

            return Result.error(ErrorCode.EXCEPTION_CODE, "更新失败");
        }
        return Result.success();
    }

    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public Result upUserBackpack(Long userId, String upTarget, String tips) {
        // 查询用户改名卡
        Map<String, Object> productCodeBackpack = petsProductBackpackMapper.getProductCodeBackpack(userId, upTarget);

        // 判断没有转化卡直接返回
        if (ObjectUtil.isEmpty(productCodeBackpack)) {
            return Result.error(6002, "没有" + tips + "，请先获取");
        }
        // 转化卡数量
        Integer productNum = MapUtil.getInt(productCodeBackpack, "productNum");
        if (Objects.isNull(productNum) || productNum < 1) {
            return Result.error(6002, tips + "数量不足，请先获取");
        }

        // 扣除转化卡, 返回扣除成功,走修改逻辑
        int id = petsProductBackpackMapper.updateProductNum(MapUtil.getInt(productCodeBackpack, "id"), productNum);
        if (id == 1) {
            return Result.success();
        }
        return Result.error(ErrorCode.DATA_ERROR);
    }

    /**
     * @MethodName: unlock
     * @Description: TODO 解锁小窝
     * @Param: [params]
     * @Return: com.enuos.live.result.Result
     * @Author: xubin
     * @Date: 13:23 2020/10/13
     **/
    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public Result unlock(Map<String, Object> params) {
        log.info("解锁小窝入参,params=[{}]", params);
        Integer nestId = MapUtil.getInt(params, "nestId");// 小窝编码
        Long userId = MapUtil.getLong(params, "userId");// 用户ID
        if (null == nestId || null == userId) {
            return Result.error(ErrorCode.EXCEPTION_CODE, "必填参数不可为空");

        }
        // 判断小窝是否解锁
        Integer isNestUnlock = petsNestMapper.getIsNestUnlock(userId, nestId);
        if (null != isNestUnlock) {
            return Result.error(60, "该小窝已经解锁");
        }

        PetsNestConfig petsNestConfig = petsNestMapper.selectByIdNestConfig(nestId);
        if (null == petsNestConfig) {
            return Result.error(ErrorCode.EXCEPTION_CODE, "暂不可解锁");
        }

        Integer lockType = petsNestConfig.getLockType(); // 解锁类型 1:等级解锁 2:钻石解锁 3:金币解锁 4:会员解锁

        Integer lockValue = petsNestConfig.getLockValue(); // 解锁值

        int isUnlock = 0;
        String errorMsg = "解锁失败";
        Currency currency = new Currency();
        currency.setUserId(userId);

        Map<String, Object> billMap = new HashMap(); // 入账
        billMap.put("price", -lockValue);
        billMap.put("productName", "解锁小窝");
        billMap.put("status", 1);
        billMap.put("userId", userId);
        switch (lockType) {
            case 1:
                // 查询用户等级
                Map<String, Object> userMsg = userRemote.getUserMsg(userId);
                if (ObjectUtil.isEmpty(userMsg)) {
                    log.warn("未查询到用户信息,NestServiceImpl.unlock");
                    return Result.error(ErrorCode.EXCEPTION_CODE, "未查询到用户信息");
                }
                Integer level = MapUtil.getInt(userMsg, "level");
                // 判断等级达到后可解锁
                if (lockValue <= level) {
                    isUnlock = 1;
                } else {
                    errorMsg = "达到" + lockValue + "级后方可解锁";
                }
                break;
            case 2:
                // 查询用户钻石
                Result diamondResult = userRemote.getCurrency(userId);
                Map<String, Object> diamondMap = diamondResult.getCode().equals(0) ? (Map<String, Object>) diamondResult.getData() : null;
                if (ObjectUtil.isEmpty(diamondMap)) {
                    log.error("1040, 未查询到用户金币和钻石数据userId={}", userId);
                    return Result.error(ErrorCode.CONTENT_EMPTY);
                }
                final long diamond = MapUtil.getLong(diamondMap, "diamond"); // 用户钻石
                if (diamond < lockValue) {
                    log.info("用户钻石不足, userId=[{}]", userId);
                    return Result.error(ErrorCode.NOT_ENOUGH_DIAMOND);
                }
                currency.setOriginalDiamond(diamond);
                currency.setDiamond(diamond - Long.valueOf(lockValue)); // 2钻石 3:金币
                billMap.put("priceType", 2); // 2钻石 3:金币
                Result result1 = userRemote.upUserCurrency(currency);
                if ((result1.getCode() != 0)) {
                    log.warn("解锁小窝扣款失败, userId=[{}]", userId);
                    throw new DataIntegrityViolationException("数据异常");
                } else {
                    orderFeign.entryBill(billMap);
                    isUnlock = 1;
                }
                new Thread(() -> {
                    userRemote.addGrowth(userId, Integer.parseInt(String.valueOf(lockValue)));
                }).start();
                break;
            case 3:
                // 查询用户金币
                Result result = userRemote.getCurrency(userId);
                Map<String, Object> goldMap = result.getCode().equals(0) ? (Map<String, Object>) result.getData() : null;
                if (ObjectUtil.isEmpty(goldMap)) {
                    log.error("1040, 未查询到用户金币和钻石数据userId={}", userId);
                    return Result.error(ErrorCode.CONTENT_EMPTY);
                }
                final long gold = MapUtil.getLong(goldMap, "gold"); // 用户金币
                if (gold < lockValue) {
                    log.info("用户金币不足, userId=[{}]", userId);
                    return Result.error(ErrorCode.NOT_ENOUGH_GOLD);
                }
                currency.setOriginalGold(gold);
                currency.setGold(gold - Long.valueOf(lockValue));
                billMap.put("priceType", 3); // 2钻石 3:金币
                Result result2 = userRemote.upUserCurrency(currency);
                if ((result2.getCode() != 0)) {
                    log.warn("解锁小窝扣款失败, userId=[{}]", userId);
                    throw new DataIntegrityViolationException("数据异常");
                } else {
                    orderFeign.entryBill(billMap);
                    isUnlock = 1;
                }
                break;
            case 4:
                // 查询用户是否会员
                Map<String, Object> map = userRemote.getUserMsg(userId);
                if (ObjectUtil.isEmpty(map)) {
                    log.warn("未查询到用户信息,NestServiceImpl.unlock");
                    return Result.error(ErrorCode.EXCEPTION_CODE, "未查询到用户信息");
                }
                Integer isMember = MapUtil.getInt(map, "isMember");
                if (1 == isMember) {
                    isUnlock = 1;
                } else {
                    errorMsg = "成为会员后方可解锁";
                }
                break;
        }

        // 解锁用户小窝
        if (isUnlock == 1) {
            PetsNestUnlock record = new PetsNestUnlock();
            record.setUserId(userId);
            record.setNestId(nestId);
            record.setPetsId(null);
            record.setIsUnlock(1);
            petsNestMapper.insertNestUnlock(record);

            return Result.success();
        } else {
            log.info(errorMsg);
            return Result.error(ErrorCode.EXCEPTION_CODE, errorMsg);
        }
    }

    /**
     * @MethodName: upDown
     * @Description: TODO 宠物上下窝
     * @Param: [params]
     * @Return: com.enuos.live.result.Result
     * @Author: xubin
     * @Date: 16:10 2020/10/13
     **/
    @Override
    public Result upDown(Map<String, Object> params) {
        log.info("宠物上下窝入参,params=[{}]", params);
        Long userId = MapUtil.getLong(params, "userId");
        Integer oper = MapUtil.getInt(params, "oper"); // 1:上窝 2:下窝
        if (null == userId || null == oper) {
            log.error("用户ID或oper不可为空,NestServiceImpl.upDown");
            return Result.error(ErrorCode.EXCEPTION_CODE, "必填参数不可为空");
        }
        Integer nestId = MapUtil.getInt(params, "nestId"); // 小窝ID
        Long petsId = MapUtil.getLong(params, "petsId"); // 宠物ID

        switch (oper) {
            case 1:
                // petsId不为null为上窝操作
                if (null != petsId) {
                    // 判断用户是否有这个宠物
                    Integer isPets = petsInfoMapper.getIsPets(petsId, userId);
                    if (null == isPets) {
                        return Result.error(60, "未查询到的宠物");
                    }
                    // 判断该宠物是否在窝内
                    Integer isNestPets = petsNestMapper.getIsNestPets(userId, petsId);
                    if (null != isNestPets) {
                        return Result.error(60, "宠物已在小窝内");
                    }
                    // 获取用户的一个空窝
                    PetsNestUnlock userNullNest = petsNestMapper.getUserNullNest(userId);
                    if (ObjectUtil.isEmpty(userNullNest)) {
                        log.warn("未查到用户的空窝");
                        return Result.error(60, "没有空窝");
                    }
                    userNullNest.setPetsId(petsId);
                    petsNestMapper.updatePetsNestUnlock(userNullNest);
                    return Result.success();
                }
                break;
            case 2:
                // nestId不为null为下窝操作
                if (null != nestId) {
                    petsNestMapper.updateCleanOneNest(userId, nestId);
                    return Result.success();
                }
                break;
            default:
                log.error("操作异常,petsId和nestId不可同时为空NestServiceImpl.upDown");
                break;
        }
        return Result.error(ErrorCode.ERROR_OPERATION);
    }

    /**
     * @MethodName: pieces
     * @Description: TODO 获取用户宠物碎片列表
     * @Param: [userId]
     * @Return: com.enuos.live.result.Result
     * @Author: xubin
     * @Date: 11:05 2020/10/14
     **/
    @Override
    public Result pieces(Long userId) {
        log.info("获取用户宠物碎片列表入参,userId=[{}]", userId);
        return Result.success(petsProductBackpackMapper.getUserPieces(userId));
    }

    /**
     * @MethodName: petsExchange
     * @Description: TODO 宠物兑换
     * @Param: [params]
     * @Return: com.enuos.live.result.Result
     * @Author: xubin
     * @Date: 11:17 2020/10/14
     **/
    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public Result petsExchange(Map<String, Object> params) {
        log.info("宠物兑换入参,params=[{}]", params);
        Long userId = MapUtil.getLong(params, "userId");
        String pieceCode = MapUtil.getStr(params, "pieceCode"); // 碎片code
        if (null == userId || null == pieceCode) {
            return Result.error(ErrorCode.EXCEPTION_CODE, "必填参数不可为空");
        }
        Map<String, Object> userPieceMap = petsProductBackpackMapper.getUserPiece(userId, pieceCode);

        // 判断用户碎片是否够兑换
        if (ObjectUtil.isEmpty(userPieceMap)) {
            return Result.error(60, "未查询到碎片信息");
        }
        Integer isExchange = MapUtil.getInt(userPieceMap, "isExchange");
        if (0 == isExchange) {
            return Result.error(60, "碎片不足");
        }
        Long id = MapUtil.getLong(userPieceMap, "id");
        Integer uPieceNum = MapUtil.getInt(userPieceMap, "uPieceNum");
        Integer needPieceNum = MapUtil.getInt(userPieceMap, "needPieceNum");

        // 扣除碎片
        Integer currentNum = uPieceNum - needPieceNum;
        int result = petsProductBackpackMapper.updateUserBackpackPiece(currentNum, id, userId, uPieceNum);
        if (result == 1) {
            // 添加宠物到用户
            PetsInfo record = new PetsInfo();
            record.setUserId(userId);
            record.setPetCode(MapUtil.getStr(userPieceMap, "petsCode"));
            record.setPetNick(MapUtil.getStr(userPieceMap, "petsName"));
            record.setPetSex(MapUtil.getInt(userPieceMap, "initSex"));
            record.setPetLevel(1);
            record.setAllMoodNum(1200.00);
            record.setCurrentMoodNum(0.0);
            record.setAllSaturat(1200.00);
            record.setCurrentSaturat(0.0);
            record.setIsStatus(1);
            petsInfoMapper.insert(record);
            return Result.success();
        }
        return Result.error(ErrorCode.ERROR_OPERATION);
    }
}
