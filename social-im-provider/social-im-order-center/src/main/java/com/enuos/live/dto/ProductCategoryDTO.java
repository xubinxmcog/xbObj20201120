package com.enuos.live.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.NotBlank;
import java.util.Date;

/**
 * @ClassName ProductDTO
 * @Description: TODO
 * @Author xubin
 * @Date 2020/4/3
 * @Version V1.0
 **/
@Data
public class ProductCategoryDTO {

    private Long id;

    /**
     * 分类名称
     */
    @NotBlank(message = "categoryName分类名称不能为空")
    private String categoryName;

    /**
     * 分类编码
     */
    @NotBlank(message = "categoryCode分类编码不能为空")
    private String categoryCode;

    /**
     * 父分类ID
     */
    private String parentId;

    /**
     * 分类层级
     */
    private String categoryLevel;

    /**
     * 分类状态 0：无效，1：有效
     */
    private String categoryStatus;

}
