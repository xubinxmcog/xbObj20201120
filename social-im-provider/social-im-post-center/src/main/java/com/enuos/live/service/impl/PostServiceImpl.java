package com.enuos.live.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.enuos.live.error.ErrorCode;
import com.enuos.live.feign.ChatFeign;
import com.enuos.live.feign.ProducerFeign;
import com.enuos.live.feign.UserFeign;
import com.enuos.live.manager.AchievementEnum;
import com.enuos.live.mapper.PostMapper;
import com.enuos.live.pojo.*;
import com.enuos.live.result.Result;
import com.enuos.live.service.PostService;
import com.enuos.live.task.Task;
import com.enuos.live.task.TemplateEnum;
import com.enuos.live.utils.DateUtils;
import com.enuos.live.utils.IDUtils;
import com.enuos.live.utils.page.PageInfo;
import com.github.pagehelper.PageHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.text.MessageFormat;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @Description 用户动态业务实现
 * @Author wangyingjie
 * @Date 15:52 2020/3/27
 * @Modified
 */
@Slf4j
@Service
public class PostServiceImpl implements PostService {

    private static final String[] DELETE_TABLE = {"tb_post_shield", "tb_comment_praise", "tb_comment", "tb_post_praise", "tb_post_resource", "tb_post"};

    /** 表名 */
    private static final String[] TABLE = {"tb_post", "tb_comment"};

    /** 列名 */
    private static final String[] COLUMN = {"praise_num", "comment_num", "reply_num"};

    @Autowired
    private ChatFeign chatFeign;

    @Autowired
    private UserFeign userFeign;

    @Autowired
    private ProducerFeign producerFeign;

    @Autowired
    private PostMapper postMapper;

    @Resource
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;

    /**
     * @Description: 发布动态
     * @Param: [post]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/9/11
     */
    @Override
    @Transactional
    public Result createPost(Post post) {
        if (Objects.isNull(post)) {
            return Result.empty();
        }

        if (Optional.ofNullable(post.getContent()).orElse("").length() > 128) {
            return Result.error(ErrorCode.EXCEPTION_CODE, "发布内容不超过128字");
        }

        Long userId = post.userId;

        if (StringUtils.isNotBlank(post.getContent()) && userFeign.matchWords(post.getContent())) {
            return Result.error(ErrorCode.CONTENT_SENSITIVE);
        }

        // 保存动态基本信息
        int result = postMapper.savePost(post);

        // 保存动态资源信息如图片，音频，视频
        // 发送通知
        if (result > 0) {
            if (CollectionUtils.isNotEmpty(post.getResourceList())) {
                postMapper.batchSaveResource(post);
            }
            // 发通知
            sendNotice(post);
        }

        // 动态成就
        achievementHandlers(userId);

        // 记录日志
        // logPost(post.userId, MessageEnum.LOG_POST_CREATE.getTemplate(), "", post.getId());

        // TASK[TD]：累计发布{0}条及以上动态
        threadPoolTaskExecutor.submit(() -> userFeign.handlerOfDailyTask(new Task(TemplateEnum.P01, userId)));

        return Result.success();
    }

    /**
     * @Description: 删除动态
     * @Param: [post]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/9/11
     */
    @Override
    @Transactional
    public Result deletePost(Post post) {
        if (Objects.isNull(post)) {
            return Result.empty();
        }

        // 是否具有删除权限
        if (Objects.isNull(postMapper.isSelfPost(post))) {
            return Result.error(ErrorCode.NO_PERMISSION);
        }

        // 依次删除回复点赞，回复，评论点赞，评论，屏蔽，动态点赞，动态
        postMapper.deletePost(DELETE_TABLE, post.getId());

        return Result.success();
    }

    /**
     * @Description: 删除评论或回复
     * @Param: [comment]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/9/27
     */
    @Override
    @Transactional
    public Result deleteCommentAndReply(Comment comment) {
        if (Objects.isNull(comment)) {
            return Result.empty();
        }

        // 是否具有删除权限
        if (Objects.isNull(postMapper.isSelfCommentAndReply(comment))) {
            return Result.error(ErrorCode.NO_PERMISSION);
        }

        Integer id = comment.getId();
        Integer type = comment.getType();

        // 是评论还是回复
        if (type == 1) {
            postMapper.updateCommentNum(id);
        } else if (type == 2) {
            postMapper.updateReplyNum(id);
        } else {
            return Result.error(ErrorCode.DATA_ERROR);
        }

        // 删除评论及回复，删除点赞
        postMapper.deleteCommentAndReply(id);
        postMapper.deleteCommentAndReplyPraise(id);

        return Result.success();
    }

    /**
     * @Description: 动态列表[0:我的;1:好友;2:关注;3:广场;4:话题动态]
     * @Param: [post]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/9/11
     */
    @Override
    public Result getMoments(Post post) {
        if (Objects.isNull(post)) {
            return Result.empty();
        }

        PageHelper.startPage(post.getPageNum(), post.getPageSize());
        // 圈标记[0:我的;1:好友;2:关注;3:广场;4:话题动态]
        Integer moments = post.getMoments();

        List<PostDetail> postList = null;
        switch (moments) {
            case 0:
                postList = post.isSelf() ? postMapper.getMyPost(post) : postMapper.getUserPost(post);
                break;
            case 1:
                postList = postMapper.getCircle(post);
                break;
            case 2:
                postList = postMapper.getFollow(post);
                break;
            case 3:
                postList = postMapper.getSquares(post);
                break;
            case 4:
                postList = postMapper.getMomentsByTopic(post);
            default:
                break;
        }

        if (CollectionUtils.isEmpty(postList)) {
            return Result.success(new PageInfo<>(postList));
        }

        List<Integer> postIdList = postList.stream().map(PostDetail::getId).collect(Collectors.toList());

        // 获取动态的资源信息
        List<PostDetail> resourceList = postMapper.getResource(postIdList);

        // 获取评论，仅朋友圈展示评论
        List<PostComment> commentList = moments == 1 ? postMapper.getCommentByPostIdList(post.userId, postIdList) : Collections.EMPTY_LIST;
        Map<Integer, List<PostComment>> commentMap = Collections.EMPTY_MAP;
        if (CollectionUtils.isNotEmpty(commentList)) {
            commentMap = commentList.stream().collect(Collectors.groupingBy(PostComment::getPostId));
            commentMap.entrySet().forEach(a -> a.getValue().sort(Comparator.comparing(PostComment::getCreateTime).reversed()));
        }

        // 转换为map格式
        Map<Integer, List<com.enuos.live.pojo.Resource>> resourceMap = resourceList.stream().collect(Collectors.toMap(PostDetail::getId, PostDetail::getResourceList));
        // 获取源动态信息
        List<RootPost> rootList = postMapper.getRootByIds(postList.stream().filter(pt -> StringUtils.isNotBlank(pt.getForwardIds())).map(PostDetail::getRootId).collect(Collectors.toList()));

        // 循环合并动态信息，动态资源，源动态信息及资源
        for (PostDetail pt : postList) {
            pt.setResourceList(resourceMap.get(pt.getId()));
            pt.setCommentList(commentMap.containsKey(pt.getId()) ? commentMap.get(pt.getId()).size() > 5 ? commentMap.get(pt.getId()).subList(0, 5) : commentMap.get(pt.getId()) : Collections.EMPTY_LIST);
            if (CollectionUtils.isNotEmpty(rootList)) {
                rootList.forEach(root -> {
                    if (Objects.equals(pt.getRootId(), root.getId())) {
                        pt.setRootPost(root);
                    }
                });
            }
        }

        return Result.success(new PageInfo<>(postList));
    }

    /**
     * @Description: 获取动态
     * @Param: [post]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/9/11
     */
    @Override
    public Result getPost(Post post) {
        if (Objects.isNull(post)) {
            return Result.empty();
        }

        PostDetail postDetail = postMapper.getPost(post);
        if (postDetail == null) {
            return Result.error(ErrorCode.NO_DATA);
        }

        Integer rootId = postDetail.getRootId();

        if (!Objects.isNull(rootId)) {
            RootPost root = postMapper.getRootById(rootId);
            if (!Objects.isNull(root)) {
                postDetail.setRootPost(root);
            }
        }

        // 记录日志
        // logPost(post.userId, MessageEnum.LOG_POST_VIEW.getTemplate(), "", post.getId());

        return Result.success(postDetail);
    }

    /**
     * @Description: 评论列表
     * @Param: [post]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/9/11
     */
    @Override
    public Result getComment(Post post) {
        if (Objects.isNull(post)) {
            return Result.empty();
        }

        PageHelper.startPage(post.getPageNum(), post.getPageSize());

        List<Comment> commentList = postMapper.getComment(post);

        if (CollectionUtils.isEmpty(commentList)) {
            return Result.success();
        }

        List<Comment> firstReplyList = postMapper.getFirstReply(post.getUserId(), commentList.stream().map(Comment::getId).collect(Collectors.toList()));

        if (CollectionUtils.isNotEmpty(firstReplyList)) {
            Map<Integer, Comment> firstReplyMap = firstReplyList.stream().collect(Collectors.toMap(Comment::getId, Function.identity()));
            commentList.forEach(comment -> {
                if (firstReplyMap.containsKey(comment.getId())) {
                    comment.setReplyNickName(firstReplyMap.get(comment.getId()).getReplyNickName());
                    comment.setReplyRemark(firstReplyMap.get(comment.getId()).getReplyRemark());
                    comment.setReplyContent(firstReplyMap.get(comment.getId()).getReplyContent());
                }
            });
        }

        return Result.success(new PageInfo<>(commentList));
    }

    /**
     * @Description: 转发
     * @Param: [post]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/9/11
     */
    @Override
    public Result getForward(Post post) {
        if (Objects.isNull(post)) {
            return Result.empty();
        }

        PageHelper.startPage(post.getPageNum(), post.getPageSize());

        List<Forward> forwardList = postMapper.getForward(post);

        return Result.success(new PageInfo<>(forwardList));
    }

    /**
     * @Description: 回复列表
     * @Param: [comment]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/9/11
     */
    @Override
    public Result getReply(Comment comment) {
        if (Objects.isNull(comment)) {
            return Result.empty();
        }

        PageHelper.startPage(comment.getPageNum(), comment.getPageSize());
        List<Reply> replyList = postMapper.getReply(comment);

        return Result.success(new PageInfo<>(replyList));
    }

    /**
     * @Description: 评论&回复
     * @Param: [comment]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/9/11
     */
    @Override
    @Transactional
    public Result createCommentOrReply(Comment comment) {
        if (Objects.isNull(comment)) {
            return Result.empty();
        }

        if (Optional.ofNullable(comment.getContent()).orElse("").length() > 100) {
            return Result.error(ErrorCode.EXCEPTION_CODE, "评论内容不超过100字");
        }

        if (StringUtils.isBlank(comment.getContent())) {
            return Result.error(ErrorCode.CONTENT_EMPTY);
        } else if (userFeign.matchWords(comment.getContent())) {
            return Result.error(ErrorCode.CONTENT_SENSITIVE);
        }

        Integer commentId = comment.getCommentId();
        boolean isReply = !Objects.isNull(commentId);

        // 回复
        if (isReply) {
            // 是否第一回复
            if (postMapper.isFirstReply(commentId) == 0) {
                comment.setReplyFlag(1);
            }
        }

        int result = postMapper.saveCommentOrReply(comment);

        if (result > 0) {
            if (isReply) {
                setNum(commentId, TABLE[1], COLUMN[2], 1);
            } else {
                setNum(comment.getPostId(), TABLE[0], COLUMN[1], 1);
            }
            // 发通知
            sendNotice(comment);
        }

        return Result.success();
    }

    /**
     * @Description: 点赞&取消点赞
     * @Param: [praise]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/9/11
     */
    @Override
    @Transactional
    public Result giveOrCancelPraise(Praise praise) {
        if (Objects.isNull(praise)) {
            return Result.empty();
        }

        // 点赞或者取消赞[1:点赞;0:取消赞]
        int giveOrCancel = praise.getGiveOrCancel();
        // 点赞类型[0:动态点赞;1:评论点赞;2:回复点赞]
        Integer type = praise.getType();

        int result;
        if (type == 0) {
            // 动态点赞
            if (giveOrCancel == 1) {
                if (postMapper.postIsPraise(praise) != null) {
                    return Result.error(ErrorCode.PRAISE_YET);
                }
                result = postMapper.savePostPraise(praise);
                // 每日点赞任务达成
                taskHandler(praise);
            } else {
                result = postMapper.deletePostPraise(praise);
            }

            if (result > 0) {
                setNum(praise.getPostId(), TABLE[0], COLUMN[0], giveOrCancel);
            }
        } else {
            // 评论回复点赞
            Integer commentId = praise.getCommentId();
            if (Objects.isNull(commentId)) {
                return Result.error(ErrorCode.DATA_ERROR);
            }

            if (giveOrCancel == 1) {
                if (postMapper.commentIsPraise(praise) != null) {
                    return Result.error(ErrorCode.PRAISE_YET);
                }
                result = postMapper.saveCommentPraise(praise);
            } else {
                result = postMapper.deleteCommentPraise(praise);
            }

            Integer id = type == 1 ? commentId : praise.getReplyId();

            if (result > 0) {
                setNum(id, TABLE[1], COLUMN[0], giveOrCancel);
            }
        }

        if (giveOrCancel == 1) {
            sendNotice(praise);
        }

        return Result.success();
    }

    /**
     * @Description: 屏蔽动态
     * @Param: [shield]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/9/11
     */
    @Override
    @Transactional
    public Result shield(Shield shield) {
        if (Objects.isNull(shield) || Objects.isNull(shield.userId)) {
            return Result.empty();
        }

        int result = 0;

        // 屏蔽动态
        if (!Objects.isNull(shield.getPostId())) {
            result = postMapper.saveShieldPost(shield);
        }

        return result > 0 ? Result.success() : Result.error();
    }

    /** [OTHER] */

    /**
     * @Description: 设置值[有异常回滚]
     * @Param: [id, table, column, operator 操作符[+-] 0 减 1 加]
     * @Return: void
     * @Author: wangyingjie
     * @Date: 2020/9/11
     */
    public void setNum (Integer id, String table, String column, int operator) {
        PostNum postNum = postMapper.getNum(id, table, column);
        if (Objects.isNull(postNum)) {
            throw new RuntimeException("No post or comment") ;
        }
        // 累计保存
        switch (operator) {
            case 0:
                postNum.subtract(column);
                break;
            case 1:
                postNum.add(column);
                break;
            default:
                break;
        }
        postMapper.setNum(postNum, table);
    }

    /**
     * @Description: 成就处理
     * @Param: [userId]
     * @Return: void
     * @Author: wangyingjie
     * @Date: 2020/9/11
     */
    private void achievementHandlers(Long userId) {
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>() {
            {
                add(new HashMap<String, Object>() {
                    {
                        put("code", AchievementEnum.AMT0047.getCode());
                        put("progress", 1);
                        put("isReset", 0);
                    }
                });
                add(new HashMap<String, Object>() {
                    {
                        put("code", AchievementEnum.AMT0048.getCode());
                        put("progress", 1);
                        put("isReset", 0);
                    }
                });
            }
        };

        userFeign.achievementHandlers(new HashMap<String, Object>() {
            {
                put("userId", userId);
                put("list", list);
            }
        });
    }

    /**
     * @Description: 发送通知
     * @Param: [t]
     * @Return: void
     * @Author: wangyingjie
     * @Date: 2020/9/11
     */
    @Async
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public <T> void sendNotice(T t) {
        Map<String, Object> params = new HashMap<>();

        Map<String, Object> tMap = BeanUtil.beanToMap(t);
        Long userId = MapUtils.getLong(tMap, "userId");

        // 获取当前用户的昵称头像
        Map<String, Object> userBaseMap = userFeign.getUserBase(userId, "nickName", "iconUrl");
        String nickName = MapUtils.getString(userBaseMap, "nickName");
        String iconUrl = MapUtils.getString(userBaseMap, "iconUrl");

        params.put("sponsorId", userId);
        params.put("sponsorName", nickName);
        params.put("sponsorIcon", iconUrl);
        try {
            if (t instanceof Post) {
                // 转发&@
                Post post = (Post) t;

                if (IDUtils.isNotNull(post.getForwardId())) {
                    if (userId.equals(post.getForwardUser())) {
                        return;
                    }

                    // 转发
                    params.put("source", 3);
                    params.put("receiverId", post.getForwardUser());
                    params.put("storyId", post.getForwardId());
                    chatFeign.sendNotice(params);
                }

                if (StringUtils.isNotBlank(post.getAtMap())) {

                    if (post.getAtUser().contains(userId)) {
                        post.getAtUser().remove(userId);
                    }

                    // at
                    params.put("source", 6);
                    params.put("userIds", post.getAtUser());
                    params.put("storyId", post.getId());
                    chatFeign.sendNotice(params);
                }

            } else if (t instanceof Comment) {
                // 评论&回复
                Comment comment = (Comment) t;

                if (userId.equals(comment.getToUserId())) {
                    return;
                }

                params.put("receiverId", comment.getToUserId());

                if (IDUtils.isNull(comment.getCommentId())) {
                    // 评论
                    params.put("source", 1);
                    params.put("storyId", comment.getPostId());
                } else {
                    // 回复
                    params.put("source", 2);
                    params.put("storyId", comment.getPostId());
                    params.put("attachId", comment.getCommentId());
                }
                chatFeign.sendNotice(params);
            } else if (t instanceof Praise) {
                // 点赞评论&点赞回复
                Praise praise = (Praise) t;

                if (userId.equals(praise.getToUserId())) {
                    return;
                }

                params.put("receiverId", praise.getToUserId());

                if (IDUtils.isNull(praise.getCommentId())) {
                    // 动态点赞
                    params.put("source", 4);
                    params.put("storyId", praise.getPostId());
                } else {
                    params.put("storyId", praise.getPostId());
                    params.put("source", 5);
                    if (Objects.isNull(praise.getReplyId())) {
                        // 评论点赞
                        params.put("attachId", praise.getCommentId());
                    } else {
                        // 回复点赞
                        params.put("attachId", praise.getReplyId());
                    }
                }
                chatFeign.sendNotice(params);
            }
        } catch (Exception e) {
            log.error("Post send notice error [message:{}]", e);
        }
    }

    /**
     * @Description: 点赞任务达成
     * @Param: [praise]
     * @Return: void
     * @Author: wangyingjie
     * @Date: 2020/7/29
     */
    private void taskHandler(Praise praise) {
        Long userId = praise.userId;

        // 获取被点赞的动态的发布人是否为好友
        TemplateEnum templateEnum = postMapper.isFriend(praise) == null ? TemplateEnum.P04 : TemplateEnum.P03;
        // TASK[TD]：好友动态点赞{0}次
        threadPoolTaskExecutor.submit(() -> userFeign.handlerOfDailyTask(new Task(templateEnum, userId)));
    }
    
    /** 
     * @Description: 记录日志
     * @Param: [key, target, post] 
     * @Return: void 
     * @Author: wangyingjie
     * @Date: 2020/9/3 
     */ 
    private void logPost(Long userId, String template, String target, Integer id) {
        Map<String, Object> message = new HashMap<String, Object>() {
            {
                put("userId", userId);
                put("operation", MessageFormat.format(template, target, id));
                put("logTime", DateUtils.getCurrentDateTime());
            }
        };

        producerFeign.sendPost(message);
    }

}
