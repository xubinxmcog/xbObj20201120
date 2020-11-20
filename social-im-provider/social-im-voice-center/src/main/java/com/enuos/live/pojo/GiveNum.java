package com.enuos.live.pojo;

import lombok.Data;

import java.io.Serializable;

/**
 * give_num
 * @author 
 */
@Data
public class GiveNum implements Serializable {
    private Long id;

    /**
     * 数量
     */
    private Integer num;

    /**
     * 描述
     */
    private String describe;

    private static final long serialVersionUID = 1L;

}