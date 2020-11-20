package com.enuos.live.service.impl;

import com.enuos.live.mapper.TopicMapper;
import com.enuos.live.pojo.Topic;
import com.enuos.live.result.Result;
import com.enuos.live.service.TopicService;
import com.enuos.live.utils.page.PageInfo;
import com.github.pagehelper.PageHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Description
 * @Author wangyingjie
 * @Date 2020/6/15
 * @Modified
 */
@Slf4j
@Service
public class TopicServiceImpl implements TopicService {

    @Autowired
    private TopicMapper topicMapper;

    /**
     * @Description: 话题列表
     * @Param: [topic]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/9/11
     */
    @Override
    public Result listWithPage(Topic topic) {
        PageHelper.startPage(topic.pageNum, topic.pageSize);
        List<Topic> topicList = topicMapper.listWithPage(topic);
        return Result.success(new PageInfo<>(topicList));
    }

    /**
     * @Description: 话题列表
     * @Param: [topic]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/6/16
     */
    @Override
    public Result list(Topic topic) {
        return Result.success(topicMapper.list());
    }

    /**
     * @Description: 话题信息
     * @Param: [topic]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/6/16
     */
    @Override
    public Result info(Topic topic) {
        return Result.success(topicMapper.info(topic));
    }

}
