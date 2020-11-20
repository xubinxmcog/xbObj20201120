package com.enuos.live.service.impl;

import cn.hutool.core.map.MapUtil;
import com.enuos.live.mapper.PetsInfoMapper;
import com.enuos.live.pojo.PetsInfo;
import com.enuos.live.service.NestService;
import com.enuos.live.service.UpPetsHandleService;
import com.enuos.live.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

/**
 * @ClassName UpPetsNameHandleSeviceImpl
 * @Description: TODO
 * @Author xubin
 * @Date 2020/10/12
 * @Version V2.0
 **/
@Slf4j
@Component
public class UpPetsNameHandleServiceImpl implements UpPetsHandleService {

    @Autowired
    private NestService nestService;

    @Autowired
    private PetsInfoMapper petsInfoMapper;

    /**
     * @MethodName: updatePetsHandle
     * @Description: TODO 修改名字处理
     * @Param: [petsInfo]
     * @Return: java.lang.Object
     * @Author: xubin
     * @Date: 14:55 2020/10/12
     **/
    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public Result updatePetsHandle(PetsInfo petsInfo) {
        Map<String, Object> petCodeName = petsInfoMapper.getPetIdName(petsInfo.getId());
        String petNick = MapUtil.getStr(petCodeName, "petNick");
        String petsName = MapUtil.getStr(petCodeName, "petsName");

        String petNick1 = petsInfo.getPetNick();
        if (petNick1.equals(petNick)) {
            return Result.error(6003, "新宠物名不能和原名一致");
        }

        // 和原始名字一致可以直接修改
        if (petNick.equals(petsName)) {
            return Result.success();
        }


        return nestService.upUserBackpack(petsInfo.getUserId(), petsInfo.getUpTarget(), "改名卡");
    }

    @Override
    public String getCode() {
        return "pc_rename";
    }
}
