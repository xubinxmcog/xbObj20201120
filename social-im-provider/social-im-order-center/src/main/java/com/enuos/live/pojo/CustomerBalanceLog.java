package com.enuos.live.pojo;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * customer_balance_log
 * @author 
 */
@Data
public class CustomerBalanceLog implements Serializable {
    /**
     * 积分日志ID
     */
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 记录来源：1订单，2退货单
     */
    private Integer source;

    /**
     * 相关单据ID
     */
    private Long sourceSn;

    /**
     * 记录生成时间
     */
    private Date createTime;

    /**
     * 支付类型：1：人民币，2：钻石，3：金币，4：积分，5：经验值
     */
    private Integer payTypeId;

    /**
     * 变动积分
     */
    private Long amount;

    private static final long serialVersionUID = 1L;
}