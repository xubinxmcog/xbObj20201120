package com.enuos.live.pojo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

/**
 * @ClassName UserAccountAttach
 * @Description: TODO 账户附属信息实体
 * @Author xubin
 * @Date 2020/4/8
 * @Version V1.0
 **/
@Data
@ToString
public class UserAccountAttachPO implements Serializable {

    /**
     * 主键ID
     */
    private Integer id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户经验
     */
    private Long experience;

    /**
     * 用户等级
     */
    private Integer level;

    /**
     * 用户金币
     */
    private Long gold;

    /**
     * 用户钻石
     */
    private Long diamond;

    /**
     * 是否会员 0：否 1：是
     */
    private Integer isMember;

    /**
     * 头像框
     */
    private String iconFrame;

    /**
     * 皮肤
     */
    private String skin;

    /**
     * 创建时间
     */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    /**
     * 更新时间
     */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;

}
