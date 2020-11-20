package com.enuos.live.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.ToString;

import java.io.Serializable;
import java.util.List;

/**
 * product_backpack 用户宠物背包
 *
 * @author
 */
@Data
@ToString
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class PetsBackpackDTO implements Serializable {

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

    private Integer parentId;

    private List<PetsBackpackDTO> children;


    private List<ProductBackpackDTO> list;

    private static final long serialVersionUID = 1L;
}