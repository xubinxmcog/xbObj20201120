package com.enuos.live.dto;

import com.enuos.live.pojo.PetsDressUpQualityConfig;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * @ClassName PetsInfoAndDressUpDTO
 * @Description: TODO
 * @Author xubin
 * @Date 2020/10/30
 * @Version V2.0
 **/
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PetsInfoAndDressUpDTO implements Serializable {

    private static final long serialVersionUID = 1270624010457225573L;
    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 宠物ID
     */
    private Long petsId;

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
     * 饱食度更新时间
     */
    private Date saturatUpTime;

    /**
     * 心情更新时间
     */
    private Date moodUpTime;

    private List<PetsDressUpQualityConfig> effectQualitys;
}
