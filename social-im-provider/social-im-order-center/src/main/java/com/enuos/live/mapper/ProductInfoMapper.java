package com.enuos.live.mapper;

import com.enuos.live.dto.ProductInfoDTO;
import com.enuos.live.pojo.*;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface ProductInfoMapper {
//    long countByExample(@Param("id") Long id);

//    int deleteByPrimaryKey(Long id);

//    int insert(ProductInfo record);

//    int updateByExampleSelective(@Param("record") ProductInfo record, @Param("example") ProductInfoExample example);

//    int updateByPrimaryKeySelective(ProductInfo record);

//    Integer selectCount(Long id);

    List<ProductInfo> getProductInfo(ProductInfoDTO dto);

//    Map<String, Object> getPrice(@Param("id") Integer id);

//    ProductInfo getByIdProductInfo(@Param("id") Long id);

//    Map<String, Object> getByIdProductInfoMap(@Param("id") Long id);

    ProductPayInfo getProductPayInfo(@Param("productId") Long productId, @Param("priceId") Integer priceId);

    Map<String, Object> getProductCoreById(@Param("productCode") String productCode);

    List<Map<String, Object>> getRechargePackage(@Param("productId") String productId);

    /**
     * @Description: 充值套餐
     * @Param: [productId]
     * @Return: java.util.Map<java.lang.String,java.lang.Object>
     * @Author: wangyingjie
     * @Date: 2020/7/1
     */
    Map<String, Object> getRechargePackageByCode(String productId);

    /**
     * @Description: 会员信息
     * @Param: [userId]
     * @Return: com.enuos.live.pojo.Member
     * @Author: wangyingjie
     * @Date: 2020/6/30
     */
    Member getMember(Long userId);

    /**
     * @Description: 更新会员到期时间
     * @Param: [member]
     * @Return: int
     * @Author: wangyingjie
     * @Date: 2020/7/1
     */
    int updateExpirationTime(Member member);

}