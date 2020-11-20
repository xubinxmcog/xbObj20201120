package com.enuos.live.teens.pojo;

import java.io.Serializable;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

/**
 * @ClassName TeensModel
 * @Description: TODO 青少年模式实体类
 * @Author xubin
 * @Date 2020/5/6
 * @Version V1.0
 **/
@Data
public class TeensModel implements Serializable {
    /**
     * 主键ID
     */
    private Integer id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 模式密码
     */
    private String teensPwd;

    /**
     * 确认密码
     */
    private String pwd;

    /**
     * 盐
     */
    private String salt;

    /**
     * 状态 0：未开启 1：开启
     */
    private Integer status;

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