package com.enuos.live.pojo;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

/**
 * @Description 红点提示
 * @Author wangyingjie
 * @Date 2020/8/18
 * @Modified
 */
@Data
public class Tip extends Base implements Serializable {

    private static final long serialVersionUID = -8090414080148770006L;

    /** 提示类别[1：每日签到；2：星座签到；3：活跃；4：日常；5：成就；6：扭蛋抽奖；7：扭蛋兑换；8：等级；9：邀请；10：活动] */
    @NotNull(message = "提示类别不能为空")
    private List<String> categoryList;
}
