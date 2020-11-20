package com.enuos.live.mapper;

import com.enuos.live.pojo.Topic;

import java.util.List;

/**
 * @Description 话题
 * @Author wangyingjie
 * @Date 2020/6/15
 * @Modified
 */
public interface TopicMapper {
    
    /**
     * @Description: 列表
     * @Param: [topic]
     * @Return: java.util.List<com.enuos.live.pojo.Topic>
     * @Author: wangyingjie
     * @Date: 2020/9/11
     */
    List<Topic> listWithPage(Topic topic);

    /**
     * @Description: 列表
     * @Param: []
     * @Return: java.util.List<com.enuos.live.pojo.Topic>
     * @Author: wangyingjie
     * @Date: 2020/9/11
     */
    List<Topic> list();

    /**
     * @Description: 话题信息
     * @Param: [topic]
     * @Return: com.enuos.live.pojo.Topic
     * @Author: wangyingjie
     * @Date: 2020/9/11
     */
    Topic info(Topic topic);
}
