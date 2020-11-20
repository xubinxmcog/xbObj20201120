package com.enuos.live.constants;

import com.enuos.live.manager.AchievementEnum;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Description
 * @Author wangyingjie
 * @Date 2020/9/15
 * @Modified
 */
public class TaskConstants {

    public static final List<Map<String, Object>> MEMBERLIST;

    static {
        MEMBERLIST = getMemberList();
    }

    private static List<Map<String, Object>> getMemberList() {
        return new ArrayList<Map<String, Object>>() {
            {
                add(new HashMap<String, Object>() {
                    {
                        put("code", AchievementEnum.AMT0051.getCode());
                        put("progress", 1);
                        put("isReset", 0);
                    }
                });
                add(new HashMap<String, Object>() {
                    {
                        put("code", AchievementEnum.AMT0052.getCode());
                        put("progress", 1);
                        put("isReset", 0);
                    }
                });
                add(new HashMap<String, Object>() {
                    {
                        put("code", AchievementEnum.AMT0053.getCode());
                        put("progress", 1);
                        put("isReset", 0);
                    }
                });
                add(new HashMap<String, Object>() {
                    {
                        put("code", AchievementEnum.AMT0054.getCode());
                        put("progress", 1);
                        put("isReset", 0);
                    }
                });
                add(new HashMap<String, Object>() {
                    {
                        put("code", AchievementEnum.AMT0055.getCode());
                        put("progress", 1);
                        put("isReset", 0);
                    }
                });
                add(new HashMap<String, Object>() {
                    {
                        put("code", AchievementEnum.AMT0056.getCode());
                        put("progress", 1);
                        put("isReset", 0);
                    }
                });
            }
        };
    }
}
