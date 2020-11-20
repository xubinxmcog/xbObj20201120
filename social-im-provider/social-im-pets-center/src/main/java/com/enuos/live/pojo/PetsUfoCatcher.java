package com.enuos.live.pojo;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.ToString;
import sun.rmi.server.InactiveGroupException;

import java.io.Serializable;
import java.util.Date;

/**
 * @ClassName PetsUfoCatcher
 * @Description: TODO 抽奖
 * @Author xubin
 * @Date 2020/9/27
 * @Version V2.0
 **/
@ToString
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PetsUfoCatcher implements Serializable {

    private Integer id;

    /**
     * 奖品编码
     */
    private String productCode;

    /**
     * 商品id
     */
    private Long productId;

    /**
     * 权重值
     */
    private Integer prizeWeigth;

    /**
     * 是否必出
     */
    private Integer isMust;

    /**
     * 奖品单位(填写具体个数或者天数)
     */
    private Integer unit;

    /**
     * 娃娃机编号
     */
    private Integer catcherId;

    /**
     * 最后修改时间
     */
    private Date modifiedTime;

    /**
     * 奖品名称
     */
    private String productName;

    /**
     * 图片链接
     */
    private String picUrl;

    /**
     * 分类ID
     */
    private Integer categoryId;

    /**
     * 类别使用属性 1:消耗品 2:装饰品
     */
    private Integer usingPro;
    private static final long serialVersionUID = 1532589841756828858L;
}
