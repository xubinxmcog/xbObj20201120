package com.enuos.live.pojo;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * exception_info
 * @author 
 */
@Data
public class ExceptionInfo implements Serializable {
    private Long id;

    /**
     * 异常接口
     */
    private String url;

    /**
     * 编码
     */
    private String exceptionCode;

    /**
     * 异常描述
     */
    private String describe;

    /**
     * 异常信息详情
     */
    private String contnet;

    /**
     * 创建时间
     */
    private Date createTime;

    private static final long serialVersionUID = 1L;
}