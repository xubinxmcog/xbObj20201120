package com.enuos.live.service;

import com.enuos.live.pojo.Comment;
import com.enuos.live.pojo.Post;
import com.enuos.live.pojo.Praise;
import com.enuos.live.pojo.Shield;
import com.enuos.live.result.Result;

/**
 * @Description 用户动态业务接口
 * @Author wangyingjie
 * @Date 15:22 2020/3/27
 * @Modified
 */
public interface PostService {

    /**
     * @Description: 发布动态
     * @Param: [post]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/9/11
     */
    Result createPost(Post post);

    /**
     * @Description: 删除动态
     * @Param: [post]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/9/11
     */
    Result deletePost(Post post);
    
    /** 
     * @Description: 删除评论或回复
     * @Param: [comment] 
     * @Return: com.enuos.live.result.Result 
     * @Author: wangyingjie
     * @Date: 2020/9/27 
     */ 
    Result deleteCommentAndReply(Comment comment);

    /**
     * @Description: 动态列表[0:我的;1:好友;2:关注;3:广场;4:话题动态]
     * @Param: [post]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/9/11
     */
    Result getMoments(Post post);

    /**
     * @Description: 获取动态
     * @Param: [post]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/9/11
     */
    Result getPost(Post post);

    /**
     * @Description: 评论列表
     * @Param: [post]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/9/11
     */
    Result getComment(Post post);

    /**
     * @Description: 转发
     * @Param: [post]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/9/11
     */
    Result getForward(Post post);

    /**
     * @Description: 回复列表
     * @Param: [comment]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/9/11
     */
    Result getReply(Comment comment);

    /**
     * @Description: 评论&回复
     * @Param: [comment]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/9/11
     */
    Result createCommentOrReply(Comment comment);

    /**
     * @Description: 点赞&取消点赞
     * @Param: [comment]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/9/11
     */
    Result giveOrCancelPraise(Praise comment);
    
    /**
     * @Description: 屏蔽动态
     * @Param: [shield]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/9/11
     */
    Result shield(Shield shield);
    
}
