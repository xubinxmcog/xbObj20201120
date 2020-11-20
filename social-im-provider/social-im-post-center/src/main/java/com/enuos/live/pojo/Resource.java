package com.enuos.live.pojo;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.io.Serializable;

/**
 * @Description 朋友圈动态资源类
 * @Author wangyingjie
 * @Date 17:31 2020/3/27
 * @Modified
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Resource implements Serializable {

    private static final long serialVersionUID = -468284149440705761L;

    /** 主键ID */
    private Integer id;

    /** 标题 */
    private String title;

    /** 说明 */
    private String description;

    /** 动态ID */
    private Integer postId;

    /** 资源类型 [0：图片；1：音频；2：视频] */
    private Integer resourceType;

    /** 时长[秒] */
    private Integer duration;

    /** 宽[像素] */
    private Integer width;

    /** 高[像素] */
    private Integer height;

    /** 文件ID */
    private Integer fileId;

    /** 资源路径 */
    private String url;

    /** 封面URL */
    private String coverUrl;

    /** 缩略图 */
    private String thumbUrl;

    /** 链接 */
    private String link;

}
