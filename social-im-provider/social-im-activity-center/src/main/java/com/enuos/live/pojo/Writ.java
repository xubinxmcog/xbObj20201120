package com.enuos.live.pojo;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.io.Serializable;

/**
 * @Description 乐享令状
 * @Author wangyingjie
 * @Date 2020/10/10
 * @Modified
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Writ extends Page implements Serializable {

    private static final long serialVersionUID = -6045478207183595785L;

    /**
     * 列表[1 奖励 2 任务 3 兑换 4 排行]
     * 价格[1 等级价格 2 进阶价格]
     * 购买[1 等级 2 进阶]
     */
    private Integer type;

    /**
     * 任务编码
     */
    private String taskCode;

    /**
     * 模板编码
     */
    private String templateCode;

    /**
     * 等级
     */
    private Integer level;

    /**
     * 阶
     */
    private Integer step;

    /**
     * 奖励
     */
    private String rewardCode;

    /**
     * 数量
     */
    private Integer number;
}
