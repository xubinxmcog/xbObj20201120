package com.enuos.live.pojo;

import java.io.Serializable;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.NotNull;

/**
 * @ClassName Feedback
 * @Description: TODO 意见反馈实体类
 * @Author xubin
 * @Date 2020/4/22
 * @Version V1.0
 **/
@Data
public class Feedback implements Serializable {
    private Integer id;

    /**
     * 用户ID
     */
    @NotNull(message = "用户ID不能为空")
    private Long userId;

    /**
     * 反馈内容
     */
    @NotNull(message = "反馈内容不能为空")
    private String content;

    /**
     * 处理人员ID
     */
    private Long handleId;

    /**
     * 处理操作：0：未处理 1：已处理 2：置后
     */
    private Integer handleAction;

    /**
     * 处理结果
     */
    private String handleResult;

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