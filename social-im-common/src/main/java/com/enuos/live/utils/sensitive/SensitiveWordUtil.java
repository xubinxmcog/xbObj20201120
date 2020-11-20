package com.enuos.live.utils.sensitive;

/**
 * @author WangCaiWen
 * Created on 2020/3/2 14:41
 */
public class SensitiveWordUtil {

    public static String replaceSensitiveWord(String comments){
        // 敏感词过滤器
        SensitiveWordFilter filter = SensitiveWordFilter.getInstance();
        String tempWord = filter.replaceSensitiveWord(comments, SensitiveWordFilter.minMatchType, "*");
        if (tempWord != null){
            return tempWord;
        }
        return comments;
        // 是否存在敏感词
        //System.out.println(filter.checkSensitiveWord(comments, 0, SensitiveWordFilter.minMatchType))
        // 获取敏感词
        //System.out.println(filter.getSensitiveWord(comments, SensitiveWordFilter.minMatchType))
    }

    /**
     * 校验是否存在敏感词
     * @param comments
     * @return
     */
    public static boolean isExists(String comments) {
        SensitiveWordFilter filter = SensitiveWordFilter.getInstance();
        return filter.checkSensitiveWord(comments, 0, SensitiveWordFilter.minMatchType) > 0 ? true : false;
    }

}
