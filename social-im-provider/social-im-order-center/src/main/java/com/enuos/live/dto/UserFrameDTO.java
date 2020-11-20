package com.enuos.live.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

/**
 * @ClassName UserFrameDTO
 * @Description: TODO
 * @Author xubin
 * @Date 2020/7/29
 * @Version V2.0
 **/
@Data
public class UserFrameDTO {

    private String iconFrame;

    private String chatFrame;

    private Object chatFrameAttribute;

    //    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
//    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date ifTime;

    //    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
//    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date cfTime;

    private Long userId;
}
