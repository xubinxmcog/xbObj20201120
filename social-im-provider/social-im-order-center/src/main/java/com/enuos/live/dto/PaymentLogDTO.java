package com.enuos.live.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.NumberFormat;

import java.util.Date;

/**
 * @ClassName PaymentLogDTO
 * @Description: TODO
 * @Author xubin
 * @Date 2020/4/10
 * @Version V1.0
 **/
@Data
public class PaymentLogDTO {

    private Long orderId;//订单ID

    private Long orderSn; //订单编号

    private Long customerId;  //下单人ID

    private String shippingUser; // 收货人姓名

    private Integer paymentMethod; //支付方式：1金币，2钻石

    private Long orderMoney; // 订单金额

    private Long districtMoney; // 优惠金额

    @NumberFormat(style = NumberFormat.Style.CURRENCY, pattern = "#.##")
    private Float paymentMoney; // 支付金额

    private Long productId; // 订单商品ID

    private String productName; //商品名称

    private Integer productCnt;  //购买商品数量

    private Long productPrice; // 购买商品单价

    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd hh:mm:ss")
    private Date createTime; // 下单时间

}
