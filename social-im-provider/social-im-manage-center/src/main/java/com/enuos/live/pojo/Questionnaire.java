package com.enuos.live.pojo;

import java.io.Serializable;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.NotNull;

/**
 * questionnaire
 * @author 
 */
@Data
public class Questionnaire implements Serializable {
    private Long id;

    /**
     * 用户ID
     */
//    @NotNull(message = "userId不能为空")
    private Long userId;

    /**
     * 性别 0:其他 1: 男 2: 女
     */
//    @NotNull(message = "性别不能为空")
    private Integer sex;

    /**
     * 年龄: 1:16岁以下 2:16-20岁 3: 21-25岁 4:26-30岁 5:31-40岁 6:40岁以上
     */
//    @NotNull(message = "年龄不能为空")
    private Integer age;

    /**
     * 职业
     */
//    @NotNull(message = "职业不能为空")
    private String profession;

    /**
     * 安装来源
     */
//    @NotNull(message = "安装途径不能为空")
    private String source;

    /**
     * 整体满意度:5-1很不满意-非常满意
     */
//    @NotNull(message = "整体满意度不能为空")
    private Integer satisfaction;

    /**
     * 性能满意度:5-1很不满意-非常满意
     */
//    @NotNull(message = "性能满意度不能为空")
    private Integer performance;

    /**
     * 耗电量:5-1很不满意-非常满意
     */
//    @NotNull(message = "耗电量不能为空")
    private Integer powerConsumption;

    /**
     * 耗流量:5-1很不满意-非常满意
     */
//    @NotNull(message = "耗流量不能为空")
    private Integer flowConsumption;

    /**
     * 加载速度:5-1很不满意-非常满意
     */
//    @NotNull(message = "加载速度不能为空")
    private Integer loadingSpeed;

    /**
     * 网络顺畅:5-1很不满意-非常满意
     */
//    @NotNull(message = "网络顺畅不能为空")
    private Integer networkDelay;

    /**
     * 是否遇到过bug: 1: 从未 2: 极少 3: 偶尔 4: 较多 5: 频繁
     */
//    @NotNull(message = "黑屏、卡顿或卡死的情况不能为空")
    private Integer bug;

    /**
     * 描述在什么情况下出现bug
     */
    private String bugCondition;

    /**
     * 美观度: 5-1很不满意-非常满意 
     */
//    @NotNull(message = "7乐产品美观度不能为空")
    private Integer beautiful;

    /**
     * 交互功能和使用流畅度: 5-1很不满意-非常满意
     */
//    @NotNull(message = "7乐产品的交互功能和使用流畅满意度不能为空")
    private Integer fluency;

    /**
     * 功能完备可以满足您的需求: 1:能 2:一般 3:不能
     */
//    @NotNull(message = "功能完备度不能为空")
    private Integer satisfy;

    /**
     * 功能足够丰富吗: 1:非常丰富 2:比较丰富 3:一般 4:不太丰富 5:很不丰富
     */
//    @NotNull(message = "功能丰富度不能为空")
    private Integer rich;

    /**
     * 能够帮助你找到玩得来或聊得来的朋友吗？1:能 2:一般 3:不能
     */
//    @NotNull(message = "能够帮助你找到玩得来或聊得来的朋友不能为空")
    private Integer findFriends;

    /**
     * 产品的氛围如何？1:非常好 2:比较好 3:一般 4:不太好 5:很不好
     */
//    @NotNull(message = "7乐产品的氛围 不能为空")
    private Integer atmosphere;

    /**
     * 产品的活动是否满意？1:非常满意 2:比较满意 3:一般 4:不太满意 5:很不满意
     */
//    @NotNull(message = "产品的活动是否满意 不能为空")
    private Integer activity;

    /**
     * 活动通知及时：1:非常满意 2:比较满意 3:一般 4:不太满意 5:很不满意
     */
//    @NotNull(message = "通知及时 不能为空")
    private Integer activityNotice;

    /**
     * 活动丰富: 1:非常满意 2:比较满意 3:一般 4:不太满意 5:很不满意
     */
//    @NotNull(message = "活动丰富 不能为空")
    private Integer activityRich;

    /**
     * 活动内容有趣: 1:非常满意 2:比较满意 3:一般 4:不太满意 5:很不满意
     */
//    @NotNull(message = "内容有趣 不能为空")
    private Integer activityInteresting;

    /**
     * 活动有参与感：1:非常满意 2:比较满意 3:一般 4:不太满意 5:很不满意
     */
//    @NotNull(message = "有参与感 不能为空")
    private Integer activityParticipate;

    /**
     * 活动有吸引力: 1:非常满意 2:比较满意 3:一般 4:不太满意 5:很不满意
     */
//    @NotNull(message = "有吸引力 不能为空")
    private Integer activityAttractive;

    /**
     * 您最喜欢的功能是？【游戏】【语音房】【朋友圈/广场】【其他】__
     */
//    @NotNull(message = "您最喜欢的功能 不能为空")
    private String likeFeatures;

    /**
     * 是否愿意将7乐产品推荐给您的朋友: 1:非常愿意 2:愿意 3:一般 4:不愿意 5:很不愿意
     */
//    @NotNull(message = "是否愿意将7乐产品推荐给您的朋友 不能为空")
    private Integer recommend;

    /**
     * 使用过的同类产品
     */
    private String similarProducts;

    /**
     * 每天使用7乐产品的时间大约为:1: 1小时以内 2: 1-2小时 2: 2-3小时 3: 3-5小时 4: 5小时以上
     */
//    @NotNull(message = "每天使用7乐产品的时间大约 不能为空")
    private Integer useTime;

    /**
     * 建议
     */
    private String suggest;

    /**
     * QQ号
     */
    private String qq;

    /**
     * 电话
     */
    private String tel;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 创建时间
     */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    private static final long serialVersionUID = 1L;
}