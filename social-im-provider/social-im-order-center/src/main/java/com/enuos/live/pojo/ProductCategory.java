package com.enuos.live.pojo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.util.Date;

/**
 * @ClassName ProductCategory
 * @Description: TODO 商品分类
 * @Author xubin
 * @Date 2020/4/3
 * @Version V1.0
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class ProductCategory {

    private Long id;

    /**
     * 分类名称
     */
    @NotBlank(message = "categoryName分类名称不能为空")
    @ApiModelProperty(value = "分类名称",required = true)
    private String categoryName;

    /**
     * 分类编码
     */
    @NotBlank(message = "categoryCode分类编码不能为空")
    @ApiModelProperty(value = "分类编码",required = true)
    private String categoryCode;

    /**
     * 父分类ID
     */
    private Long parentId;

    /**
     * 父分类名称
     */
    private String parentName;

    /**
     * 分类层级
     */
    private String categoryLevel;

    /**
     * 分类状态 0：无效，1：有效
     */
    private String categoryStatus;

    /**
     * 类属性状态：1：消耗品 2：个人装饰品 3：其他
     */
    private Integer attributeStatus;

    /**
     * 创建时间
     */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd hh:mm:ss")
    private Date indateTime;

    /**
     * 修改时间
     */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd hh:mm:ss")
    private Date modifiedTime;
}
