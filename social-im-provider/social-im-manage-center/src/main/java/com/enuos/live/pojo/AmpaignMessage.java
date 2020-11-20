package com.enuos.live.pojo;

import java.io.Serializable;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

/**
 * @ClassName AmpaignMessage
 * @Description: TODO 活动信息实体类
 * @Author xubin
 * @Date 2020/4/20
 * @Version V1.0
 **/
@Data
public class AmpaignMessage implements Serializable {
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
     * 活动链接
     */
    private String linkUrl;

    /**
     * 文件链接
     */
    private String fileUrl;

    /**
     * 发布人ID
     */
    private Long editId;

    /**
     * 发布人姓名
     */
    private String editName;

    /**
     * 状态：0：失效，1：有效
     */
    private Integer status;

    /**
     * 活动开始时间
     */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date ampaignStartTime;

    /**
     * 活动结束时间
     */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date ampaignEndTime;

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

    /**
     * 排序: 数字越大 排名越靠前
     */
    private Integer sort;

    private static final long serialVersionUID = 1L;
}