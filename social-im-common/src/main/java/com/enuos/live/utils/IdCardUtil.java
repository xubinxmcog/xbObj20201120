package com.enuos.live.utils;

/**
 * @ClassName IdCardUtil
 * @Description: TODO 身份证合法性校验
 * @Author xubin
 * @Date 2020/5/13
 * @Version V1.0
 **/
public class IdCardUtil {

    /**
     * @Description TODO 校验身份证号码是否合法
     * @param idcard 身份证号码
     * @return boolean 错误返回false， 正确返回true
     * @author xubin
     * @date 2019年8月10日 09:24:56
     */
    public static boolean isIdcard(String idcard) {

        // 校验身份证号码是否为空
        if (StringUtils.isEmpty(idcard) || idcard.length() != 18) {
            return false;
        }
        // 匹配身份证号码的正则表达式
        if (!idcard.matches(REGEX_ID_NO_18)) {
            return false;
        }
        // 校验身份证号码的验证码
        if (!validateCheckNumber(idcard)) {
            return false;
        }
        return true;
    }

    private static boolean validateCheckNumber(String IDNo18) {
        // 加权因子
        int[] W = { 7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10, 5, 8, 4, 2 };
        char[] IDNoArray = IDNo18.toCharArray();
        int sum = 0;
        for (int i = 0; i < W.length; i++) {
            sum += Integer.parseInt(String.valueOf(IDNoArray[i])) * W[i];
        }
        // 校验位是X，则表示10
        if (IDNoArray[17] == 'X' || IDNoArray[17] == 'x') {
            sum += 10;
        } else {
            sum += Integer.parseInt(String.valueOf(IDNoArray[17]));
        }
        // 如果除11模1，则校验通过
        return sum % 11 == 1;
    }
    /**
     * 18位二代身份证号码的正则表达式
     */
    public static final String REGEX_ID_NO_18 = "^"
            + "\\d{6}" // 6位地区码
            + "(18|19|([23]\\d))\\d{2}" // 年YYYY
            + "((0[1-9])|(10|11|12))" // 月MM
            + "(([0-2][1-9])|10|20|30|31)" // 日DD
            + "\\d{3}" // 3位顺序码
            + "[0-9Xx]" // 校验码
            + "$";

    /**
     * 15位一代身份证号码的正则表达式
     */
    public static final String REGEX_ID_NO_15 = "^"
            + "\\d{6}" // 6位地区码
            + "\\d{2}" // 年YYYY
            + "((0[1-9])|(10|11|12))" // 月MM
            + "(([0-2][1-9])|10|20|30|31)" // 日DD
            + "\\d{3}"// 3位顺序码
            + "$";

}
