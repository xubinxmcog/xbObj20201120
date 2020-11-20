package com.enuos.live.pojo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

/**
 * @ClassName PetsNestUnlock
 * @Description: TODO 已解锁的小窝
 * @Author xubin
 * @Date 2020/10/13
 * @Version V2.0
 **/
@Data
public class PetsNestUnlock implements Serializable {
    private static final long serialVersionUID = -7825543144773861496L;

    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 小窝编码
     */
    private Integer nestId;

    /**
     * 宠物ID
     */
    private Long petsId;

    /**
     * 是否解锁 0:未解锁 1:解锁
     */
    private Integer isUnlock;

    /**
     * 录入时间
     */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date indateTime;

    /**
     * 最后修改时间
     */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date modifiedTime;
}
