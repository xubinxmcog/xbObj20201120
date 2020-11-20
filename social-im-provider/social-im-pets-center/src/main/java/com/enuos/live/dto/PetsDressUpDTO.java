package com.enuos.live.dto;

import com.enuos.live.pojo.PetsDressUpQualityConfig;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

/**
 * @ClassName PetsDressUpDTTO
 * @Description: TODO
 * @Author xubin
 * @Date 2020/10/30
 * @Version V2.0
 **/
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PetsDressUpDTO {

    private Long userId;

    private Long petsId;

    private Long backpackId;

    private Long timeLimit;

    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd hh:mm:ss")
    private Date useTime;

    private String productCode;

    private Integer productStatus;

    private String productName;

    private String picUrl;

    private String attribute;

    private String termDescribe;

    private PetsDressUpQualityConfig effectQuality;
}
