package com.enuos.live.pojo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

/**
 * @Description 用户附属信息
 * @Author wangyingjie
 * @Date 2020/5/29
 * @Modified
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AccountAttach extends Base {

    /** 主键 */
    private Integer id;

    /** 会员等级 */
    private Integer vip;

    /** 成长值 */
    private Integer growth;

    /** VIP到期时间 */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime expirationTime;

    /** 用户等级 */
    private Integer level;

    /** 用户经验 */
    private Long experience;

    /** 用户金币 */
    private Long gold;

    /** 用户钻石 */
    private Long diamond;

    /** 扭蛋数 */
    private Integer gashapon;

    /** 身价[单位GOLD] */
    private Long worth;

    /** 魅力值 */
    private Long charm;

    /** 是否会员 */
    private Integer isMember;

    /** 每日经验 */
    private Long dayExp;

    /** 还可获得的经验 */
    private Long remainderExp;

    /** 日期 */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /** 日期 */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

}
