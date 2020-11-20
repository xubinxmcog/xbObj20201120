package com.enuos.live.pojo;

import java.io.Serializable;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

/**
 * @ClassName AdMessage
 * @Description: TODO 广告信息实体类
 * @Author xubin
 * @Date 2020/4/16
 * @Version V1.0
 **/
@Data
public class AdMessage implements Serializable {
    /**
     * 主键
     */
    private Long id;

    /**
     * 标题
     */
    @NotNull(message = "标题不能为空")
    private String title;

    /**
     * 内容
     */
    @NotNull(message = "内容不能为空")
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
    @NotNull(message = "发布人ID不能为空")
    private Long editId;

    /**
     * 发布人姓名
     */
    @NotNull(message = "发布人姓名不能为空")
    private String editName;

    /**
     * 状态：状态：0：无效，1：有效
     */
//    @Max(message = "最大值不能大于2",value = 2)
//    @Min(message = "最小值不能小于1",value = 1)
//    @NotNull(message = "状态不能为空")
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

    private static final long serialVersionUID = 1L;
}