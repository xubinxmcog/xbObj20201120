package com.enuos.live.pojo;

import lombok.Data;

/**
 * @Description
 * @Author wangyingjie
 * @Date 2020/6/23
 * @Modified
 */
@Data
public class GashaponUser {

    /** 主键 */
    private Integer id;

    /** 用户ID */
    private Long userId;

    /** 参与次数 */
    private Integer joinCount;

    /** 参与结果[0：待开奖 1：中奖 2：未中奖] */
    private Integer result;

}
