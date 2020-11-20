package com.enuos.live.pojo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @Description 成就
 * @Author wangyingjie
 * @Date 2020/6/17
 * @Modified
 */
@Data
public class Achievement implements Serializable {

    private static final long serialVersionUID = -7592615968016061088L;

    private String title;

    private String description;

    private String code;

    private Integer line;

    private Integer progress;

    private String iconUrl;

    private Integer type;

    private Integer isGot;

    private Integer isWear;

    private List<AchievementReward> rewardList;

}

@Data
class AchievementReward implements Serializable {

    private static final long serialVersionUID = 8280368548240210609L;

    private String rewardCode;

    private String number;

    private String url;

}
