package com.enuos.live.pojo;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * @Description 丹枫迎秋
 * @Author wangyingjie
 * @Date 2020/8/12
 * @Modified
 */
@Getter
@Setter
@NoArgsConstructor
public class QiuRi extends Activity {

    /** 奖章 */
    private Integer current;

    /** 奖池计数 */
    private Integer count;

    /** 钻石兑换轮数 */
    private Integer progress;

    /** 奖池 */
    private Jackpot jackpot;

    /** 活动任务 */
    private List<QiuRiTask> taskList;

    public QiuRi(Long userId, String code) {
        this.userId = userId;
        this.code = code;
    }

    public QiuRi(Long userId, String code, Integer progress) {
        this.userId = userId;
        this.code = code;
        this.progress = progress;
    }
}