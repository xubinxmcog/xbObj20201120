package com.enuos.live.pojo;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * pets_label
 * @author 
 */
@Data
public class PetsLabel implements Serializable {
    private Integer id;

    /**
     * 标签
     */
    private String label;

    /**
     * 标签code
     */
    private String labelCode;

    /**
     * 标签名称
     */
    private String labelName;

    /**
     * 父类ID
     */
    private Integer parentId;

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