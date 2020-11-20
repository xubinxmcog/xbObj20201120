package com.enuos.live.manager;

/**
 * @Description 乐享令状枚举类
 * @Author wangyingjie
 * @Date 2020/10/10
 * @Modified
 */
public enum WritEnum {

    SA("writ.step.a", "解锁进阶版价格[钻石]", 500),
    SB("writ.step.b", "解锁至尊进阶版价格[钻石]", 1770),
    SAI("writ.step.a.integral", "进阶版解锁奖励积分[积分]", 2000),
    SBI("writ.step.b.integral", "至尊进阶版解锁奖励积分[积分]", 20000),
    SAIL("writ.step.a.integral.line", "普通版每周积分上限[积分]", 8000),
    SBIL("writ.step.b.integral.line", "进阶版每周积分上限[积分]", 10000),

    LP("writ.level.price", "购买等级每级价格[钻石]", 77),
    LI("writ.level.integral", "每级积分[积分]", 1000);

    public final String CODE;

    public final String NAME;

    public final Integer VALUE;

    WritEnum(String CODE, String NAME, Integer VALUE) {
        this.CODE = CODE;
        this.NAME = NAME;
        this.VALUE = VALUE;
    }
}
