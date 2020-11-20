package com.enuos.live.pojo;

import lombok.Data;

import java.util.List;

/**
 * @Description 金秋送福[ACT0005]
 * @Author wangyingjie
 * @Date 2020/9/23
 * @Modified
 */
@Data
public class JinQiu extends Activity {

    /** 奖励 */
    private List<JackpotReward> list;

}
