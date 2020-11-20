package com.enuos.live.pojo;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * tb_voice_room_heat
 * @author 
 */
@Data
public class TbVoiceRoomHeat implements Serializable {
    private Long id;

    /**
     * 语音房ID
     */
    private Long roomId;

    /**
     * 房间热度
     */
    private Long heat;

    /**
     * 最后修改时间
     */
    private Date modifiedTime;

    private static final long serialVersionUID = 1L;
}