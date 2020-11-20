package com.enuos.live.manager;

/**
 * @Description
 * @Author wangyingjie
 * @Date 2020/8/7
 * @Modified
 */
public enum CurrencyEnum {

    RMB("RMB", 1, "人民币"),
    DIAMOND("DIAMOND", 2, "钻石"),
    GOLD("GOLD", 3, "金币"),
    ACTIVE("ACTIVE", 4, "活跃"),
    EXP("EXP", 5, "经验"),
    GASHAPON("GASHAPON", 6, "扭蛋"),
    PROP001("PROP001", 7, "奖章"),
    PASSCHECK001("PASSCHECK001", 7, "令状券");

    public final String CODE;

    public final Integer TYPE;

    public final String DESCRIPTION;

    CurrencyEnum(String CODE, Integer TYPE, String DESCRIPTION) {
        this.CODE = CODE;
        this.TYPE = TYPE;
        this.DESCRIPTION = DESCRIPTION;
    }
}
