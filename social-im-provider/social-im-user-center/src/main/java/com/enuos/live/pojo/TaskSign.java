package com.enuos.live.pojo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * @Description 签到任务
 * @Author wangyingjie
 * @Date 15:23 2020/4/9
 * @Modified
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TaskSign extends Base implements Serializable {

    private static final long serialVersionUID = 7756327451141521248L;

    /** 任务ID */
    private Integer taskId;

    /** code */
    private String code;

    /** 签到类型[0：正常；1：补签] */
    private Integer signType;

    /** 签到时间 */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate signTime;

    /** 签到次数 */
    private Integer signCount;

    /** 连续签到次数 */
    private Integer continueSignCount;

    /** 补签次数 */
    private Integer backSignCount;

}
