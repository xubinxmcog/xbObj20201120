package com.enuos.live.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * @ClassName UserMusicDTO
 * @Description: TODO
 * @Author xubin
 * @Date 2020/6/23
 * @Version V2.0
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class UserMusicDTO {

    private Long musicId;

    private Integer pageNum = 1;

    private Integer pageSize = 15;

    /**
     * 用户ID
     */
    private Long userId;

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
     * 类型  0:原创 1:伴奏
     */
    private Integer musicType;

    /**
     * 审核 0:未审核 1:已审核
     */
    private Integer auditStatus;


}
