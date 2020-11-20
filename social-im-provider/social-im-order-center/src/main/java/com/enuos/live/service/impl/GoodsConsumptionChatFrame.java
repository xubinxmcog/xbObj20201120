package com.enuos.live.service.impl;

import com.enuos.live.constants.RedisKey;
import com.enuos.live.dto.ProductBackpackDetail;
import com.enuos.live.dto.UserFrameDTO;
import com.enuos.live.mapper.ProductBackpackMapper;
import com.enuos.live.pojo.ProductBackpack;
import com.enuos.live.result.Result;
import com.enuos.live.service.GoodsConsumption;
import com.enuos.live.service.ProductBackpackService;
import com.enuos.live.utils.RedisUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * @ClassName GoodsConsumptionChatFrame
 * @Description: TODO 用户聊天框使用
 * @Author xubin
 * @Date 2020/5/11
 * @Version V1.0
 **/
@Slf4j
@Component
public class GoodsConsumptionChatFrame implements GoodsConsumption {

    @Autowired
    private ProductBackpackMapper productBackpackMapper;

    @Autowired
    private RedisUtils redisUtils;

    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public Result consumption(ProductBackpackDetail productBackpackDetail, ProductBackpack productBackpack) {
        Integer categoryId = productBackpackDetail.getCategoryId();
        String redisKey = RedisKey.KEY_CHATFRAME + productBackpackDetail.getUserId() + "_" + categoryId;
        Long userId = productBackpackDetail.getUserId();
        log.info("用户聊天框操作=[{}],userId=[{}]", productBackpackDetail.getProductStatus(), userId);
        // 分类ID
        Long timeLimit = productBackpackDetail.getTimeLimit(); // 有效期 秒
        long surplusTime = -1;
        if (-1 != timeLimit) {
            long createTime = productBackpackDetail.getCreateTime().getTime() / 1000; // 购买时间
            long expirationTime = timeLimit + createTime + 1; // 到期时间 + 1秒
            long currentTime = System.currentTimeMillis() / 1000; // 当前时间
            surplusTime = expirationTime - currentTime;// 剩余时间

        }
        String chatFrame = null;
        Object font = null;
        if (productBackpackDetail.getProductStatus() == 1) {
            productBackpackDetail.setProductStatus(2);
            productBackpack.setProductStatus(2);
            redisUtils.set(redisKey, productBackpackDetail.getPicUrl(), surplusTime);
            chatFrame = productBackpackDetail.getPicUrl();
            font = productBackpackDetail.getAttribute4();
        } else if (productBackpackDetail.getProductStatus() == 2) {
            productBackpackDetail.setProductStatus(1);
            productBackpack.setProductStatus(1);
            redisUtils.del(redisKey);
            chatFrame = "";
            font = "null";
        }
        // 更新物品状态
        int i = productBackpackMapper.updateByPrimaryKeySelective(productBackpack);
        if (i > 0 && 2 == productBackpack.getProductStatus()) {
            productBackpackMapper.updateProductStatus(productBackpackDetail.getCategoryId(), productBackpackDetail.getId(), null, userId);
        }
        UserFrameDTO dto = new UserFrameDTO();
        dto.setChatFrame(chatFrame);
        dto.setChatFrameAttribute(font);
        dto.setCfTime(productBackpackDetail.getUseTime());
        dto.setUserId(userId);
        productBackpackMapper.updateUserFrame(dto);
        return Result.success(productBackpackDetail);

    }

    @Override
    public String getCode() {
        // 用户聊天框
        return "CHATFRAME";
    }
}
