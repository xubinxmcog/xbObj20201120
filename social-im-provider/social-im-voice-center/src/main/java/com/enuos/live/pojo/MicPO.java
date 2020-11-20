package com.enuos.live.pojo;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;
import java.util.Set;

/**
 * @Description
 * @Author wangyingjie
 * @Date 2020/5/13
 * @Modified
 */
@Data
public class MicPO extends Base implements Serializable {

    private static final long serialVersionUID = 8750377909229838739L;

    /**
     * 房间号
     */
    @NotNull(message = "房间号不能为空")
    private Long roomId;

    /**
     * 房房主
     */
    private Long roomUserId;

    /**
     * 头像
     */
    private String thumbIconUrl;

    /**
     * 操作[0 取消排麦 1 排麦]
     */
    private Integer type = 0;

    /**
     * 分值
     */
    private Integer score;

    private List<Long> userIdList;

    /**
     * 原排麦用户列表
     */
    private List<Long> originalUserIds;

    /**
     * 现在的排麦用户列表
     */
    private List<Long> currentUserIds;

}
