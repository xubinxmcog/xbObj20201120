package com.enuos.live.service;

import com.enuos.live.feign.OrderFeign;
import com.enuos.live.mapper.UserAccountAttachPOMapper;
import com.enuos.live.mapper.UserCharmMapper;
import com.enuos.live.pojo.UserAccountAttachPO;
import com.enuos.live.pojo.UserCharm;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

/**
 * @ClassName TaskAsync
 * @Description: TODO 语音房异步处理类
 * @Author xubin
 * @Date 2020/6/11
 * @Version V1.0
 **/
@Slf4j
@Component
public class RoomTaskAsync {

    @Autowired
    private UserAccountAttachPOMapper accountAttachPOMapper;

    @Autowired
    private UserCharmMapper userCharmMapper;

    @Autowired
    private OrderFeign orderFeign;

    /**
     * @MethodName: upUserAttach
     * @Description: TODO
     * @Param: [userId: 抢红包人的ID, rp: 红包金额]
     * @Return: void
     * @Author: xubin
     * @Date:
     **/
    @Async
    public void upUserAttach(Long userId, Integer rp) {
        //查询用户金币
//        Map<String, Object> map = accountAttachPOMapper.getBalance(null, userId);
//        final int id = (Integer) map.get("id"); // 用户账户Id
//        final long gold = (Long) map.get("gold"); // 用户金币
//        final long surplusGold = gold + rp;
//        UserAccountAttachPO accountAttachPO = new UserAccountAttachPO();
//        accountAttachPO.setId(id);
//        accountAttachPO.setDiamond(surplusGold);
//        accountAttachPOMapper.upUserAccountAttachAmount(gold, userId);
        accountAttachPOMapper.upUserAccountAttachAmount(Long.valueOf(rp), userId);

    }

    // 保存送礼魅力值
    @Async
    public void getListGiftServiceImpl(UserCharm userCharm) {
        // 增加魅力表
        if (userCharmMapper.selectIsExistence(userCharm.getUserId(), userCharm.getGiveUserId(), userCharm.getGiftId()) < 1) { // 查询是否有同一人送同一礼物
            userCharmMapper.insert(userCharm); // 没有新增
        } else {
            // 有则累加
            userCharmMapper.updateGiftNum(userCharm);
        }

    }

    /**
     * @MethodName: entryBill
     * @Description: TODO 异步入账
     * @Param: [params]
     * @Return: void
     * @Author: xubin
     * @Date: 11:30 2020/6/18
    **/
    public void entryBill(@RequestBody Map<String, Object> params){
        orderFeign.entryBill(params);
    }

}
