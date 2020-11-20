package com.enuos.live.pojo;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @Description 兑换
 * @Author wangyingjie
 * @Date 2020/10/13
 * @Modified
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class WritExchange extends Base implements Serializable {

    private static final long serialVersionUID = -6151218092502472735L;

    /** 兑换券 */
    private Integer ticket;

    /** 兑换列表 */
    private List<Reward> exchangeList;
}
