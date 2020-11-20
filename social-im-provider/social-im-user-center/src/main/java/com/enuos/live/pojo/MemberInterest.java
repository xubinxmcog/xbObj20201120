package com.enuos.live.pojo;

import lombok.Data;

import java.io.Serializable;

/**
 * @Description
 * @Author wangyingjie
 * @Date 2020/6/30
 * @Modified
 */
@Data
public class MemberInterest implements Serializable {

    private static final long serialVersionUID = -4250891567279185106L;

    /** 权益 */
    private String interest;

    /** 权益图标 */
    private String iconUrl;

    /** 权益样例 */
    private String exampleUrl;

    /** vips */
    private String vips;

}
