package com.enuos.live.pojo;

import lombok.Getter;
import lombok.Setter;

/**
 * @Description 记录
 * @Author wangyingjie
 * @Date 2020/10/12
 * @Modified
 */
@Getter
@Setter
public class WritRecord extends Base {

    /** 任务编码 */
    private String taskCode;

    /** 模板编码 */
    private String templateCode;

    /** 积分 */
    private Integer category;

    /** 后缀 */
    private Integer suffix;

    public WritRecord() {
        super();
    }

    public WritRecord(Long userId, String taskCode, String templateCode) {
        this.userId = userId;
        this.taskCode = taskCode;
        this.templateCode =  templateCode;
    }

    public WritRecord(Long userId, String taskCode, String templateCode, Integer category, Integer suffix) {
        this.userId = userId;
        this.taskCode = taskCode;
        this.templateCode = templateCode;
        this.category = category;
        this.suffix = suffix;
    }
}
