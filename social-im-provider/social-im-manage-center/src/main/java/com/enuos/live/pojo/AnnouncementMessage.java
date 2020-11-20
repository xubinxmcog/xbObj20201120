package com.enuos.live.pojo;

import java.io.Serializable;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.NotNull;

/**
 * @ClassName AnnouncementMessage
 * @Description: TODO 公告信息实体类
 * @Author xubin
 * @Date 2020/4/16
 * @Version V1.0
 **/
@Data
@ToString
public class AnnouncementMessage implements Serializable {
    /**
     * 主键
     */
    private Long id;

    /**
     * 内容
     */
    @NotNull(message = "内容不能为空")
    private String content;

    /**
     * 发布范围：0：系统内，1：房间，2：群组
     */
    @NotNull(message = "发布范围不能为空")
    private Integer releaseRange;

    /**
     * 范围ID
     */
    @NotNull(message = "范围ID不能为空")
    private Integer rangeId;

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
     * 状态：0：无效，1有效
     */
    private Integer status;

    /**
     * 创建时间
     */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date crateTime;

    /**
     * 更新时间
     */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;

    private static final long serialVersionUID = 1L;
}