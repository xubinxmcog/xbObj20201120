package com.enuos.live.utils;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.StringUtils;

import java.util.UUID;

/**
 * @Description 生成ID
 * @Author wangyingjie
 * @Date 2020/5/12
 * @Modified
 */
public class IDUtils {

    /**
     * 生成id
     * @param length
     * @return
     */
    public static Long randomId(int length) {
        StringBuffer sid = new StringBuffer(UUID.randomUUID().toString().replace("-", "").replaceAll("[a-zA-Z]", ""));
        if (StringUtils.startsWith(sid.toString(), "0")) {
            sid.replace(0,1,"1");
        }
        int rl = sid.length();
        return Long.valueOf(rl < length ? sid.append(RandomStringUtils.randomNumeric(length - rl)).toString() : sid.substring(0, length));
    }

}
