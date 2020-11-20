package com.enuos.live.pojo;

import lombok.Data;

import java.time.LocalDate;

/**
 * @Description 令状
 * @Author wangyingjie
 * @Date 2020/10/15
 * @Modified
 */
@Data
public class Writ {

    /** 令状期编码 */
    private String taskCode;

    /** 令状模板编码 */
    private String templateCode;

    /** 起始时间 */
    private LocalDate startTime;

    /** 结束时间 */
    private LocalDate endTime;
}
