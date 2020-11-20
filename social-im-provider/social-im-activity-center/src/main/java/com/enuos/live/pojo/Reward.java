package com.enuos.live.pojo;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @Description 奖励
 * @Author wangyingjie
 * @Date 2020/10/10
 * @Modified
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Reward implements Serializable {

    /** 模板编码 */
    private String templateCode;

    /** 唯一标识 */
    private String code;

    /** 奖励类型 */
    private Integer category;

    /** 等级 */
    private Integer level;

    /** 礼盒编码 */
    private String boxCode;

    /** 奖品编码 */
    private String rewardCode;

    /** 奖品名称 */
    private String rewardName;

    /** 奖品描述 */
    private String description;

    /** 奖品时效 */
    private Long life;

    /** 奖品数量 */
    private Integer number;

    /** 奖品图标 */
    private String url;

    /** 类别 */
    private Integer categoryId;

    /** 权重 */
    private Integer weight;

    /** 兑换价 */
    private Integer expendTicket;

    /** 是否获取 */
    private Integer isGot;

    /** 礼盒 */
    private List<Reward> boxRewardList;
}
