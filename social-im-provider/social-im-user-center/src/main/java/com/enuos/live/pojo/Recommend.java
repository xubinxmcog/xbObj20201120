package com.enuos.live.pojo;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.io.Serializable;

/**
 * @Description 推荐用户
 * @Author wangyingjie
 * @Date 10:01 2020/4/29
 * @Modified
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Recommend extends Page implements Serializable {

    private static final long serialVersionUID = -6037126457193750796L;

    /** 昵称 */
    private String nickName;

    /** 头像 */
    private String thumbIconUrl;

    /** 性别 */
    private Integer sex;

    /** 等级 */
    private Integer level;

}
