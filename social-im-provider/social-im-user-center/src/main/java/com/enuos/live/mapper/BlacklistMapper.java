package com.enuos.live.mapper;

import com.enuos.live.pojo.Blacklist;

import java.util.List;
import java.util.Map;

/**
 * @Description 黑名单
 * @Author wangyingjie
 * @Date 9:24 2020/4/14
 * @Modified
 */
public interface BlacklistMapper {

    /**
     * @Description: 黑名单/屏蔽单（不看某人动态）
     * @Param: [blacklist]
     * @Return: java.util.List<java.util.Map<java.lang.String,java.lang.Object>>
     * @Author: wangyingjie
     * @Date: 2020/7/8
     */
    List<Map<String, Object>> getBlacklist(Blacklist blacklist);

    /**
     * @Description: 是否存在黑名单关系
     * @Param: [blacklist]
     * @Return: int
     * @Author: wangyingjie
     * @Date: 2020/7/8
     */
    Integer isExists(Blacklist blacklist);

    /**
     * @Description: 拉黑/屏蔽某人动态
     * @Param: [blacklist]
     * @Return: int
     * @Author: wangyingjie
     * @Date: 2020/7/8
     */
    int save(Blacklist blacklist);

    /** 
     * @Description: 解除黑名单/屏蔽
     * @Param: [blacklist]
     * @Return: int 
     * @Author: wangyingjie
     * @Date: 2020/7/8 
     */ 
    int delete(Blacklist blacklist);

}
