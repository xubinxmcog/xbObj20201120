package com.enuos.live.pojo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import org.apache.commons.lang.StringUtils;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Description 动态参数
 * @Author wangyingjie
 * @Date 10:57 2020/4/22
 * @Modified
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PostDetail extends Base implements Serializable {

    private static final long serialVersionUID = -2674027609163005537L;

    /** 主键 */
    private Integer id;

    /** 话题ID */
    private Integer topicId;

    /** 话题名称 */
    private String topicName;

    /** 头像 */
    private String iconUrl;

    /** 头像缩略图 */
    private String thumbIconUrl;

    /** 头像框 */
    private String iconFrame;

    /** 性别[1:男;2:女] */
    private Integer sex;

    /** 等级 */
    private Integer level;

    /** 文本内容 */
    private String content;

    /** 动态发布时间 */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    /** 昵称(若是好友关系则显示好友备注) */
    private String nickName;

    /** 是否好友关系[0:false;1:true] */
    private Integer isFriend;

    /** 好友备注 */
    private String remark;

    /** 是否点赞[0:false;1:true] */
    private Integer isPraise;

    /** 点赞数 */
    private Integer praiseNum;

    /** 评论数 */
    private Integer commentNum;

    /** 转发数 */
    private Integer forwardNum;

    /** 转发ID */
    private Integer rootId;

    /** 转发动态IDS */
    private String forwardIds;

    /** 转发用户与ID的映射关系[json泛型<nickName, userId>] */
    private String forwardMap;

    /** at用户与ID的映射关系[json泛型<nickName, userId>] */
    private String atMap;

    /** 转发动态IDS */
    private List<Integer> forwardIdList;

    /** 动态资源集 */
    private List<Resource> resourceList;

    /** 评论 */
    private List<PostComment> commentList;

    /** 根动态 */
    private RootPost rootPost;

    /**
     * 重写set方法
     * @param forwardIds
     */
    public void setForwardIds(String forwardIds) {
        this.forwardIds = forwardIds;
        if (StringUtils.isNotBlank(forwardIds)) {
            this.forwardIdList = Arrays.asList(forwardIds.split(",")).stream().map(fid -> Integer.valueOf(fid)).collect(Collectors.toList());
            this.rootId = forwardIdList.get(0);
        }
    }
}
