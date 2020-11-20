package com.enuos.live.service.impl;

import com.enuos.live.pojo.PetsInfo;
import com.enuos.live.service.NestService;
import com.enuos.live.service.UpPetsHandleService;
import com.enuos.live.result.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * @ClassName UpPetsSexHandleServiceImpl
 * @Description: TODO 修改性别处理
 * @Author xubin
 * @Date 2020/10/12
 * @Version V2.0
 **/
@Component
public class UpPetsSexHandleServiceImpl implements UpPetsHandleService {


    @Autowired
    private NestService nestService;

    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public Result updatePetsHandle(PetsInfo petsInfo) {

        return nestService.upUserBackpack(petsInfo.getUserId(), petsInfo.getUpTarget(), "转化卡");
    }

    @Override
    public String getCode() {
        return "pc_shiftsex";
    }
}
