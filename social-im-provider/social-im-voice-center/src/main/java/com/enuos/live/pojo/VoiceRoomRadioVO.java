package com.enuos.live.pojo;

import lombok.Data;

import java.io.Serializable;

/**
 * @ClassName VoiceRoomRadioVO
 * @Description: TODO 专辑
 * @Author xubin
 * @Date 2020/7/21
 * @Version V2.0
 **/
@Data
public class VoiceRoomRadioVO implements Serializable {

    private static final long serialVersionUID = 4185579348289501119L;


    /**
     * id
     */
    private Integer albumId;

    /**
     * 标题
     */
    private String albumTitle;

    /**
     * 封面
     */
    private String coverUrlSmall;

}
