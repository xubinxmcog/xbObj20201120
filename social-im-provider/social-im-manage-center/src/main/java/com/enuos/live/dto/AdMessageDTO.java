package com.enuos.live.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Date;

/**
 * @ClassName AdMessageDTO
 * @Description: TODO 广告信息实体类
 * @Author xubin
 * @Date 2020/4/16
 * @Version V1.0
 **/
@Data
public class AdMessageDTO implements Serializable {

    private Integer pageNum = 1;

    private Integer pageSize = 10;

    /**
     * 主键
     */
    private Long id;

    /**
     * 标题
     */
    private String title;

    /**
     * 内容
     */
    private String content;

    /**
     * 广告链接
     */
    private String linkUrl;

    /**
     * 文件链接
     */
    private String fileUrl;

    /**
     * 发布人ID
     */
    private Integer editId;

    /**
     * 发布人姓名
     */
    private String editName;

    /**
     * 状态：状态：0：无效，1：有效
     */
//    @Max(message = "最大值不能大于3", value = 3)
//    @Min(message = "最小值不能小于1", value = 1)
    private Integer status;

    /**
     * 创建时间
     */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date crateTime;

    /**
     * 发布时间
     */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date releaseTime;

    /**
     * 更新时间
     */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;

    private String beginTime;

    private String endTime;

    private static final long serialVersionUID = 1L;
}