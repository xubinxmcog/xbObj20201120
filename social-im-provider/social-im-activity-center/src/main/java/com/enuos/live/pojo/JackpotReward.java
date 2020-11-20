package com.enuos.live.pojo;

import lombok.Data;

import java.io.Serializable;
import java.util.Map;

/**
 * @Description 奖池奖励
 * @Author wangyingjie
 * @Date 2020/8/13
 * @Modified
 */
@Data
public class JackpotReward implements Serializable {

    private static final long serialVersionUID = -1733075029218810497L;

    /** 奖励序号 */
    private Integer sequence;

    /** 是否获取[0 否 1 是] */
    private Integer isGot;

    /** 奖励 */
    private Map<String, Object> reward;
}
