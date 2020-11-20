package com.enuos.live.service.impl;

import cn.hutool.core.map.MapUtil;
import com.enuos.live.mapper.PetsInfoMapper;
import com.enuos.live.mapper.PetsProductBackpackMapper;
import com.enuos.live.mapper.PetsUserDynamicMsgMapper;
import com.enuos.live.dto.PetsDynamicDTO;
import com.enuos.live.pojo.PetsUserDynamicMsg;
import com.enuos.live.service.PetsDynamicService;
import com.enuos.live.service.PetsService;
import com.enuos.live.result.Result;
import com.enuos.live.utils.page.PageInfo;
import com.github.pagehelper.PageHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @ClassName PetsDynamicServiceImpl
 * @Description: TODO
 * @Author xubin
 * @Date 2020/10/20
 * @Version V2.0
 **/
@Slf4j
@Service
public class PetsDynamicServiceImpl implements PetsDynamicService {

    @Autowired
    private PetsUserDynamicMsgMapper petsUserDynamicMsgMapper;

    @Autowired
    private PetsProductBackpackMapper petsProductBackpackMapper;

    @Autowired
    private PetsInfoMapper petsInfoMapper;

    @Autowired
    private PetsService petsService;

    @Override
    public Result get(PetsDynamicDTO dto) {
        log.info("获取动态入参=[{}]", dto);

        PageHelper.startPage(dto.getPageNum(), dto.getPageSize());
        List<PetsUserDynamicMsg> petsUserDynamicMsgs = petsUserDynamicMsgMapper.get(dto.getUserId(), dto.getIsRead());
        petsUserDynamicMsgs.forEach(pd -> pd.setMessage(pd.getGfNickName() + pd.getMessage()));
        return Result.success(new PageInfo<>(petsUserDynamicMsgs));
    }

    @Override
    public Result sign(PetsDynamicDTO dto) {
        log.info("标记动态入参,dto=[{}]", dto);
        int i = petsUserDynamicMsgMapper.upIsReadStatus(dto.getIds());
        return Result.success(i);
    }

    @Override
    public Result goodFriends(Long userId) {
        log.info("获取好友列表入参,userId=[{}]", userId);
        List<Map<String, Object>> friendList = petsUserDynamicMsgMapper.getFriendList(userId);
        int friendNum = friendList.size();
        Map<String, Object> resultMap = new HashMap<String, Object>() {
            {
                put("friendList", friendList);
                put("friendNum", friendNum);
            }
        };

        return Result.success(resultMap);
    }

    @Override
    public Result feed(Map<String, Object> params) {
        log.info("帮好友喂食入参,params=[{}]", params);

        Long userId = MapUtil.getLong(params, "userId");
        Long targetUserId = MapUtil.getLong(params, "targetUserId");
        String id = MapUtil.getStr(params, "id"); // 物品id
        String petCode = MapUtil.getStr(params, "petCode"); // 宠物code
        if (userId == null || targetUserId == null || id == null || petCode == null) {
            return Result.error(201, "缺少必填参数");
        }

        return petsService.foodOrToys(userId, targetUserId, petCode, id, 1);
    }

    @Override
    public Result toys(Map<String, Object> params) {
        log.info("帮好友互动入参,params=[{}]", params);

        Long userId = MapUtil.getLong(params, "userId");
        Long targetUserId = MapUtil.getLong(params, "targetUserId");
        String id = MapUtil.getStr(params, "id"); // 物品id
        String petCode = MapUtil.getStr(params, "petCode"); // 宠物code

        if (userId == null || targetUserId == null || id == null || petCode == null) {
            return Result.error(201, "缺少必填参数");
        }

        return petsService.foodOrToys(userId, targetUserId, petCode, id, 2);
    }

    @Override
    public Result hello(Map<String, Object> params) {
        log.info("和好友打招呼入参,params=[{}]", params);

        Long userId = MapUtil.getLong(params, "userId");
        Long targetUserId = MapUtil.getLong(params, "targetUserId");

        if (userId == null || targetUserId == null) {
            return Result.error(201, "缺少必填参数");
        }
        return Result.success();
    }

    public static void main(String[] args) {
        List<Byte[]> list = new ArrayList<>();
        int i = 0;
        try {
            while (true) {
                list.add(new Byte[1024 * 1024]);
                i++;
            }
        } catch (Throwable e) {
            e.printStackTrace();
            System.out.println("执行了" + i + "次");
        }
    }
}
