package com.enuos.live.pojo;

import cn.hutool.json.JSONUtil;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @Description 朋友圈动态实体类
 * @Author wangyingjie
 * @Date 17:05 2020/3/27
 * @Modified
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Post extends Base implements Serializable {

    private static final long serialVersionUID = -3216072744898341123L;

    /** 主键 */
    private Integer id;

    /** 话题ID */
    private Integer topicId;

    /** 文本内容 */
    private String content;

    /** 展示状态 [0：所有人可见；1：仅好友可见；2：仅自己可见] */
    private Integer showStatus;

    /** 点赞数 */
    private Integer praiseNum ;

    /** 转发数 */
    private Integer forwardNum;

    /** 转发源动态ID */
    private List<Integer> forwardIdList;

    /** 转发源动态ID */
    private String forwardIds;

    /** 上一个转发人的动态ID */
    private Integer forwardId;

    /** 转发源动态ID */
    private Integer rootId;

    /** 用户ID */
    private Long toUserId;

    /** 动态发布时间 */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /** 动态资源集 */
    private List<Resource> resourceList;

    /** 转发用户与ID的映射关系[json]泛型<nickName, userId> */
    private String forwardMap;

    /** at用户与ID的映射关系[json]泛型<nickName, userId> */
    private String atMap;

    /** 其他参数 */
    /** 圈标记[1:好友;2:关注；3：广场] */
    private Integer moments;

    /** 页码 */
    private Integer pageNum;

    /** 每页条数 */
    private Integer pageSize;

    /**
     * 重写set方法
     * @param forwardIdList
     */
    public void setForwardIdList(List<Integer> forwardIdList) {
        this.forwardIdList = forwardIdList;
        if (CollectionUtils.isNotEmpty(forwardIdList)) {
            this.rootId = forwardIdList.get(0);
            this.forwardId = forwardIdList.get(forwardIdList.size() - 1);
            this.forwardIds = StringUtils.join(forwardIdList, ",");
        }
    }

    /**
     * 获取转发动态的发布者
     * @return
     */
    public Long getForwardUser() {
        if (StringUtils.isNotBlank(this.forwardMap)) {
            List<Long> userIdList = Arrays.stream(JSONUtil.parseObj(this.forwardMap).values().toArray()).map(fUserId -> Long.valueOf(String.valueOf(fUserId))).collect(Collectors.toList());
            return userIdList.get(userIdList.size() - 1);
        }
        return null;
    }

    /**
     * 返回at用户
     * @return
     */
    public List<Long> getAtUser() {
        if (StringUtils.isNotBlank(this.atMap)) {
            return JSONUtil.parseObj(this.atMap).values().stream().map(atUserId ->Long.valueOf(String.valueOf(atUserId))).collect(Collectors.toList());
        }
        return null;
    }

    /**
     * 返回
     * @return
     */
    public boolean isSelf() {
        return Objects.equals(this.userId, this.toUserId);
    }

}
