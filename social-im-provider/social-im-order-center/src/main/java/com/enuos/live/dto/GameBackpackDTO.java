package com.enuos.live.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

/**
 * @ClassName GameBackpackDTO
 * @Description: TODO 游戏物品加载查询
 * @Author xubin
 * @Date 2020/6/9
 * @Version V1.0
 **/
@Data
public class GameBackpackDTO {
    private Integer id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 分类ID
     */
    private Integer categoryId;

    /**
     * 商品有效期限 -1：永久 0：过期的 其他为秒值
     */
    private Long timeLimit;

    /**
     * 创建时间
     */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    /**
     * 物品使用状态：0：无使用状态 1：未使用 2：使用中
     */
    private Integer productStatus;

    /**
     * 商品编码
     */
    private String productCode;
}
