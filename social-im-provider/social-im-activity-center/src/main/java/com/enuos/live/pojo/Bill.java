package com.enuos.live.pojo;

import lombok.*;

import java.time.LocalDateTime;

/**
 * @Description 账单
 * @Author wangyingjie
 * @Date 2020/10/13
 * @Modified
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Bill {

    /** 用户ID */
    private Long userId;

    /** 商品名称 */
    private String productName;

    /** 交易价格 进账为正 出账为负 */
    private Long price;

    /** 价格类型[2 钻石 3 金币] */
    private Integer priceType;

    /** 状态[1 成功] */
    private Integer status;

    /** 创建时间 */
    private LocalDateTime createTime;

}
