package com.enuos.live.pojo;

import lombok.*;

import java.io.Serializable;

/**
 * @Description
 * @Author wangyingjie
 * @Date 2020/5/12
 * @Modified
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RelationPO implements Serializable {

    private static final long serialVersionUID = 3822253382969743951L;

    private Long userId;

    /** from */
    private Long fromId;

    /** to */
    private Long toId;

    /** 关系[房间对用户：0 禁麦 1 拉黑 2 禁言；用户对房间：0 关注] */
    private Integer relation;

    /** 关注[0 取消关注 1 关注] */
    /** 禁麦[0 取消禁麦 1 禁麦] */
    /** 拉黑[0 取消拉黑 1 拉黑] */
    /** 禁言[0 解除禁言 1 1分钟 2 10分钟 3 1小时 4 7天] */
    private Integer type;

    public RelationPO(Long fromId, Long toId, Integer relation) {
        this.fromId = fromId;
        this.toId = toId;
        this.relation = relation;
    }
}
