package com.enuos.live.pojo;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * @Description 花名册
 * @Author wangyingjie
 * @Date 13:28 2020/4/3
 * @Modified
 */
@Data
public class Friend extends Base implements Serializable {

    private static final long serialVersionUID = 569502764381440678L;

    /** 好友ID */
    @NotNull(message = "好友ID不能为空")
    private Long friendId;

    /** 好友昵称 */
    private String nickName;

    /** 好友备注 */
    private String remark;

    /** 是否刪除[0：否；1：是] */
    private Integer isDel;

    /** 交友方式[1：userId添加；2：扫码] */
    private Integer type;

}
