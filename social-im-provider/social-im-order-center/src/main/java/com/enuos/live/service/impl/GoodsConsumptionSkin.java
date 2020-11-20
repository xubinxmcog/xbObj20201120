package com.enuos.live.service.impl;

import com.enuos.live.constants.RedisKey;
import com.enuos.live.dto.ProductBackpackDetail;
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
 * @ClassName SkinGoodsConsumption
 * @Description: TODO 用户皮肤消费
 * @Author xubin
 * @Date 2020/5/11
 * @Version V1.0
 **/
@Slf4j
@Component
public class GoodsConsumptionSkin implements GoodsConsumption {

    @Autowired
    private ProductBackpackService productBackpackService;

    @Autowired
    private ProductBackpackMapper productBackpackMapper;

    @Autowired
    private RedisUtils redisUtils;

    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public Result consumption(ProductBackpackDetail productBackpackDetail, ProductBackpack productBackpack) {
        Integer categoryId = productBackpackDetail.getCategoryId();
        String redisKey = RedisKey.KEY_SKIN + productBackpackDetail.getUserId() + "_" + categoryId;

        Long userId = productBackpackDetail.getUserId();
        log.info("用户皮肤操作=[{}],userId=[{}]", productBackpackDetail.getProductStatus(), userId);
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
            productBackpackDetail.setProductStatus(2);
            productBackpack.setProductStatus(2);
            redisUtils.set(redisKey, productBackpackDetail.getPicUrl(), surplusTime);
        } else if (productBackpackDetail.getProductStatus() == 2) {
            productBackpackDetail.setProductStatus(1);
            productBackpack.setProductStatus(1);
            redisUtils.del(redisKey);
        }
        // 更新物品状态
        int i = productBackpackMapper.updateByPrimaryKeySelective(productBackpack);
        if (i > 0 && 2 == productBackpack.getProductStatus()) {
            productBackpackMapper.updateProductStatus(productBackpackDetail.getCategoryId(), productBackpackDetail.getId(), null, userId);
        }
        return Result.success(productBackpackDetail);

    }

    @Override
    public String getCode() {
        // 皮肤
        return "SKIN";
    }
}
