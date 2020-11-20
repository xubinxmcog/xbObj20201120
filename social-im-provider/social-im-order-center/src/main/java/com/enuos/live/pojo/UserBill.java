package com.enuos.live.pojo;

import java.io.Serializable;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.NotNull;

/**
 * @ClassName ProductCategory
 * @Description: TODO 用户账单实体类
 * @Author xubin
 * @Date 2020/6/18
 * @Version V2.0
 **/
@Data
public class UserBill implements Serializable {
    private Long billId;

    /**
     * 用户ID
     */
    @NotNull(message = "用户ID不能为空")
    private Long userId;

    /**
     * 商品名称
     */
    @NotNull(message = "商品名称不能为空")
    private String productName;

    /**
     * 交易价格
     */
    @NotNull(message = "交易价格不能为空")
    private Long price;

    /**
     * 价格类型
     */
    @NotNull(message = "价格类型不能为空")
    private Integer priceType;

    /**
     * 状态: 1:成功
     */
    @NotNull(message = "状态不能为空")
    private Integer status;

    /**
     * 创建时间
     */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    private static final long serialVersionUID = 1L;
}