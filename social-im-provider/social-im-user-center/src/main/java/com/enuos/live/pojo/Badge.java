package com.enuos.live.pojo;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.io.Serializable;

/**
 * @Description 徽章
 * @Author wangyingjie
 * @Date 2020/9/17
 * @Modified
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Badge extends Page implements Serializable {

    private static final long serialVersionUID = 1441479132606940554L;

    /** 徽章编码 */
    private String code;

    /** 徽章名称 */
    private String title;

    /** 描述 */
    private String description;

    /** 徽章类别 */
    private Integer category;

    /** 徽章类型 */
    private Integer type;

    /** 徽章URL */
    private String iconUrl;

    /** 佩戴情况[-1 未获得 0 未佩戴 1 已佩戴] */
    private Integer isWear;

}
