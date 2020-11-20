package com.enuos.live.utils.encrypt;

import java.util.UUID;

/**
 * @author WangCaiWen
 * Created on 2020/3/19 14:18
 */
public class Token {

    public static String GetGUID(){
        return UUID.randomUUID().toString().replace("-", "");
    }
}
