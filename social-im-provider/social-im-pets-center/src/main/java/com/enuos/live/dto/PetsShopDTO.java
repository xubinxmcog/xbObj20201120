package com.enuos.live.dto;

import com.enuos.live.pojo.PetsDressUpQualityConfig;
import com.enuos.live.pojo.PetsProductPrice;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * @ClassName PetsShopDTO
 * @Description: TODO 商店商品
 * @Author xubin
 * @Date 2020/8/31
 * @Version V2.0
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PetsShopDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 类别ID
     */
    private Integer categoryId;

    private Long productId;

    /**
     * 物品编码
     */
    private String productCode;

    /**
     * 物品名称
     */
    private String productName;

    /**
     * 物品图片
     */
    private String picUrl;

    /**
     * 排序 : 数字越小,排名越靠前
     */
    private Long sort;

    /**
     * 属性
     */
    private String attribute;

    /**
     * 描述
     */
    private String descript;

    private List<PetsProductPrice> prices;


    private PetsDressUpQualityConfig effectQuality;

}
