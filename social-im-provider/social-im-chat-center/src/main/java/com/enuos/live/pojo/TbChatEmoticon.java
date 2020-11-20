package com.enuos.live.pojo;

import java.io.Serializable;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

/**
 * tb_chat_emoticon
 *
 * @author
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TbChatEmoticon implements Serializable {
//    private Integer id;

    /**
     * 分类ID
     */
    private Integer categoryId;

    /**
     * 名称
     */
    private String emName;

    /**
     * 表情图片连接
     */
    private String emUrl;

    /**
     * 动态图片URL
     */
    private String animUrl;

    /**
     * 排序 : 数字越小,排名越靠前
     */
    private Integer sort;

    /**
     * 创建时间
     */
//    private Date createTime;

    /**
     * 更新时间
     */
//    private Date updateTime;

    private static final long serialVersionUID = 1L;
}