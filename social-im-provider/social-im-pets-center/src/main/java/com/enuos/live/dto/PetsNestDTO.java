package com.enuos.live.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.ToString;

import java.io.Serializable;

/**
 * @ClassName PetsNestDTO
 * @Description: TODO
 * @Author xubin
 * @Date 2020/10/14
 * @Version V2.0
 **/
@Data
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PetsNestDTO implements Serializable {
    private static final long serialVersionUID = 652114528255240367L;

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
     * 解锁类型 1:等级解锁 2:钻石解锁 3:金币解锁 4:会员解锁
     */
    private Integer lockType;

    /**
     * 解锁值
     */
    private Integer lockValue;

    /**
     * 小窝名称
     */
    private String nestName;

    /**
     * 是否解锁 1:解锁 0:未解锁
     */
    private Integer isUnlock;

    /**
     * 宠物信息
     */
    private PetsInfoDTO petsInfo;


}
