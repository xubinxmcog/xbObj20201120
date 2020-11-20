package com.enuos.live.feign;

import com.enuos.live.feign.impl.ChatFeignFallback;
import com.enuos.live.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

/**
 * @Description 通知
 * @Author wangyingjie
 * @Date 9:09 2020/4/30
 * @Modified
 */
@Component
@FeignClient(value = "CHAT-CENTER", fallback = ChatFeignFallback.class)
public interface ChatFeign {

    /**
     * @Description: 发送通知
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
    @PostMapping(value = "/feign/notice/saveInteractNotice")
    Result sendNotice(@RequestBody Map<String, Object> params);

}
