package com.enuos.live.dto;

import lombok.Data;
import lombok.ToString;

import java.io.Serializable;
import java.util.List;

/**
 * product_backpack 用户背包
 * @author 
 */
@Data
@ToString
public class CategoryBackpackDTO implements Serializable {

    /**
     * 分类ID
     */
    private Integer categoryId;

    /**
     * 分类名称
     */
    private String categoryName;

    /**
     * 分类编码
     */
    private String categoryCode;


    private List<BackpackDTO> list;

    private static final long serialVersionUID = 1L;
}