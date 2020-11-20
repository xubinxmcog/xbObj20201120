package com.enuos.live.pojo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @Description 任务
 * @Author wangyingjie
 * @Date 2020/10/12
 * @Modified
 */
@Data
public class WritTask extends Base implements Serializable {

    private static final long serialVersionUID = 4045953790065346283L;

    /** 积分 */
    private Integer integral;

    /** 上限 */
    private Integer line;

    /** 阶[1：无；2：解锁进阶；3：解锁至尊进阶] */
    private Integer step;

    /** 任务列表 */
    private List<Task> taskList;

}
