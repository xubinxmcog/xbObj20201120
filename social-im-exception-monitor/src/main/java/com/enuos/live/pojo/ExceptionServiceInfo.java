package com.enuos.live.pojo;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * exception_info
 * @author 
 */
@Data
public class ExceptionServiceInfo implements Serializable {
    private Long id;

    /**
     * 服务名称
     */
    private String name;

    /**
     * 服务IP
     */
    private String ip;

    /**
     * 上次连接时间
     */
    private String lastConnectionTime;


    /**
     * 创建时间
     */
    private Date createTime;

    private static final long serialVersionUID = 1L;
}