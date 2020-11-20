package com.enuos.live.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

/**
 * @ClassName PetsInfoDTO
 * @Description: TODO
 * @Author xubin
 * @Date 2020/10/12
 * @Version V2.0
 **/
@Data
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PetsInfoDTO implements Serializable {

    private static final long serialVersionUID = -4265500130731343789L;

    /**
     * 宠物code
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
     * 总饱食
     */
    private Integer allSaturat;

    /**
     * 状态
     */
    private Integer isStatus;

    /**
     * 总心情
     */
    private Integer allMoodNum;

    /**
     * 宠物ID
     */
    private Long petsId;

    /**
     * 宠物等级
     */
    private Integer petLevel;

    /**
     * 当前心情
     */
    private Integer currentMoodNum;

    /**
     * 图片链接
     */
    private String picUrl;

    /**
     * 当前饱食
     */
    private Integer currentSaturat;


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
