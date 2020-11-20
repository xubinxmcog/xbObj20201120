package com.enuos.live.controller;

import com.enuos.live.annotations.Cipher;
import com.enuos.live.error.ErrorCode;
import com.enuos.live.pojo.*;
import com.enuos.live.result.Result;
import com.enuos.live.service.PostService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Description 动态中心
 * @Author wangyingjie
 * @Date 15:19 2020/3/27
 * @Modified
 */
@Slf4j
@Api("动态中心")
@RestController
@RequestMapping("/post")
public class PostController {

    @Autowired
    private PostService postService;

    /**
     * @Description: 用户发表动态
     * @Param: [post]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/9/11
     */
    @ApiOperation(value = "朋友圈发表动态", notes = "朋友圈发表动态")
    @Cipher
    @PostMapping("/createPost")
    public Result createPost(@RequestBody Post post) {
        return postService.createPost(post);
    }

    /**
     * @Description: 删除动态
     * @Param: [post]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/9/11
     */
    @ApiOperation(value = "删除动态", notes = "删除动态")
    @Cipher
    @PostMapping("/deletePost")
    public Result deletePost(@RequestBody Post post) {
        return postService.deletePost(post);
    }

    /**
     * @Description: 删除评论或回复
     * @Param: [comment]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/9/11
     */
    @ApiOperation(value = "删除评论或回复", notes = "删除评论或回复")
    @Cipher
    @PostMapping("/deleteCommentAndReply")
    public Result deleteCommentAndReply(@RequestBody Comment comment) {
        return postService.deleteCommentAndReply(comment);
    }

    /**
     * @Description: [0:用户;1:好友;2:关注;3:广场;4:话题动态]
     * @Param: [post]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/9/11
     */
    @ApiOperation(value = "动态列表", notes = "动态列表")
    @Cipher
    @PostMapping("/getMoments")
    public Result getMoments(@RequestBody Post post) {
        return postService.getMoments(post);
    }

    /**
     * @Description: 获取动态
     * @Param: [post]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/9/11
     */
    @ApiOperation(value = "获取动态", notes = "获取动态")
    @Cipher
    @PostMapping("/getPost")
    public Result getPost(@RequestBody Post post) {
        return postService.getPost(post);
    }

    /**
     * @Description: 评论
     * @Param: [post]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/9/11
     */
    @ApiOperation(value = "评论", notes = "评论")
    @Cipher
    @PostMapping("/getComment")
    public Result getComment(@RequestBody Post post) {
        return postService.getComment(post);
    }

    /**
     * @Description: 转发
     * @Param: [post]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/9/11
     */
    @ApiOperation(value = "转发", notes = "转发")
    @Cipher
    @PostMapping("/getForward")
    public Result getForward(@RequestBody Post post) {
        return postService.getForward(post);
    }

    /**
     * @Description: 回复
     * @Param: [comment]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/9/11
     */
    @ApiOperation(value = "回复", notes = "回复")
    @Cipher
    @PostMapping("/getReply")
    public Result getReply(@RequestBody Comment comment) {
        return postService.getReply(comment);
    }

    /**
     * @Description: 发表评论&回复
     * @Param: [comment]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/9/11
     */
    @ApiOperation(value = "发表评论&回复", notes = "发表评论&回复")
    @Cipher
    @PostMapping("/createCommentOrReply")
    public Result createCommentOrReply(@RequestBody Comment comment) {
        return postService.createCommentOrReply(comment);
    }

    /**
     * @Description: 点赞&取消点赞
     * @Param: [praise]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/9/11
     */
    @ApiOperation(value = "点赞&取消点赞", notes = "点赞&取消点赞")
    @Cipher
    @PostMapping("/giveOrCancelPraise")
    public Result giveOrCancelPraise(@RequestBody Praise praise) {
        try {
            return postService.giveOrCancelPraise(praise);
        } catch (Exception e) {
            return Result.error(ErrorCode.EXCEPTION_CODE, e.getMessage());
        }
    }

    /**
     * @Description: 屏蔽动态
     * @Param: [shield]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/9/11
     */
    @ApiOperation(value = "屏蔽动态", notes = "屏蔽动态")
    @Cipher
    @PostMapping("/shield")
    public Result shield(@RequestBody Shield shield) {
        return postService.shield(shield);
    }

}