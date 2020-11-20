package com.enuos.live.pojo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

/**
 * @ClassName PetsNestConfig
 * @Description: TODO
 * @Author xubin
 * @Date 2020/10/13
 * @Version V2.0
 **/
@Data
public class PetsNestConfig implements Serializable {
    private static final long serialVersionUID = 2061751371939290653L;

    private Integer id;

    /**
     * 小窝编码
     */
    private Integer nestId;

    /**
     * 小窝名称
     */
    private String nestName;

    /**
     * 解锁值
     */
    private Integer lockValue;

    /**
     * 解锁类型 1:等级解锁 2:钻石解锁 3:金币解锁 4:会员解锁
     */
    private Integer lockType;

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
