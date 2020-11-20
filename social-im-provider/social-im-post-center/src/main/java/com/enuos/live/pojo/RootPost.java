package com.enuos.live.pojo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @Description 根动态
 * @Author wangyingjie
 * @Date 15:24 2020/4/28
 * @Modified
 */
@Data
public class RootPost extends Base implements Serializable {

    private static final long serialVersionUID = 6034262091401057667L;

    /** ID */
    private Integer id;

    /** 昵称 */
    private String nickName;

    /** 内容 */
    private String content;

    /** at映射关系 */
    private String atMap;

    /** 资源 */
    private List<Resource> resourceList;

}
