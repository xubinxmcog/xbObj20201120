package com.enuos.live.pojo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

/**
 * @Description 证件
 * @Author wangyingjie
 * @Date 16:56 2020/5/8
 * @Modified
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Card extends Base implements Serializable {

    private static final long serialVersionUID = -4958898937727081627L;

    /** 主键 */
    private Integer id;

    /** 手机 */
    private String phone;

    /** 真实姓名 */
    private String name;

    /** 性别 */
    private String sex;

    /** 证件类型[1:居民身份证;2:港澳居民来往内地通行证;3:台湾居民来往大陆通行证;4:护照] */
    private Integer cardType;

    /** 证件号码 */
    private String idCard;

    /** 创建时间 */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    /** 更新时间 */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;

}
