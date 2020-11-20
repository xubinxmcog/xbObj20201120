package com.enuos.live.pojo;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.io.Serializable;

/**
 * @ClassName petsDressUpQualityConfig
 * @Description: TODO 宠物装扮品质效果配置
 * @Author xubin
 * @Date 2020/10/29
 * @Version V2.0
 **/
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PetsDressUpQualityConfig implements Serializable {
    private static final long serialVersionUID = -2636499820752007725L;

    private Integer id;

    /**
     * 品质ID 品质 1:优秀 2:稀有 3:传说 4:神话
     */
    private Integer qualityId;

    /**
     * 品质名称
     */
    private String qualityName;

    /**
     * 对应商品编码
     */
    private String productCode;

    /**
     * 金币获取速率(单位%)
     */
    private Integer effectGold;

    /**
     * 饱食消耗速率(单位%)
     */
    private Integer effectBeFull;

    /**
     * 心情消耗速率(单位%)
     */
    private Integer effectMood;

    /**
     * 食物使用效果(单位%)
     */
    private Integer effectFood;

    /**
     * 玩具使用效果(单位%)
     */
    private Integer effectToys;
}
