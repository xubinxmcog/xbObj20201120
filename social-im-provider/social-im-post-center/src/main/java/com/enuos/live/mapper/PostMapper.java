package com.enuos.live.mapper;

import com.enuos.live.pojo.*;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @Description 用户动态dao接口
 * @Author wangyingjie
 * @Date 15:23 2020/3/27
 * @Modified
 */
public interface PostMapper {

    /**
     * @Description: 保存用户动态基础信息
     * @Param: [post]
     * @Return: int
     * @Author: wangyingjie
     * @Date: 2020/9/11
     */
    int savePost(Post post);

    /**
     * @Description: 获取数值
     * @Param: [id, table, column]
     * @Return: com.enuos.live.pojo.PostNum
     * @Author: wangyingjie
     * @Date: 2020/9/11
     */
    PostNum getNum(@Param("id") Integer id, @Param("table") String table, @Param("column") String column);

    /**
     * @Description: 设置数量
     * @Param: [postNum, table]
     * @Return: int
     * @Author: wangyingjie
     * @Date: 2020/9/11
     */
    int setNum(@Param("postNum") PostNum postNum, @Param("table") String table);
    
    /**
     * @Description: 动态是否存在
     * @Param: [praise]
     * @Return: java.lang.Integer
     * @Author: wangyingjie
     * @Date: 2020/9/11
     */
    Integer postIsExists(Praise praise);
    
    /**
     * @Description: 是否点赞
     * @Param: [praise]
     * @Return: java.lang.Integer
     * @Author: wangyingjie
     * @Date: 2020/9/11
     */
    Integer postIsPraise(Praise praise);

    /**
     * @Description: 是否点赞
     * @Param: [praise]
     * @Return: java.lang.Integer
     * @Author: wangyingjie
     * @Date: 2020/9/11
     */
    Integer commentIsPraise(Praise praise);

    /**
     * @Description: 批量保存动态资源如图片，音频，视频，链接
     * @Param: [post]
     * @Return: void
     * @Author: wangyingjie
     * @Date: 2020/9/11
     */
    void batchSaveResource(Post post);

    /**
     * @Description: 查询是否为自己的动态
     * @Param: [post]
     * @Return: java.lang.Integer
     * @Author: wangyingjie
     * @Date: 2020/9/11
     */
    Integer isSelfPost(Post post);

    /**
     * @Description: 查询是否为自己的评论或回复
     * @Param: [comment]
     * @Return: java.lang.Integer
     * @Author: wangyingjie
     * @Date: 2020/9/27
     */
    Integer isSelfCommentAndReply(Comment comment);

    /**
     * @Description: 删除动态
     * @Param: [tables, id]
     * @Return: int
     * @Author: wangyingjie
     * @Date: 2020/9/11
     */
    int deletePost(@Param("tables") String[] tables, @Param("id") Integer id);

    /** 
     * @Description: 删除评论或回复
     * @Param: [id] 
     * @Return: int 
     * @Author: wangyingjie
     * @Date: 2020/9/27 
     */ 
    int deleteCommentAndReply(Integer id);

    /**
     * @Description: 评论数-1
     * @Param: [id]
     * @Return: int
     * @Author: wangyingjie
     * @Date: 2020/9/27
     */
    int updateCommentNum(Integer id);

    /**
     * @Description: 获取
     * @Param: [id]
     * @Return: java.lang.Integer
     * @Author: wangyingjie
     * @Date: 2020/9/27
     */
    Integer getCommentIdBy(Integer id);

    /**
     * @Description: 回复数-1
     * @Param: [id]
     * @Return: int
     * @Author: wangyingjie
     * @Date: 2020/9/27
     */
    int updateReplyNum(Integer id);

    /**
     * @Description: 删除评论或回复的点赞记录
     * @Param: [id]
     * @Return: int
     * @Author: wangyingjie
     * @Date: 2020/9/27
     */
    int deleteCommentAndReplyPraise(Integer id);
    
    /**
     * @Description: 动态列表[我的动态]
     * @Param: [post]
     * @Return: java.util.List<com.enuos.live.pojo.PostDetail>
     * @Author: wangyingjie
     * @Date: 2020/9/11
     */
    List<PostDetail> getMyPost(Post post);

    /**
     * @Description: 动态列表[别人的动态]
     * @Param: [post]
     * @Return: java.util.List<com.enuos.live.pojo.PostDetail>
     * @Author: wangyingjie
     * @Date: 2020/9/11
     */
    List<PostDetail> getUserPost(Post post);

    /**
     * @Description: 动态列表[朋友圈]
     * @Param: [post]
     * @Return: java.util.List<com.enuos.live.pojo.PostDetail>
     * @Author: wangyingjie
     * @Date: 2020/9/11
     */
    List<PostDetail> getCircle(Post post);

    /**
     * @Description: 动态列表[关注]
     * @Param: [post]
     * @Return: java.util.List<com.enuos.live.pojo.PostDetail>
     * @Author: wangyingjie
     * @Date: 2020/9/11
     */
    List<PostDetail> getFollow(Post post);

    /**
     * @Description: 动态列表[广场]
     * @Param: [post]
     * @Return: java.util.List<com.enuos.live.pojo.PostDetail>
     * @Author: wangyingjie
     * @Date: 2020/9/11
     */
    List<PostDetail> getSquares(Post post);
    
    /**
     * @Description: 动态列表[话题]
     * @Param: [post]
     * @Return: java.util.List<com.enuos.live.pojo.PostDetail>
     * @Author: wangyingjie
     * @Date: 2020/9/11
     */
    List<PostDetail> getMomentsByTopic(Post post);

    /**
     * @Description: 获取动态
     * @Param: [post]
     * @Return: com.enuos.live.pojo.PostDetail
     * @Author: wangyingjie
     * @Date: 2020/9/11
     */
    PostDetail getPost(Post post);

    /**
     * @Description: 转发源动态信息
     * @Param: [idList]
     * @Return: java.util.List<com.enuos.live.pojo.RootPost>
     * @Author: wangyingjie
     * @Date: 2020/9/11
     */
    List<RootPost> getRootByIds(List<Integer> idList);

    /**
     * @Description: 转发源动态信息
     * @Param: [id]
     * @Return: com.enuos.live.pojo.RootPost
     * @Author: wangyingjie
     * @Date: 2020/9/11
     */
    RootPost getRootById(Integer id);

    /**
     * @Description: 动态资源
     * @Param: [postIdList]
     * @Return: java.util.List<com.enuos.live.pojo.PostDetail>
     * @Author: wangyingjie
     * @Date: 2020/9/11
     */
    List<PostDetail> getResource(List<Integer> postIdList);

    /**
     * @Description: 评论
     * @Param: [post]
     * @Return: java.util.List<com.enuos.live.pojo.Comment>
     * @Author: wangyingjie
     * @Date: 2020/9/11
     */
    List<Comment> getComment(Post post);

    /**
     * @Description: 获取动态的评论
     * @Param: [userId, postIdList]
     * @Return: java.util.List<com.enuos.live.pojo.PostComment>
     * @Author: wangyingjie
     * @Date: 2020/9/11
     */
    List<PostComment> getCommentByPostIdList(@Param("userId") Long userId, @Param("postIdList") List<Integer> postIdList);

    /**
     * @Description: 转发
     * @Param: [post]
     * @Return: java.util.List<com.enuos.live.pojo.Forward>
     * @Author: wangyingjie
     * @Date: 2020/9/11
     */
    List<Forward> getForward(Post post);

    /**
     * @Description: 评论的第一个回复
     * @Param: [userId, commentIdList]
     * @Return: java.util.List<com.enuos.live.pojo.Comment>
     * @Author: wangyingjie
     * @Date: 2020/9/11
     */
    List<Comment> getFirstReply(@Param("userId") Long userId, @Param("commentIdList") List<Integer> commentIdList);

    /**
     * @Description: 回复
     * @Param: [comment]
     * @Return: java.util.List<com.enuos.live.pojo.Reply>
     * @Author: wangyingjie
     * @Date: 2020/9/11
     */
    List<Reply> getReply(Comment comment);

    /**
     * @Description: 保存评论&回复
     * @Param: [comment]
     * @Return: int
     * @Author: wangyingjie
     * @Date: 2020/9/11
     */
    int saveCommentOrReply(Comment comment);

    /**
     * @Description: 评论是否有回复
     * @Param: [commentId]
     * @Return: int
     * @Author: wangyingjie
     * @Date: 2020/9/11
     */
    int isFirstReply(Integer commentId);

    /**
     * @Description: 动态点赞
     * @Param: [praise]
     * @Return: int
     * @Author: wangyingjie
     * @Date: 2020/9/11
     */
    int savePostPraise(Praise praise);

    /**
     * @Description: 评论&回复点赞
     * @Param: [praise]
     * @Return: int
     * @Author: wangyingjie
     * @Date: 2020/9/11
     */
    int saveCommentPraise(Praise praise);

    /**
     * @Description: 取消动态赞
     * @Param: [praise]
     * @Return: int
     * @Author: wangyingjie
     * @Date: 2020/9/11
     */
    int deletePostPraise(Praise praise);

    /**
     * @Description: 取消评论&回复赞
     * @Param: [praise]
     * @Return: int
     * @Author: wangyingjie
     * @Date: 2020/9/11
     */
    int deleteCommentPraise(Praise praise);

    /**
     * @Description: 获取动态或者评论或者回复的userId
     * @Param: [id, table]
     * @Return: int
     * @Author: wangyingjie
     * @Date: 2020/9/11
     */
    int getUserIdById(@Param("id") Integer id, @Param("table") String table);
    
    /**
     * @Description: 屏蔽动态
     * @Param: [shield]
     * @Return: int
     * @Author: wangyingjie
     * @Date: 2020/9/11
     */
    int saveShieldPost(Shield shield);

    /**
     * @Description: 获取点赞人和被点赞人是否好友关系
     * @Param: [praise]
     * @Return: java.lang.Integer
     * @Author: wangyingjie
     * @Date: 2020/9/11
     */
    Integer isFriend(Praise praise);
    
}
