package com.enuos.live.pojo;

import java.io.Serializable;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

/**
 * tb_user
 * @author 
 */
@Data
public class User implements Serializable {
    /**
     * 主键ID
     */
    private Integer id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户性别 [1男 2女]
     */
    private Byte sex;

    /**
     * 用户年龄
     */
    private Integer age;

    /**
     * 用户生日
     */
    private Date birth;

    /**
     * 用户地区 [第三方平台授权地区]
     */
    private String area;

    /**
     * 用户图标 [第三方平台授权头像]的文件ID
     */
    private Integer iconFileId;

    /**
     * 用户图标 [第三方平台授权头像]的URL
     */
    private String iconUrl;

    /**
     * 用户背景文件ID
     */
    private Integer backgroundFileId;

    /**
     * 用户背景URL
     */
    private String backgroundUrl;

    /**
     * 用户昵称 [第三方平台授权昵称]
     */
    private String nickName;

    /**
     * 个性签名
     */
    private String signature;

    /**
     * 用户状态 [1 正常 2 封禁]
     */
    private Byte status;

    /**
     * 后台创建人员工号
     */
    private Integer createId;

    /**
     * 后台创建人员姓名
     */
    private String createName;

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