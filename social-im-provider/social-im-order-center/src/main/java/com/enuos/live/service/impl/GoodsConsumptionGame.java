package com.enuos.live.service.impl;

import com.enuos.live.constants.RedisKey;
import com.enuos.live.dto.ProductBackpackDTO;
import com.enuos.live.dto.ProductBackpackDetail;
import com.enuos.live.mapper.ProductBackpackMapper;
import com.enuos.live.pojo.ProductBackpack;
import com.enuos.live.result.Result;
import com.enuos.live.service.GoodsConsumption;
import com.enuos.live.service.ProductBackpackService;
import com.enuos.live.utils.RedisUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * @ClassName GoodsConsumptionGame
 * @Description: TODO 游戏装饰消费
 * @Author xubin
 * @Date 2020/6/8
 * @Version V1.0
 **/
@Component
public class GoodsConsumptionGame implements GoodsConsumption {

    @Autowired
    private RedisUtils redisUtils;

    @Autowired
    private ProductBackpackMapper productBackpackMapper;

    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public Result consumption(ProductBackpackDetail productBackpackDetail, ProductBackpack productBackpack) {
        Long userId = productBackpackDetail.getUserId();
        Integer categoryId = productBackpackDetail.getCategoryId();
        Integer gameLabelId = productBackpackDetail.getGameLabelId();
        String redisKey = RedisKey.KEY_GAME + userId + "_" + categoryId + "_" + gameLabelId;

        // 分类ID
        Long timeLimit = productBackpackDetail.getTimeLimit(); // 有效期 秒
        long surplusTime = -1;
        if (-1 != timeLimit) {
            long createTime = productBackpackDetail.getCreateTime().getTime() / 1000; // 购买时间
            long expirationTime = timeLimit + createTime + 1; // 到期时间 + 1秒
            long currentTime = System.currentTimeMillis() / 1000; // 当前时间
            surplusTime = expirationTime - currentTime;// 剩余时间

        }
        if (productBackpackDetail.getProductStatus() == 1) {
            productBackpack.setProductStatus(2);
            productBackpackDetail.setProductStatus(2);
            redisUtils.set(redisKey, productBackpackDetail.getPicUrl(), surplusTime);
        } else if (productBackpackDetail.getProductStatus() == 2) {
            productBackpackDetail.setProductStatus(1);
            productBackpack.setProductStatus(1);
            redisUtils.del(redisKey);
        }
        // 更新物品状态
        int i = productBackpackMapper.updateByPrimaryKeySelective(productBackpack);
        if (i > 0 && 2 == productBackpack.getProductStatus()) {
            productBackpackMapper.updateProductStatus(productBackpackDetail.getCategoryId(), productBackpack.getId(), gameLabelId, userId);
        }
        return Result.success(productBackpackDetail);
    }

    @Override
    public String getCode() {
        // 游戏
        return "GAME";
    }
}
