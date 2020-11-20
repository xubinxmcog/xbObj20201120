package com.enuos.live.pojo;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * @Description
 * @Author wangyingjie
 * @Date 2020/6/30
 * @Modified
 */
@Data
public class MemberPackage extends Base implements Serializable {

    private static final long serialVersionUID = -8293720917910632108L;

    /** 套餐编码 */
    @NotBlank(message = "套餐编码不能为空")
    private String productId;
}
