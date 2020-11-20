package com.enuos.live.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 语音房用户活跃度
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RoomUserActivityNote {

    int activityType();//活跃类型: 1:聊天 2:上麦

}
