package com.enuos.live.utils;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * @author WangCaiWen
 * Created on 2019/10/21 13:42
 */
public class ExceptionUtil {

    public static String getStackTrace(Exception e){
        StringWriter sw = new StringWriter();
        try (PrintWriter pw = new PrintWriter(sw)) {
            e.printStackTrace(pw);
            return sw.toString();
        }

    }
}
