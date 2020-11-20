package com.enuos.live.pojo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * @Description 提现
 * @Author wangyingjie
 * @Date 2020/8/5
 * @Modified
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class InviteMoney extends Base implements Serializable {

    private static final long serialVersionUID = 784828044631254165L;

    /** 提现金额 */
    private BigDecimal getMoney;

    /** 审核金额 */
    private BigDecimal auditMoney;

    /** 审核状态[-1：驳回；1：待审；1：通过] */
    private Integer auditStatus;

    /** 单号 */
    private String orderId;

    /** 描述 */
    private String description;

    /** 提现时间 */
    @JsonFormat(timezone = "GMT+8", pattern = "MM-dd HH:mm")
    @DateTimeFormat(pattern = "MM-dd HH:mm")
    private LocalDateTime getTime;

}
