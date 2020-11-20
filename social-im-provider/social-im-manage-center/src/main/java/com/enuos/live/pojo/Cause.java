package com.enuos.live.pojo;

import java.io.Serializable;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

/**
 * @ClassName ReportCause
 * @Description: TODO 举报理由实体类
 * @Author xubin
 * @Date 2020/4/22
 * @Version V1.0
 **/
@Data
public class Cause implements Serializable {
    private Long id;

    /**
     * 举报理由
     */
    private String cause;

    /**
     * 类型： 0：账号注销理由 1：举报理由
     */
    private Integer type;

    /**
     * 更新时间
     */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;

    private static final long serialVersionUID = 1L;
}