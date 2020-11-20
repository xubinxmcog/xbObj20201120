package com.enuos.live.mapper;

import com.enuos.live.dto.PetsBackpackDTO;
import com.enuos.live.pojo.PetsProductBackpack;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface PetsProductBackpackMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(PetsProductBackpack record);

    PetsProductBackpack selectByPrimaryKey(PetsProductBackpack backpack);

    int updateByPrimaryKeySelective(PetsProductBackpack record);

    // 更新宠物数量
    int updateProductNum(@Param("id") Integer id, @Param("productNum") Integer productNum);

    int upProductStatus(@Param("id") Long id, @Param("productStatus") Integer productStatus);

    int upByCategoryProductStatus(@Param("id") Long id, @Param("userId") Long userId, @Param("categoryId") Integer categoryId);

    // 更新过期饰品状态
    int updateUserTimeLimit(@Param("userId") Long userId);

    // 按类别获取用户宠物物品
    List<Map<String, Object>> getUserCategory(@Param("userId") Long userId, @Param("categoryId") Integer categoryId);

    Map<String, Object> getUserPetsProductInfo(@Param("userId") Long userId, @Param("labelCode") String labelCode, @Param("label") String label);

    // 获取用户宠物背包所有物品
    List<PetsBackpackDTO> getUserPetsBackpack(@Param("userId") Long userId, @Param("categoryId") Integer categoryId);

    // 根据物品code获取物品
    Map<String, Object> getProductCodeBackpack(@Param("userId") Long userId, @Param("productCode") String productCode);

    List<Map<String, Object>> getUserPieces(@Param("userId") Long userId);

    Map<String, Object> getUserPiece(@Param("userId") Long userId, @Param("pieceCode") String pieceCode);

    int updateUserBackpackPiece(@Param("currentNum") Integer currentNum, @Param("id") Long id,
                                @Param("userId") Long userId, @Param("productNum") Integer productNum);


}