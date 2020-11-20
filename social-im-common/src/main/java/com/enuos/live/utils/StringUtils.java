package com.enuos.live.utils;

import com.alibaba.fastjson.JSON;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.util.*;
import java.util.regex.Pattern;

/**
 * 字符串工具类
 * @author WangCaiWen
 * Created on 2020/3/10 16:14
 */
public class StringUtils {

    /**
     * 判断字符串是否为空
     *
     * @param str 待验证字符串
     * @return 如果字符串为null,"null"或者为空，都将返回true，否则返回false
     */
    public static boolean isEmpty(String str) {
        String strNull = "null";
        String strEmpty = "";
        return str == null || strEmpty.equals(str.trim()) || strNull.equalsIgnoreCase(str.trim());
    }

    /**
     * 清洗字符串
     *
     * @param str 待清洗的字符串
     * @param defaultValue 默认替换值
     * @return String
     */
    public static String clear(String str, String defaultValue) {
        if (isEmpty(str)) {
            return defaultValue;
        } else {
            return str.trim();
        }
    }

    /**
     * 判断端口号是否正确
     *
     * @param port 待验证字符串
     * @return true/false
     */
    public static boolean isPort(String port) {
        if (isEmpty(port)) {
            return false;
        }
        try {
            int i = Integer.parseInt(port);
            return i >= 0 && i <= 65535;
        } catch (Exception e) {
            // 非数字串
            return false;
        }
    }

    /**
     * 判断字符串是否可转换为Long类型
     *
     * @param longStr 待验证字符串
     * @return true/false
     */
    public static boolean isLong(String longStr) {
        if (isEmpty(longStr)) {
            return false;
        }
        try {
            Long.parseLong(longStr);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 判断字符串是否可转换为Integer类型
     *
     * @param intStr 待验证字符串
     * @return true/false
     */
    public static boolean isInteger(String intStr) {
        if (isEmpty(intStr)) {
            return false;
        }
        try {
            Integer.parseInt(intStr);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     *
     * 判断字符串是否可转换为float类型
     *
     * @param floatStr 待验证字符串
     * @return  true/false
     */
    public static boolean isFloat(String floatStr) {
        if (isEmpty(floatStr)) {
            return false;
        }
        try {
            Float.parseFloat(floatStr);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     *
     * 判断字符串是否可转换为Double类型
     *
     * @param doubleStr  待验证字符串
     * @return true/false
     */
    public static boolean isDouble(String doubleStr) {
        if (isEmpty(doubleStr)) {
            return false;
        }
        try {
            Double.parseDouble(doubleStr);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     *
     * 判断字符串是否可转换为日期类型
     *
     * @param dateStr 待验证字符串
     * @return true/false
     */
    public static boolean isDate(String dateStr) {
        if (isEmpty(dateStr)) {
            return false;
        }
        try {
            DateUtils.parse(dateStr, DateUtils.yyyy_MM_dd);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     *
     * 判断字符串是否可转换为日期时间类型
     *
     * @param dateTimeStr  待验证字符串
     * @return true/false
     */
    public static boolean isDateTime(String dateTimeStr) {
        if (isEmpty(dateTimeStr)) {
            return false;
        }
        try {
            DateUtils.parse(dateTimeStr, DateUtils.yyyy_MM_dd_HH_mm_ss);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     *
     * 判断字符串是否可转换为日期时间类型
     *
     * @param dateTimeStr 待验证字符串
     * @param formatterStr 日期时间格式化串
     * @return true/false
     */
    public static boolean isDateTime(String dateTimeStr, String formatterStr) {
        if (isEmpty(dateTimeStr)) {
            return false;
        }
        try {
            DateUtils.parse(dateTimeStr, formatterStr);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 判断字符串是否可转换为BigDecimal类型
     *
     * @param decimalStr 待验证字符串
     * @return true/false
     */
    public static boolean isDecimal(String decimalStr) {
        try {
            new BigDecimal(decimalStr);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * 将数值型字符串转换为数字
     *
     * @param intStr 待转换字符串
     * @return int
     */
    public static int intValue(String intStr) {
        if (isEmpty(intStr)) {
            return 0;
        }
        return Integer.parseInt(intStr);
    }

    /**
     * 将长整形字符串转换为长整形数值
     *
     * @param longStr 待转换字符串
     * @return  long
     */
    public static long longValue(String longStr) {
        return Long.parseLong(longStr);
    }

    /**
     *
     * 将float类型字符串转换为float类型
     *
     * @param floatStr 待转换字符串
     * @return Float
     */
    public static Float floatValue(String floatStr) {
        return Float.parseFloat(floatStr);
    }

    /**
     *
     * 将Double类型字符串转换为Double类型
     *
     * @param doubleStr 待转换字符串
     * @return Double
     */
    public static Double doubleValue(String doubleStr) {
        return Double.parseDouble(doubleStr);
    }

    /**
     * 将数值型字符串转换为数值型
     *
     * @param decimalStr 待转换字符串
     * @return BigDecimal
     */
    public static BigDecimal decimalValue(String decimalStr) {
        return new BigDecimal(decimalStr);
    }

    /**
     * 将table名称转换成类名称
     *
     * @param tableName 数据表名称
     * @return String
     */
    public static String table2Class(String tableName) {
        String className = tableName;
        String underline = "_";
        String tUnderline = "t_";
        // 去除两端空格
        className = className.trim();
        // 去除T_
        if (className.startsWith(tUnderline)) {
            className = className.substring(2);
        }
        // 将首字母转换成大写
        className = className.substring(0, 1)
                .toUpperCase() + className.substring(1);
        // 将下划线_后面的首字母转换为大写
        int index = className.indexOf(underline);
        while (index > -1) {
            String left = className.substring(0, index);
            String right = className.substring(index + 1);
            if (right.length() > 0) {
                right = right.substring(0, 1)
                        .toUpperCase() + right.substring(1);
            }
            className = left + right;
            index = className.indexOf(underline);
        }
        return className;
    }

    /**
     * 将数据表的字段名称转换成对应的类属性名称
     *
     * @param columnName 数据表字段名称
     * @return String
     */
    public static String column2Field(String columnName) {
        String fieldName = columnName;
        String underline = "_";
        // 去除两端空格
        fieldName = fieldName.trim();
        // 将下划线_后面的首字母转换为大写
        int index = fieldName.indexOf(underline);
        while (index > -1) {
            String left = fieldName.substring(0, index);
            String right = fieldName.substring(index + 1);
            if (right.length() > 0) {
                right = right.substring(0, 1)
                        .toUpperCase() + right.substring(1);
            }
            fieldName = left + right;
            index = fieldName.indexOf(underline);
        }
        return fieldName;
    }

    /**
     * 用于解决URL参数中文奇数乱码问题
     *
     * @param str  待转换字符串
     * @return String
     */
    public static String encodeURL(String str) {
        if (StringUtils.isNotEmpty(str)) {
            try {
                return URLEncoder.encode(str, "UTF-8");
            } catch (UnsupportedEncodingException e) {}
        }
        return str;
    }

    /**
     * 判断给定的字符串是否可以用作包名
     *
     * @return true/false
     */
    public static boolean isPackageNameCorrect(String pkgName) {
        if (isEmpty(pkgName)) {
            return true;
        }
        pkgName = pkgName.trim().toLowerCase();
        // 去除两端空格，并转换为小写格式
        // 包命名规则：可以取以下字符，且只能以字母或下划线开始，用"."分隔包名，且最后一个字符不能为"."
        String criterionStr1 = "abcdefghijklmnopqrstuvwxyz_";
        String criterionStr2 = "0123456789.";
        char[] chars = pkgName.toCharArray();
        for (char c: chars) {
            if (!criterionStr1.contains(String.valueOf(c)) && !criterionStr2.contains(String.valueOf(c))) {
                return false;
            }
        }
        // 判断首字符
        char c = pkgName.charAt(0);
        if (!criterionStr1.contains(String.valueOf(c))) {
            return false;
        }
        // 判断尾字符
        return !pkgName.endsWith(".");
    }

    /**
     * 判断字符是否是中文
     *
     * @param c 字符
     * @return  true/false
     */
    public static boolean isChinese(char c) {
        // 假定英文范围为0-127(ASCII),128-255(ASCII扩展)
        int enEnd = 255;
        return (int) c > enEnd;
    }

    /**
     * 判断是否是ASCII码
     *
     * @param c 字符
     * @return true/false
     */
    public static boolean isAscii(char c) {
        // ASCII码:0-127(每个字符占1字节)
        int begin = 0;
        int end = 127;
        int code = c;
        return code >= begin && code <= end;
    }


    /**
     * 判断是否是ASCII扩展码
     *
     * @param c 字符
     * @return  true/false
     */
    public static boolean isAsciiExtend(char c) {
        // ASCII扩展码:128-255(每个字符占2字节)
        int begin = 128;
        int end = 255;
        int code = c;
        return code >= begin && code <= end;
    }

    /**
     * 截取字符串
     *
     * @param str 待截取的字符串
     * @param len  截取长度(2个英文字符相当于一个中文字符)
     * @return String
     */
    public static String cutString(String str, int len) {
        if (StringUtils.isEmpty(str)) {
            return "";
        }
        int two = 2;
        if (len <= 0 || len * two >= length(str)) {
            // 字符长度未达到截断限制要求
            return str;
        }
        int totalLength = 0;
        StringBuilder result = new StringBuilder();
        char[] chars = str.toCharArray();
        for (char c: chars) {
            if (totalLength < len * 2) {
                if (isChinese(c)) {
                    if (totalLength + 2 <= len * 2) {
                        result.append(c);
                        totalLength += 2;
                    } else {
                        break;
                    }
                } else {
                    if (totalLength + 1 <= len * 2) {
                        result.append(c);
                        totalLength++;
                    } else {
                        break;
                    }
                }
            } else {
                break;
            }
        }
        result.append("...");
        return result.toString();
    }

    /**
     * 计算字符串长度(一个中文字符长度为2)
     *
     * @param str 待计算的字符串
     * @return int
     */
    public static int length(String str) {
        return length(str, 2);
    }

    /**
     *
     * 计算字符串长度
     *
     * @param str 字符段
     * @param size 一个中文算作的长度
     * @return int
     */
    public static int length(String str, int size) {
        int totalLength = 0;
        char[] chars = str.toCharArray();
        for (char c: chars) {
            if (isAsciiExtend(c)) {
                totalLength += 2;
            } else if (isChinese(c)) {
                totalLength += size;
            } else {
                totalLength++;
            }
        }
        return totalLength;
    }

    /**
     * 去除字符串中的HTML元素
     *
     * @param str 待处理的字符串
     * @return String
     */
    public static String removeHtmlTag(String str) {
        // 含html标签的字符串
        String htmlStr = str;
        String textStr = "";
        Pattern pScript;
        java.util.regex.Matcher mScript;
        Pattern pHtml;
        java.util.regex.Matcher mHtml;
        try {
            // 定义HTML标签的正则表达式
            String regExHtml = "<[^>]+>";
            // 定义script的正则表达式{或<script[^>]*?>[/s/S]*?<//script>
            String regExScript = "<[/s]*?script[^>]*?>[/s/S]*?<[/s]*?//[/s]*?script[/s]*?>";
            pScript = Pattern.compile(regExScript, Pattern.CASE_INSENSITIVE);
            mScript = pScript.matcher(htmlStr);
            // 过滤script标签
            htmlStr = mScript.replaceAll("");
            pHtml = Pattern.compile(regExHtml, Pattern.CASE_INSENSITIVE);
            mHtml = pHtml.matcher(htmlStr);
            // 过滤html标签
            htmlStr = mHtml.replaceAll("");
            textStr = htmlStr;
        } catch (Exception e) {
            System.err.println("Html2Text: " + e.getMessage());
        } finally {
            pScript = null;
            mScript = null;
            pHtml = null;
            mHtml = null;
        }
        // 返回文本字符串
        return textStr;
    }

    public static int lastIndexOf(String str1, String str2) {
        return str1.lastIndexOf(str2);
    }

    /**
     * 获取起始年月中的所有年月，例如输入201201和201205将返回201201,201202,201203,201204,201205
     *
     * @param minYearMonth 开始年月
     * @param maxYearMonth 结束年月
     * @return list
     */
    public static List< String > yearMonthRange(String minYearMonth, String maxYearMonth) {
        List < String > yearMonthRange = new ArrayList< String >();
        yearMonthRange.add(minYearMonth);
        while (true) {
            String nextYearMonth = DateUtils.format(
                    DateUtils.getMonth(DateUtils.parse(minYearMonth, DateUtils.yyyyMM), 1), DateUtils.yyyyMM);
            if (nextYearMonth.compareTo(maxYearMonth) <= 0) {
                minYearMonth = nextYearMonth;
                yearMonthRange.add(nextYearMonth);
            } else {
                break;
            }
        }
        return yearMonthRange;
    }

    /**
     * 获取连续的年月，以当前月为基数，间隔intervals月数
     *
     * @param intervals 间隔数
     * @return list
     */
    public static List < String > yearMonthRange(int intervals) {
        Date base = DateUtils.getCurrDate();
        if (intervals > 0) {
            return yearMonthRange(DateUtils.format(base, DateUtils.yyyyMM)
                    , DateUtils.format(DateUtils.getMonth(base, intervals), DateUtils.yyyyMM));
        } else {
            return yearMonthRange(DateUtils.format(DateUtils.getMonth(base, intervals), DateUtils.yyyyMM)
                    , DateUtils.format(base, DateUtils.yyyyMM));
        }
    }

    /**
     * 获取两个日期中间的所有日期
     *
     * @param minDay 最小日期
     * @param maxDay 最大日期
     * @param pattern 日期格式
     * @return List
     */
    public static List < String > dayRange(Date minDay, Date maxDay, String pattern) {
        String minDayStr = DateUtils.format(minDay, pattern);
        String maxDayStr = DateUtils.format(maxDay, pattern);
        List < String > dayRange = new ArrayList < String > ();
        dayRange.add(minDayStr);
        while (true) {
            Date nextDay = DateUtils.getDay(minDay, 1);
            minDayStr = DateUtils.format(nextDay, pattern);
            if (minDayStr.compareTo(maxDayStr) <= 0) {
                minDay = nextDay;
                dayRange.add(minDayStr);
            } else {
                break;
            }
        }
        return dayRange;
    }

    /**
     * 集合拼接
     *
     * @param list 集合
     * @param split 分隔符
     * @return String
     */
    public static String join(List < String > list, String split) {
        if (list.size() > 0) {
            StringBuilder result = new StringBuilder();
            for (String s: list) {
                result.append(s)
                        .append(",");
            }
            return result.substring(0, result.length() - 1);
        } else {
            return null;
        }
    }

    /**
     * 获取长日期字符串 将 yyyyMMddHHmmss 转换为 yyyy-MM-dd HH:mm:ss 格式
     *
     * @param shortDateStr 短日期字符窜
     * @return String
     */
    public static String toLongDateStr(String shortDateStr) {
        if (shortDateStr == null || StringUtils.isEmpty(shortDateStr)) {
            return "";
        }
        int index = 14;
        if (shortDateStr.length() < index) {
            return shortDateStr;
        }
        return shortDateStr.substring(0, 4) + "-" + shortDateStr.substring(4, 6) + "-" + shortDateStr.substring(6, 8) +
                " " + shortDateStr.substring(8, 10) + ":" + shortDateStr.substring(10, 12) + ":" +
                shortDateStr.substring(12, 14);
    }

    /**
     * 获取短日期字符串 将 yyyy-MM-dd HH:mm:ss 转换为 yyyyMMddHHmmss 格式
     *
     * @param longDateStr 短日期字符窜
     * @return String
     */
    public static String toShortDateStr(String longDateStr) {
        return longDateStr.replaceAll("-", "")
                .replaceAll(":", "")
                .replaceAll(" ", "");
    }

    /**
     * 多个对象字符串相加
     *
     * @param objs 对象数组
     * @return String
     */
    public static String toStrings(Object...objs) {
        StringBuilder str = new StringBuilder();
        for (Object obj: objs) {
            str.append(obj.toString());
        }
        return str.toString();
    }

    /**
     * 两个对象字符串相加
     *
     * @param obj1 对象1
     * @param obj2 对象2
     * @return String
     */
    public static String concat(Object obj1, Object obj2) {
        return toStrings(obj1, obj2);
    }

    /**
     *
     * 编码，类似javascript中的escape功能
     *
     * @param src 待转换字符串
     * @return String
     */
    public static String escape(String src) {
        int i;
        char j;
        StringBuilder temp = new StringBuilder();
        temp.ensureCapacity(src.length() * 6);
        for (i = 0; i < src.length(); i++) {
            j = src.charAt(i);
            if (Character.isDigit(j) || Character.isLowerCase(j) || Character.isUpperCase(j)){
                temp.append(j);}
            else if (j < 256) {
                temp.append("%");
                if (j < 16){
                    temp.append("0");}
                temp.append(Integer.toString(j, 16));
            } else {
                temp.append("%u");
                temp.append(Integer.toString(j, 16));
            }
        }
        return temp.toString();
    }

    /**
     *
     * 解码，类似javascript中的unescape功能
     *
     * @param src 待转换字符串
     * @return String
     */
    public static String unescape(String src) {
        StringBuilder temp = new StringBuilder();
        temp.ensureCapacity(src.length());
        int lastPos = 0, pos = 0;
        char ch;
        while (lastPos < src.length()) {
            pos = src.indexOf("%", lastPos);
            if (pos == lastPos) {
                if (src.charAt(pos + 1) == 'u') {
                    ch = (char) Integer.parseInt(src.substring(pos + 2, pos + 6), 16);
                    temp.append(ch);
                    lastPos = pos + 6;
                } else {
                    ch = (char) Integer.parseInt(src.substring(pos + 1, pos + 3), 16);
                    temp.append(ch);
                    lastPos = pos + 3;
                }
            } else {
                if (pos == -1) {
                    temp.append(src.substring(lastPos));
                    lastPos = src.length();
                } else {
                    temp.append(src.substring(lastPos, pos));
                    lastPos = pos;
                }
            }
        }
        return temp.toString();
    }

    /**
     * 判断字符串是否不为空
     */
    public static boolean isNotEmpty(String str) {
        return !isEmpty(str);
    }

    /**
     * 获取UUID
     *
     * @return String
     */
    public static String getUUID() {
        return UUID.randomUUID()
                .toString();
    }
    public static String getSimpleUUID() {
        return getUUID()
                .replace("-", "");
    }

    /**
     *
     * 解决跨域easyUi form提交解析
     * form组件提交是通过iframe来模拟实现的，当在页面中增加document.domain解决跨域js交互问题后，造成easyUi
     * form组件提交后无法正常解析返回结果，为此需要将原先直接返回的json数据包装成html进行返回〉
     *
     * @param data 待解析数据
     * @return String
     */
    public static String json2HtmlFixCrossDomain(Object data) {
        String template = "<html><head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\"/><script type=\"text/javascript\">document.domain='mychebao.com';</script></head><body>%s</body></html>";
        if (data instanceof String) {
            if (data != null && !data.toString()
                    .startsWith("<html>")) {
                return String.format(template, data);
            } else {
                return data.toString();
            }
        } else {
            return String.format(template, JSON.toJSONString(data));
        }
    }

    // 随机生成密码
    public static String genRandomNum(int pwdLen) {
        // 35是因为数组是从0开始的，26个字母+10个数字
        // 生成的随机数
        int i;
        // 生成的密码的长度
        int count = 0;
        char[] str = {
                'a'
                , 'b'
                , 'c'
                , 'd'
                , 'e'
                , 'f'
                , 'g'
                , 'h'
                , 'i'
                , 'j'
                , 'k'
                , 'l'
                , 'm'
                , 'n'
                , 'o'
                , 'p'
                , 'q'
                , 'r'
                , 's'
                , 't'
                , 'u'
                , 'v'
                , 'w'
                , 'x'
                , 'y'
                , 'z'
                , '0'
                , '1'
                , '2'
                , '3'
                , '4'
                , '5'
                , '6'
                , '7'
                , '8'
                , '9'
        };
        StringBuilder pwd = new StringBuilder("");
        Random r = new Random();
        while (count < pwdLen) {
            // 生成随机数，取绝对值，防止生成负数，
            // 生成的数最大为36-1
            i = Math.abs(r.nextInt(str.length));
            if (i >= 0 && i < str.length) {
                pwd.append(str[i]);
                count++;
            }
        }
        return pwd.toString();
    }
    /**
     *
     * 判断字符串是否符合电子邮件格式
     *
     * @param email 电子邮件
     * @return true/false
     */
    public static boolean isEmail(String email) {
        if (StringUtils.isEmpty(email)) {
            return false;
        }
        String regex = "^[a-zA-Z]([a-zA-Z0-9]|[-_])*@[a-zA-Z0-9]+.[a-zA-z]+";
        return email.matches(regex);
    }

    /**
     * 判断字符串是否符合电子邮件前缀格式
     *
     * @param emailPrefix 电子邮件前缀
     * @return true/false
     */
    public static boolean isEmailPrefix(String emailPrefix) {
        // 正则语法[字母开头，包含字母，数字，下划线和中划线-_]
        String regex = "^[a-zA-Z]([a-zA-Z0-9]|[-_])*";
        return emailPrefix.matches(regex);
    }

    /**
     * 生成长度为captchaLength验证码 如果captchaLength小于或等于0，则长度为默认6
     *
     * @param captchaLength 验证码长度
     * @return String
     */
    public static String getCaptchaNum(int captchaLength) {
        if (captchaLength <= 0) {
            captchaLength = 6;
        }
        StringBuffer sf = new StringBuffer();
        String[] array = {
                "0"
                , "1"
                , "2"
                , "3"
                , "4"
                , "5"
                , "6"
                , "7"
                , "8"
                , "9"
        };
        Random rand = new Random();
        for (int i = 10; i > 1; i--) {
            int index = rand.nextInt(i);
            String tmp = array[index];
            array[index] = array[i - 1];
            array[i - 1] = tmp;
        }
        for (int i = 0; i < captchaLength; i++){
            sf.append(array[i]);}
        return sf.toString();
    }

    /**
     * 首字母小写
     */
    public static String lowerChar0(String str) {
        if (StringUtils.isEmail(str)) {
            return null;
        }
        char[] chars = str.toCharArray();
        char char0 = chars[0];
        if (char0 >= 'A' && char0 <= 'Z') {
            chars[0] += 32;
        }
        return String.valueOf(chars);
    }

    /**
     * 首字母大写
     */
    public static String upperChar0(String str) {
        if (StringUtils.isEmail(str)) {
            return null;
        }
        char[] chars = str.toCharArray();
        char char0 = chars[0];
        if (char0 >= 'a' && char0 <= 'z') {
            chars[0] -= 32;
        }
        return String.valueOf(chars);
    }

    public static String valueOf(Object obj) {
        return (obj == null) ? "" : obj.toString();
    }

    public static String empty2Default(String str, String defaultValue) {
        return isEmpty(str) ? defaultValue : str;
    }

    public static String nullToEmpty(String s) {
        return s == null ? "" : s;
    }

    public static String nvl(Object object) {
        String retString = String.valueOf(object);
        if (isEmpty(retString)) {
            return "";
        }
        return retString;
    }

    public static boolean notEmptyForManyStr(String...strs) {
        boolean flag = true;
        if (strs == null || strs.length <= 0) {
            return false;
        }
        for (String str: strs) {
            if (isEmpty(str)) {
                flag = false;
                break;
            }
        }
        return flag;
    }

    public static boolean isEmptyForManyStr(String...str) {
        return !notEmptyForManyStr(str);
    }

    private static final Pattern PATTERN_PHONE = Pattern.compile("^((13[0-9])|(14[5,7,9])|(15([0-3]|[5-9]))|(166)|(17[0,1,3,5,6,7,8])|(18[0-9])|(19[8|9]))\\d{8}$");

    /**
     * @Description: 手机号规则校验
     * @Param: [phone]
     * @Return: boolean
     * @Author: wangyingjie
     * @Date: 2020/9/8
     */
    public static boolean isPhone(String phone) {
        if (isEmpty(phone)) {
            return false;
        }

        return PATTERN_PHONE.matcher(phone).matches();
    }

    /**
     * @Description: 遮盖
     * @Param: [str]
     * @Return: java.lang.String
     * @Author: wangyingjie
     * @Date: 2020/7/6
     */
    public static String hideString(String str) {
        return str.replaceAll("(?<=[\\w]{3})\\w(?=[\\w]{4})", "*");
    }

}
