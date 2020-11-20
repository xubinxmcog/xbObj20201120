package com.enuos.live.pojo;

import java.io.Serializable;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

/**
 * @ClassName ProductCategory
 * @Description: TODO 发送红包实体类
 * @Author xubin
 * @Date 2020/6/10
 * @Version V1.0
 **/
@Data
public class RedPacketsSend implements Serializable {
    /**
     * 主键
     */
    private Long id;

    /**
     * 发红包用户ID
     */
    @NotNull(message = "用户ID不能为空")
    private Long userId;

    /**
     * 语音房ID
     */
    @NotNull(message = "语音房ID不能为空")
    private Long roomId;

    /**
     * 红包总个数
     */
    @NotNull(message = "红包总个数不能为空")
    @Max(message = "红包个数最多50个", value = 50)
    @Min(message = "红包个数最少1个", value = 1)
    private Integer rpNum;

    /**
     * 红包总金额
     */
    @NotNull(message = "红包总额不能为空")
    @Min(message = "红包总额不能低于10金币", value = 10)
    @Max(message = "红包总额最多100000金币", value = 100000)
    private Integer totalAmount;

    /**
     * 剩余红包
     */
    private Integer rpSurplus;

    /**
     * 剩余金额
     */
    private Integer surplusAmount;

    /**
     * 是否关闭: 0:否 1是
     */
    private Integer isClose;

    /**
     * 用户头像缩略图URL
     */
    private String thumbIconUrl;

    /**
     * 用户昵称
     */
    private String nickName;

    /**
     * 发放时间
     */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date sendTime;

    /**
     * 最后修改时间
     */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date modifiedTime;

    private static final long serialVersionUID = 1L;
}