package com.enuos.live.pojo;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

/**
 * @Description 奖池
 * @Author wangyingjie
 * @Date 2020/8/13
 * @Modified
 */
@Data
public class Jackpot implements Serializable {

    private static final long serialVersionUID = 6208419167415917392L;

    /** 奖池号[1-6] */
    @NotNull(message = "奖池号不能为空")
    private Integer id;

    /** 奖池名称 */
    private String name;

    /** 奖池背板 */
    private String url;

    /** 奖池奖励 */
    private List<JackpotReward> list;

}
