package com.enuos.live.pojo;

import java.io.Serializable;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

/**
 * tb_pets_info
 *
 * @author
 */
@Data
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PetsInfo implements Serializable {
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 小窝编码
     */
    private Long nestId;

    /**
     * 宠物ID
     */
    private String petCode;

    /**
     * 宠物昵称
     */
    private String petNick;

    /**
     * 性别
     */
    private Integer petSex;

    /**
     * 宠物等级
     */
    private Integer petLevel;

    /**
     * 总饱食
     */
    private Double allSaturat;

    /**
     * 当前饱食
     */
    private Double currentSaturat;

    /**
     * 总心情
     */
    private Double allMoodNum;

    /**
     * 当前心情
     */
    private Double currentMoodNum;

    /**
     * 状态
     */
    private Integer isStatus;

    /**
     * 是否使用中 1:是 0:否
     */
    private Integer isUse;

    /**
     * 修改目标 名字和性别需要
     */
    private String upTarget;

    /**
     * 饱食度更新时间
     */
    private Date saturatUpTime;

    /**
     * 心情更新时间
     */
    private Date moodUpTime;

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

    private static final long serialVersionUID = 1L;
}