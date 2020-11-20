package com.enuos.live.pojo;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * @Description 令状用户信息
 * @Author wangyingjie
 * @Date 2020/10/10
 * @Modified
 */
@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class WritUser extends Base implements Serializable {

    private static final long serialVersionUID = -8525433904148105451L;

    /** 任务编码 */
    private String taskCode;

    /** 解锁进阶[1：解锁进阶；2：解锁至尊进阶] */
    private Integer step;

    /** 乐享令状等级 */
    private Integer level;

    /** 积分 */
    private Integer integral;

    /** 积分线 */
    private Integer line;

    /** 乐享券 */
    private Integer ticket;

    /** 昵称 */
    private String nickName;

    /** 头像 */
    private String iconUrl;

    /** 头像缩略图 */
    private String thumbIconUrl;

    /** 规则说明 */
    private String ruleUrl;

    public WritUser() {
        super();
    }

    public WritUser(Long userId, String taskCode, Integer ticket) {
        this.userId = userId;
        this.taskCode = taskCode;
        this.ticket = ticket;
    }

    public WritUser(Long userId, String taskCode, Integer level, Integer integral) {
        this.userId = userId;
        this.taskCode = taskCode;
        this.level = level;
        this.integral = integral;
    }

    public WritUser(Long userId, String taskCode, Integer step, Integer level, Integer integral) {
        this.userId = userId;
        this.taskCode = taskCode;
        this.step = step;
        this.level = level;
        this.integral = integral;
    }
}