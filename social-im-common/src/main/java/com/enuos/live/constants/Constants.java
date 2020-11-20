package com.enuos.live.constants;

/**
 * @MethodName:
 * @Description: TODO 常量
 * @Param: 
 * @Return: 
 * @Author: xubin
 * @Date: 2020/5/11
**/
public interface Constants {

    /**
     * 加密方式
     */
    String MD5 = "MD5";
    /**
     * 散列次数
     */
    int HASHITERATIONS = 1024;

    String KEY_ORDER_CATEGORY_LIST = "KEY_ORDER:CATEGORY_LIST"; // 订单中心：商品类别

    String KEY_MANAGE_CAUSELIST = "KEY_MANAGE:CAUSELIST"; // 管理中心：举报理由字典


    /**
     * 保存聊天文件前缀
     */
    String ROOM_CHAT = "room/chatFile/";
}
