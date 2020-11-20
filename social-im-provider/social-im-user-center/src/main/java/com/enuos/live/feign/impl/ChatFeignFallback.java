package com.enuos.live.feign.impl;

import com.enuos.live.feign.ChatFeign;
import com.enuos.live.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @Description
 * @Author wangyingjie
 * @Date 2020/9/15
 * @Modified
 */
@Slf4j
@Component
public class ChatFeignFallback implements ChatFeign {

    /**
     * @Description: 建立取消聊天关系[添加好友删除好友调用]
     * @Param: [params]
     * [source][通知来源]
     * [0:关注用户[sponsorId,sponsorName,sponsorIcon,receiverId]]
     * [1:评论动态[sponsorId,sponsorName,sponsorIcon,receiverId,storyId]]
     * [2:回复评论[sponsorId,sponsorName,sponsorIcon,receiverId,storyId,attachId]]
     * [3:转发动态[sponsorId,sponsorName,sponsorIcon,receiverId,storyId]]
     * [4:点赞动态[sponsorId,sponsorName,sponsorIcon,receiverId,storyId]]
     * [5:点赞评论[sponsorId,sponsorName,sponsorIcon,receiverId,storyId,attachId]]
     * [6:@用户[sponsorId,sponsorName,sponsorIcon,userIds]]
     * [sponsorId][发起人ID]
     * [sponsorName][发起人昵称]
     * [sponsorIcon][发起人头像]
     * [receiverId][接收人ID]
     * [storyId][动态ID]
     * [attachId][附属ID(评论ID)]
     * [userIds][用户ID列表]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/9/15
     */
    @Override
    public Result sendNotice(Map<String, Object> params) {
        log.error("==========[SOCIAL-IM-USER ChatFeign sendNotice error, params={}]", params);
        return null;
    }

    /**
     * @Description: 建立取消聊天关系[添加好友删除好友调用]
     * @Param: [params] userId  targetId  action (0 建立 1 取消)
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/9/15
     */
    @Override
    public Result buildRelationships(Map<String, Object> params) {
        log.error("==========[SOCIAL-IM-USER ChatFeign buildRelationships error, params={}]", params);
        return null;
    }

    /**
     * @Description: 聊天展示[拉黑取消拉黑调用]
     * @Param: [params] userId  targetId  show (0展示 1关闭)
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/9/15
     */
    @Override
    public Result updateExhibition(Map<String, Object> params) {
        log.error("==========[SOCIAL-IM-USER ChatFeign updateExhibition error, params={}]", params);
        return null;
    }

    /**
     * @Description: 注销时删除数据
     * @Param: [userId]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/9/15
     */
    @Override
    public Result logoutToDeleteChat(Long userId) {
        log.error("==========[SOCIAL-IM-USER ChatFeign logoutToDeleteChat error, userId={}]", userId);
        return null;
    }

    /**
     * @Description: 获取群聊
     * @Param: [params]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/9/15
     */
    @Override
    public Result searchGroup(Map<String, Object> params) {
        log.error("==========[SOCIAL-IM-USER ChatFeign searchGroup error, params={}]", params);
        return Result.success();
    }
}
