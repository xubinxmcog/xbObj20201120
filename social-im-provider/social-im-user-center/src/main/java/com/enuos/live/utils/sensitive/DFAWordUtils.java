package com.enuos.live.utils.sensitive;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.*;
import java.util.*;

/**
 * @Description DFA算法过滤敏感词
 * @Author wangyingjie
 * @Date 2020/7/23
 * @Modified
 */
@Slf4j
@Component
public class DFAWordUtils {

    /** 编码格式 */
    private static final String ENCODING = "utf8";

    /** 敏感词库 */
    private static Map WORD_REPOSITORY;

    /** 最小匹配粒度 */
    private static int DEFAULT_MATCH = 2;

    /** 
     * @Description: 初始化敏感词库
     * @Param: [] 
     * @Return: void 
     * @Author: wangyingjie
     * @Date: 2020/7/23 
     */ 
    @PostConstruct
    private void init() {
        log.info("==========[DFAWordUtils init start]");
        Set<String> set = new HashSet<>();
        try {
            // 编译jar可识别地址resources.filter.filter.txt
            InputStream inputStream = DFAWordUtils.class.getResourceAsStream("/filter/filter.txt");
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, ENCODING));
            String txt;
            while ((txt = bufferedReader.readLine()) != null) {
                set.add(txt);
            }

            if (CollectionUtils.isEmpty(set)) {
                log.info("==========[DFAWordUtils init end, message [set is empty]]");
            }

            // 初始化词库
            WORD_REPOSITORY = new HashMap(set.size());
            Object obj;
            Map map, newMap;
            char key;
            int lastIndex;
            // 完善词库节点
            for (String str : set) {
                lastIndex = str.length() - 1;
                map = WORD_REPOSITORY;
                for (int i = 0; i < str.length(); i++) {
                    key = str.charAt(i);
                    // UTF8+BOM文件格式读取的第一个字符（i = 0）的ASCII码为65279，显示为空格
                    if ((int) key == 65279) {
                        continue;
                    }
                    // 该词在词库中是否有节点
                    obj = map.get(key);

                    if (obj == null) {
                        // 新建节点
                        newMap = new HashMap();
                        newMap.put("isEnd", i == lastIndex ? "1" : "0");
                        map.put(key, newMap);
                        map = newMap;
                    } else {
                        // 有节点则获取该节点下所有子节点
                        map = (Map)obj;
                    }
                }
            }
            log.info("==========[DFAWordUtils init end]");
        } catch (FileNotFoundException e) {
            log.error("==========[DFAWordUtils error, message [no file error {}]]", e);
        } catch (IOException e) {
            log.error("==========[DFAWordUtils error, message [read error {}]]", e);
        }
    }

    /**
     * @Description: 匹配敏感词返回匹配个数[单个匹配，有粒度]
     * @Param: [txt 敏感词, beginIndex 开始, minMatch 至少匹配]
     * @Return: int
     * @Author: wangyingjie
     * @Date: 2020/7/23
     */
    public int matchWords(String txt, int beginIndex, int minMatch) {
        int count = 0;
        char word;
        Map map = WORD_REPOSITORY;

        for (int i = beginIndex; i < txt.length() ; i++) {
            word = txt.charAt(i);
            map = (Map) map.get(word);
            if (map != null) {
                // 统计匹配的词的个数
                count++;
                if ("1".equals(MapUtils.getString(map, "isEnd"))) {
                    break;
                }
            } else {
                break;
            }
        }

        // 低于最少匹配则不敏感
        if (count < minMatch) {
            count = 0;
        }

        return count;
    }

    /**
     * @Description: 匹配敏感词返回是否匹配[全匹配]
     * @Param: [txt]
     * @Return: boolean
     * @Author: wangyingjie
     * @Date: 2020/7/24
     */
    public boolean matchWords(String txt) {
        if (StringUtils.isBlank(txt)) {
            return false;
        }

        boolean flag = false;

        char word;

        Map map = WORD_REPOSITORY;

        for (int i = 0; i < txt.length(); i++) {
            word = txt.charAt(i);
            map = (Map) map.get(word);

            if (map != null) {
                // 词库中存在该词
                if ("1".equals(MapUtils.getString(map, "isEnd"))) {
                    flag = true;
                }
            } else {
                break;
            }
        }

        return flag;
    }

    /** 
     * @Description: 获取敏感词
     * @Param: [txt, minMatch]
     * @Return: java.util.Set<java.lang.String> 
     * @Author: wangyingjie
     * @Date: 2020/7/23 
     */ 
    public Set<String> getWords(String txt, int minMatch) {
        Set<String> sets = new HashSet<>();
        int length;
        for (int i = 0; i < txt.length(); i++) {
            length = matchWords(txt, i,  minMatch);
            if (length > 0) {
                sets.add(txt.substring(i, i + length));
            }
        }
        return sets;
    }

    /** 
     * @Description: 替换敏感词
     * @Param: [txt, minMatch, replacement]
     * @Return: java.lang.String 
     * @Author: wangyingjie
     * @Date: 2020/7/23 
     */ 
    public String replaceWords(String txt, int minMatch, char replacement) {
        Set<String> sets = getWords(txt, minMatch);
        Set<Character> characterSet = new HashSet<>();

        for (String s : sets) {
            for (int i = 0; i < s.length(); i++) {
                characterSet.add(s.charAt(i));
            }
        }

        for (char c : characterSet) {
            txt = txt.replace(c, replacement);
        }

        return txt;
    }

    /** 
     * @Description: 替换敏感词
     * @Param: [txt, minMatch]
     * @Return: java.lang.String 
     * @Author: wangyingjie
     * @Date: 2020/7/23 
     */ 
    public String replaceWords(String txt, int minMatch) {
        return replaceWords(txt, minMatch, '*');
    }

    /**
     * @Description: 是否存在敏感词
     * @Param: [txt, minMatch]
     * @Return: boolean 
     * @Author: wangyingjie
     * @Date: 2020/7/23 
     */ 
    public boolean isExists(String txt, int minMatch) {
        return StringUtils.isBlank(txt) ? false : matchWords(txt, 0, minMatch) > 0 ? true : false;
    }

    /** 
     * @Description: 是否存在敏感词
     * @Param: [txt] 
     * @Return: boolean 
     * @Author: wangyingjie
     * @Date: 2020/7/23 
     */ 
    public boolean isExists(String txt) {
        return StringUtils.isBlank(txt) ? false : matchWords(txt, 0, DEFAULT_MATCH) > 0 ? true : false;
    }

}