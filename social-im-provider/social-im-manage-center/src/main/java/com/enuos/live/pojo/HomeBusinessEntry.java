package com.enuos.live.pojo;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * home_business_entry
 * @author 
 */
@Data
public class HomeBusinessEntry implements Serializable {
    private Integer id;

    /**
     * 图标
     */
    private String iconUrl;

    /**
     * 名称
     */
    private String name;

    /**
     * 跳转连接
     */
    private String linkUrl;

    /**
     * 是否有效 0:无效 1:有效
     */
    private Integer isStatus;

    /**
     * 录入时间
     */
    private Date indateTime;

    /**
     * 最后修改时间
     */
    private Date modifiedTime;

    private static final long serialVersionUID = 1L;
}