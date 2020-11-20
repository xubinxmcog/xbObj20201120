package com.enuos.live.pojo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * @Description 邀请返参
 * @Author wangyingjie
 * @Date 2020/8/5
 * @Modified
 */
@Data
public class Invite extends Base implements Serializable {

    private static final long serialVersionUID = -1311424530356481865L;

    /** 受邀人数 */
    private Integer inviteNum;

    /** 总收益 */
    private BigDecimal sumMoney;

    /** 审核中 */
    private BigDecimal auditMoney;

    /** 可提现 */
    private BigDecimal ableMoney;

    /** 已提现 */
    private BigDecimal haveMoney;

    /** 已提现 */
    private BigDecimal getMoney;

    /** 是否绑定微信[0 否 1 是] */
    private Integer isBindWeChatPay;

    /** 微信昵称 */
    private String weChatNickName;

    /** 背景 */
    private String backgroundUrl;

    /** 规则 */
    private String ruleUrl;

    /** 通知列表 */
    private List<Map<String, Object>> noticeList;

}
