package com.enuos.live.mapper;

import com.enuos.live.dto.*;
import com.enuos.live.pojo.ProductBackpack;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface ProductBackpackMapper {

    int insert(ProductBackpack record);

    List<ProductBackpack> selectByPrimaryKey(ProductBackpack productBackpack);

    int updateByPrimaryKeySelective(ProductBackpack record);

//    List<ProductBackpackDTO> selectBackpackList(@Param("userId") Long userId, @Param("categoryId") Integer categoryId, @Param("productCode") String productCode);

    List<Map> getGameOrnament(@Param("userId") Long userId, @Param("categoryId") Integer categoryId);

    ProductBackpackDetail getProductBackpackDetail(@Param("id") Integer id, @Param("userId") Long userId);

    // 更新饰品使用状态
    int updateProductStatus(@Param("categoryId") Integer categoryId, @Param("id") Integer id, @Param("gameLabelId") Integer gameLabelId, @Param("userId") Long userId);

    // 更新用户头像框和聊天框
    int updateUserFrame(UserFrameDTO dto);

    // 更新过期饰品状态
    int updateUserTimeLimit(@Param("userId") Long userId);

    // 查询用户饰品
    List<CategoryBackpackDTO> getUserOrnaments(@Param("userId") Long userId);

    // 根据商品编码查询商品URL
    Map<String, Object> getProductUrl(@Param("productCode") String productCode);


}