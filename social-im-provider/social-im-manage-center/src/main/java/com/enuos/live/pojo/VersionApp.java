package com.enuos.live.pojo;

import java.io.Serializable;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.NotNull;

/**
 * @ClassName VersionApp
 * @Description: TODO APP版本管理实体类
 * @Author xubin
 * @Date 2020/5/7
 * @Version V1.0
 **/
@Data
public class VersionApp implements Serializable {
    private Integer id;

    /**
     * 版本号
     */
    @NotNull(message = "版本号不能为空")
    private String version;

    /**
     * 平台：iOS Android
     */
    @NotNull(message = "平台为iOS或Android, 不能为空")
    private String platform;

    /**
     * 版本介绍
     */
    @NotNull(message = "版本介绍不能为空")
    private String introduction;

    /**
     * 下载链接
     */
    @NotNull(message = "版本下载链接不能为空")
    private String downloadUrl;

    /**
     * 状态：0无效 1有效
     */
    private Integer status;

    /**
     * 是否必须更新 0: 否 1是
     */
    @NotNull(message = "是否必须更新 0否 1是, 不能为空")
    private Integer isMust;

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