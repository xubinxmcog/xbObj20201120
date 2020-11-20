package com.enuos.live.pojo;

import java.io.Serializable;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

/**
 * @ClassName Feedback
 * @Description: TODO 协议实体类
 * @Author xubin
 * @Date 2020/4/22
 * @Version V1.0
 **/
@Data
public class Agreement implements Serializable {
    private Integer id;

    /**
     * 类型：1：用户 2：隐私 3：其他
     */
    private Byte type;

    /**
     * 标题
     */
    private String title;

    /**
     * 协议内容
     */
    private String content;

    /**
     * 创建时间
     */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    /**
     * 修改时间
     */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;

    private static final long serialVersionUID = 1L;
}