package com.enuos.live.pojo;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * exception_send_msg_config
 * @author 
 */
@Data
public class ExceptionSendMsgConfig implements Serializable {
    private Integer id;

    /**
     * 目标姓名
     */
    private String toName;

    /**
     * 邮箱
     */
    private String eMail;

    /**
     * 异常类别 1:接口 2:服务
     */
    private Byte eType;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date upTime;

    private static final long serialVersionUID = 1L;
}