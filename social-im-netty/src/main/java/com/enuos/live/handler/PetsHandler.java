package com.enuos.live.handler;

import com.enuos.live.core.Packet;
import io.netty.channel.Channel;

/**
 * @MethodName:
 * @Description: TODO 宠物接口
 * @Param:        
 * @Return: 
 * @Author: xubin
 * @Date: 12:42 2020/8/24
**/
public interface PetsHandler {

    /**
     * @MethodName: play
     * @Description: TODO 进入游戏
     * @Param: [channel, packet]
     * @Return: void
     * @Author: xubin
     * @Date: 13:14 2020/8/24
    **/
    void play(Channel channel, Packet packet);

    /**
     * @MethodName: operation
     * @Description: TODO 操作
     * @Param: [channel, packet]
     * @Return: void
     * @Author: xubin
     * @Date: 17:06 2020/8/24
    **/
    void operation(Channel channel, Packet packet);

    /**
     * @MethodName: food
     * @Description: TODO 喂食
     * @Param: [channel, packet]       
     * @Return: void
     * @Author: xubin
     * @Date: 17:29 2020/8/24
    **/
    void food(Channel channel, Packet packet);

    /**
     * @MethodName: toys
     * @Description: TODO 玩具
     * @Param: [channel, packet]
     * @Return: void
     * @Author: xubin
     * @Date: 17:34 2020/8/24
    **/
    void toys(Channel channel, Packet packet);



}
