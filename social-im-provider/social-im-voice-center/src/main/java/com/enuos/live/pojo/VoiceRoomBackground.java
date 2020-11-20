package com.enuos.live.pojo;

import java.io.Serializable;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

/**
 * tb_voice_room_background
 * @author 
 */
@Data
public class VoiceRoomBackground implements Serializable {
    private Integer id;

    /**
     * 名称
     */
    private String name;

    /**
     * 图片链接
     */
    private String picUrl;

    /**
     * 是否有效 0:无效 1:有效
     */
    private Integer isStatus;

    /**
     * 创建时间
     */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date indateTime;

    /**
     * 修改时间
     */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date modifiedTime;

    private static final long serialVersionUID = 1L;
}