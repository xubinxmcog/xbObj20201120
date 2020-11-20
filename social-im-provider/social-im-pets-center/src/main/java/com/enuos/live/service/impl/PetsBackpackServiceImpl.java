package com.enuos.live.service.impl;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ObjectUtil;
import com.enuos.live.dto.PetsBackpackDTO;
import com.enuos.live.dto.PetsDressUpDTO;
import com.enuos.live.dto.ProductBackpackDTO;
import com.enuos.live.error.ErrorCode;
import com.enuos.live.mapper.PetsDressUpMapper;
import com.enuos.live.mapper.PetsProductBackpackMapper;
import com.enuos.live.mapper.PetsProductCategoryMapper;
import com.enuos.live.pojo.PetsDressUp;
import com.enuos.live.pojo.PetsProductBackpack;
import com.enuos.live.result.Result;
import com.enuos.live.service.PetsBackpackService;
import com.enuos.live.server.handler.PetsHandler;
import com.enuos.live.util.TimeUtil;
import com.enuos.live.utils.DateUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @ClassName PetsBackpackServiceImpl
 * @Description: TODO
 * @Author xubin
 * @Date 2020/9/1
 * @Version V2.0
 **/
@Slf4j
@Service
public class PetsBackpackServiceImpl implements PetsBackpackService {

    @Autowired
    private PetsProductBackpackMapper petsProductBackpackMapper;

    @Autowired
    private PetsProductCategoryMapper petsProductCategoryMapper;

    @Autowired
    private PetsDressUpMapper petsDressUpMapper;

    @Autowired
    private PetsHandler petsHandler;

    /**
     * @MethodName: upProductBackpack
     * @Description: TODO 物品入宠物背包
     * @Param: [backpack, usingPro类别使用属性 1:消耗品 2:装饰品]
     * @Return: com.enuos.live.result.Result
     * @Author: xubin
     * @Date: 17:45 2020/9/1
     **/
    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public Result upProductBackpack(PetsProductBackpack backpack, Integer usingPro) {
        log.info("宠物背包入参信息：【{}】, usingPro=[{}]", backpack.toString(), usingPro);
        PetsProductBackpack petsBackpack = petsProductBackpackMapper.selectByPrimaryKey(backpack);
        // 背包没有相同物品则新增
        if (ObjectUtils.isEmpty(petsBackpack)) {

            if (2 == usingPro) {
                Date date = new Date();
                if (backpack.getTimeLimit() != -1) {
                    long milliSecond = System.currentTimeMillis() + backpack.getTimeLimit() * 1000;
                    date.setTime(milliSecond);
                } else {
                    date.setTime(System.currentTimeMillis() + 3162240000000L); // 100年
                }
                backpack.setProductStatus(1);
                backpack.setUseTime(date);
            } else {
                backpack.setProductStatus(0);
            }
            int i = petsProductBackpackMapper.insert(backpack);
            if (i == 1) {
                log.info("背包添加成功id={}", backpack.getId());
                return Result.success(backpack.getId());
            } else {
                log.error("背包添加失败:{}", backpack.toString());
                return Result.error(ErrorCode.ERROR_OPERATION);
            }
        } else {

            if (2 == usingPro) {
                Long currentTimeLimit = petsBackpack.getTimeLimit();// 背包中现有物品有效期时间
                if (-1 == currentTimeLimit) {
                    // 有相同永久物品，不可再次购买
                    return Result.error(200, "有相同永久物品不可再次购买");
                } else {
                    Long newProductTimeLimit = backpack.getTimeLimit(); // 用户购买时长
                    Date date = new Date();
                    if (-1 == newProductTimeLimit) { // -1:永久
                        petsBackpack.setTimeLimit(newProductTimeLimit);
                        date.setTime(System.currentTimeMillis() + 3153600000000L); // 100年
                    } else {
                        Long totalTimeLimit = currentTimeLimit + newProductTimeLimit;
                        petsBackpack.setTimeLimit(totalTimeLimit);
                        date.setTime(System.currentTimeMillis() + totalTimeLimit * 1000);
                    }
                    if (currentTimeLimit == 0) {
                        petsBackpack.setCreateTime(new Date());
                    } else {
                        petsBackpack.setCreateTime(null);
                    }
                    petsBackpack.setUseTime(date);
                }
            } else {
                petsBackpack.setProductNum(petsBackpack.getProductNum() + backpack.getProductNum());
            }
            petsBackpack.setCategoryId(null);
            if (petsProductBackpackMapper.updateByPrimaryKeySelective(petsBackpack) > 0) {
                log.info("背包修改成功id={}", petsBackpack.getId());
                return Result.success();
            } else {
                log.error("背包修改失败：{}", petsBackpack.toString());
                return Result.error(ErrorCode.ERROR_OPERATION);
            }
        }
    }

    @Override
    public Result queryBackpack(Long userId, Integer categoryId) {
        log.info("获取用户宠物背包所有物品入参, userId=[{}], categoryId=[{}]", userId, categoryId);
        petsProductBackpackMapper.updateUserTimeLimit(userId);
        List<PetsBackpackDTO> userPetsBackpacks = petsProductBackpackMapper.getUserPetsBackpack(userId, categoryId);
        if (ObjectUtil.isEmpty(userPetsBackpacks)) {
            return Result.success();
        }

        if (ObjectUtil.isNotEmpty(userPetsBackpacks)) {
            for (PetsBackpackDTO userPetsBackpack : userPetsBackpacks) {
                Integer parentId = userPetsBackpack.getParentId();
                List<ProductBackpackDTO> backpackDTOs = userPetsBackpack.getList();
                for (ProductBackpackDTO backpackDTO : backpackDTOs) {
                    long timeLimit = backpackDTO.getTimeLimit();//有效时长
                    if (-1 == timeLimit) {
                        backpackDTO.setTermDescribe("永久");
                    } else {
                        long createTime = backpackDTO.getCreateTime().getTime() / 1000; // 购买时间
                        long expirationTime = timeLimit + createTime; // 到期时间
                        long currentTime = System.currentTimeMillis() / 1000; // 当前时间
                        long surplusTime = expirationTime - currentTime;// 剩余时间
                        String expire = DateUtils.getExpire(surplusTime);
                        backpackDTO.setTermDescribe(expire + "后到期");
                    }
                    Integer categoryId1 = backpackDTO.getCategoryId();
                    if (parentId == categoryId1) {

                    }
                }
                userPetsBackpack.setList(backpackDTOs);
                if (userPetsBackpack.getParentId() == userPetsBackpack.getCategoryId()) {
                    List<PetsBackpackDTO> children = userPetsBackpack.getChildren();
                    children.add(userPetsBackpack);
                    userPetsBackpack.setChildren(children);
                }
            }
        }

        return Result.success(userPetsBackpacks);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public Result dressUp(Map<String, Object> params) {
        log.info("修改宠物装扮入参=[{}]", params);
        Long userId = MapUtil.getLong(params, "userId");
        Long petsId = MapUtil.getLong(params, "petsId"); // 宠物ID
        Long backpackId = MapUtil.getLong(params, "backpackId"); // 背包物品ID
        if (null == userId || null == petsId || null == backpackId) {
            return Result.error(ErrorCode.EXCEPTION_CODE, "必填参数不可为空");
        }

        // 判断该物品是否为装扮物品
        Integer productParentId = petsProductCategoryMapper.getProductParentId(backpackId);
        if (null == productParentId || productParentId != 1) {
            return Result.error(ErrorCode.EXCEPTION_CODE, "不可装扮物品");
        }

        // 判断该装扮是否过期
        PetsProductBackpack backpack = new PetsProductBackpack();
        backpack.setId(backpackId);
        backpack = petsProductBackpackMapper.selectByPrimaryKey(backpack);
        Date date = new Date();
        int compareTo = backpack.getUseTime().compareTo(date);
        if (compareTo == -1) {
            return Result.error(ErrorCode.EXCEPTION_CODE, "装扮已过期");
        }

        // 修改背包装扮的使用状态
        int status = 2; // 1：未使用 2：使用中
        if (backpack.getProductStatus() == 2) {
            status = 1;
        }
        int i = petsProductBackpackMapper.upProductStatus(backpackId, status);
        if (1 == i && status == 2) {
            petsProductBackpackMapper.upByCategoryProductStatus(backpackId, userId, backpack.getCategoryId());
        }

        // 绑定或解绑物品到用户宠物
        Long isDressUp = petsDressUpMapper.getIsDressUp(userId, petsId, backpackId);
        if (null == isDressUp && status == 2) {
            PetsDressUp petsDressUp = new PetsDressUp();
            petsDressUp.setUserId(userId);
            petsDressUp.setPetsId(petsId);
            petsDressUp.setBackpackId(backpackId);
            petsDressUpMapper.insert(petsDressUp);
        } else if (null != isDressUp && status == 2) {
            PetsDressUp petsDressUp = new PetsDressUp();
            petsDressUp.setId(isDressUp);
            petsDressUp.setUserId(userId);
            petsDressUp.setPetsId(petsId);
            petsDressUp.setBackpackId(backpackId);
            petsDressUpMapper.updateByPrimaryKeySelective(petsDressUp);
        } else if (null != isDressUp && status == 1) {
            petsDressUpMapper.deleteByPrimaryKey(isDressUp);
        }

        return Result.success();
    }

    @Override
    public Result getDressUp(Map<String, Object> params) {
        log.info("获取宠物装扮入参=[{}]", params);
        Long userId = MapUtil.getLong(params, "userId");
        Long petsId = MapUtil.getLong(params, "petsId"); // 宠物ID
        if (null == userId || null == petsId) {
            return Result.error(ErrorCode.EXCEPTION_CODE, "必填参数不可为空");
        }
        List<PetsDressUpDTO> dressUp = petsDressUpMapper.getDressUp(userId, petsId);

        if (ObjectUtil.isNotEmpty(dressUp)) {
            dressUp.forEach(objectMap -> {
                if (objectMap.getTimeLimit() == -1) {
                    objectMap.setTermDescribe("永久");
                } else {
                    objectMap.setTermDescribe(TimeUtil.getExpire((objectMap.getUseTime().getTime() / 1000) - (System.currentTimeMillis() / 1000)) + "后到期");
                }
                petsHandler.petsDressUpQualityNON_NULL(objectMap.getEffectQuality());
            });
        }

        return Result.success(dressUp);
    }

}
