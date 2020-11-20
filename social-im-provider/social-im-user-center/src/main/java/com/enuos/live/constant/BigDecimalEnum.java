package com.enuos.live.constant;

import java.math.BigDecimal;

/**
 * @Description 金额枚举
 * @Author wangyingjie
 * @Date 2020/8/5
 * @Modified
 */
public enum BigDecimalEnum {

    RMB1(new BigDecimal("1.00"), "提现最小金额"),
    RMB30(new BigDecimal("30.00"), "每日提现最大金额");

    private BigDecimal money;

    private String description;

    BigDecimalEnum(BigDecimal money, String description) {
        this.money = money;
        this.description = description;
    }

    public BigDecimal getMoney() {
        return money;
    }

}
