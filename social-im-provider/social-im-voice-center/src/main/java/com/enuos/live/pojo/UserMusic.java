package com.enuos.live.pojo;

import java.io.Serializable;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

/**
 * user_music
 * @author 
 */
@Data
public class UserMusic implements Serializable {
    private Long id;

    private Long musicId;

    private Integer isAdd;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户名称
     */
    private String userName;

    /**
     * 音乐名
     */
    private String musicName;

    /**
     * 歌手
     */
    private String musicSinger;

    /**
     * 专辑
     */
    private String musicAlbum;

    /**
     * 资源链接
     */
    private String musicUrl;

    /**
     * 封面
     */
    private String musicCover;

    /**
     * 类型
     */
    private Integer musicType;

    /**
     * 是否有效 0:无效 1:有效
     */
    private Integer isStatus;

    /**
     * 审核 0:未审核 1:已审核
     */
    private Integer auditStatus;

    /**
     * 创建时间
     */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    /**
     * 最后修改时间
     */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date modifiedTime;

    private static final long serialVersionUID = 1L;
}