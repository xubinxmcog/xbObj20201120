package com.enuos.live.file.service;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.enuos.live.mapper.PicInfoMapper;
import com.enuos.live.utils.AliYunOssClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * @ClassName TaskAsync
 * @Description: TODO 异步处理类
 * @Author xubin
 * @Date 2020/6/3
 * @Version V1.0
 **/
@Slf4j
@Component
public class TaskAsync {

    @Autowired
    private PicInfoMapper picInfoMapper;

    // 默认图片
    private final String defaultUrl = "https://7lestore.oss-cn-hangzhou.aliyuncs.com/picture/b277e6c2495445abab4353453ca40ffb.png";

    /**
     * @MethodName: upPic
     * @Description: TODO 更新违规图片
     * @Param: [fileName]
     * @Return: void
     * @Author: xubin
     * @Date: 15:07 2020/7/8
     **/
    @Async
    public void upPic(String fileName) {
        Map<String, Object> map = picInfoMapper.selectPicUrl(fileName);// 根据文件名查找URL
        String picUrl = map.get("picUrl").toString();
        String picNewName = map.get("picNewName").toString();

        if (picNewName.contains("room")) { // 房间
            upUrlRoom(picUrl);
        }
        if (picNewName.contains("header")) {
            upUrlUserHeader(picUrl); // 头像
        }
        if (picNewName.contains("post")) {
            urlPostResource(picUrl); // 更新用户动态违规图片
        }
        if (picNewName.contains("background")) {
            background(picUrl); // 用户背景
        }

    }

    /**
     * @MethodName: upUrlUserHeader
     * @Description: TODO 更新用户头像违规图片
     * @Param: [picUrl]
     * @Return: void
     * @Author: xubin
     * @Date: 17:57 2020/7/8
     **/
    @Async
    public void upUrlUserHeader(String picUrl) {
        Map<String, Object> map = picInfoMapper.queryUrlUserHeader(picUrl); // 头像
        if (ObjectUtil.isNotEmpty(map)) {
            String thumbIconUrl = map.get("thumbIconUrl").toString();
            if (thumbIconUrl.contains(picUrl)) {
                thumbIconUrl = defaultUrl;
            }
            Integer id = Integer.parseInt(map.get("id").toString());
            picInfoMapper.updateUserHeader(defaultUrl, thumbIconUrl, null, id);
        }
    }

    /**
     * @MethodName: upUrlRoom
     * @Description: TODO 更新语音房违规图片
     * @Param: [picUrl]
     * @Return: void
     * @Author: xubin
     * @Date: 16:43 2020/7/8
     **/
    @Async
    public void upUrlRoom(String picUrl) {
        Map<String, Object> voiceRoomCoverUrl = picInfoMapper.queryVoiceRoomCoverUrl(picUrl); // 封面
        if (ObjectUtil.isNotEmpty(voiceRoomCoverUrl)) {
//            String coverUrl = voiceRoomCoverUrl.get("coverUrl").toString();
            Integer id = Integer.parseInt(voiceRoomCoverUrl.get("id").toString());
            picInfoMapper.updateRoomUrl(defaultUrl, null, id);
        }

        Map<String, Object> voiceRoomBackgroundUrl = picInfoMapper.queryVoiceRoomBackgroundUrl(picUrl); // 语音房背景
        if (ObjectUtil.isNotEmpty(voiceRoomBackgroundUrl)) {
//            String backgroundUrl = voiceRoomBackgroundUrl.get("backgroundUrl").toString();
            Integer id = Integer.parseInt(voiceRoomCoverUrl.get("id").toString());
            picInfoMapper.updateRoomUrl(null, "", id);
        }

    }

    public static void main(String[] args) {
        String thumbUrl = "https://7lestore.oss-cn-hangzhou.aliyuncs.com/picture/b277e6c2495445abab4353453ca40ffb.png?x-oss-process=image/resize,w_400";
        String[] split = thumbUrl.split("\\?");
        if (split.length > 1) {

            System.out.println(split[1]);
        }
    }

    /**
     * @MethodName: urlPostResource
     * @Description: TODO 更新用户动态违规图片
     * @Param: [picUrl]
     * @Return: void
     * @Author: xubin
     * @Date: 15:06 2020/7/8
     **/
    @Async
    public void urlPostResource(String picUrl) {
        log.info("更新用户动态违规图片, picUrl = [{}]", picUrl);
        Map<String, Object> stringStringMap = picInfoMapper.queryPostResource(picUrl); // 根据URL查找用户动态资源表的相关文件URL
        if (ObjectUtil.isNotEmpty(stringStringMap)) {
            String id = String.valueOf(stringStringMap.get("id"));
            String url = (String) stringStringMap.get("url");
            String coverUrl = (String) stringStringMap.get("coverUrl");
            String thumbUrl = (String) stringStringMap.get("thumbUrl");
            if (StrUtil.isNotEmpty(url)) {
                url = defaultUrl;
            }
            if (StrUtil.isNotEmpty(coverUrl)) {
                coverUrl = defaultUrl;
            }
            if (StrUtil.isNotEmpty(thumbUrl)) {
//                String suffix = thumbUrl.substring(thumbUrl.lastIndexOf("?"));
                StringBuffer suffix = new StringBuffer();
                String[] split = thumbUrl.split("\\?");
                if (split.length > 1) {
                    suffix.append("?").append(split[1]);
                }
                thumbUrl = defaultUrl + suffix;
            }

            int i = picInfoMapper.updatePostResourceUrl(url, coverUrl, thumbUrl, id); // 更新用户动态资源表URL
            if (i < 0) {
                log.error("更新用户动态违规图片失败: picUrl=[{}]", picUrl);
            } else {
                log.info("更新用户动态违规图片成功: picUrl=[{}]", picUrl);
            }
        } else {
            log.error("查询用户动态违规图片失败, picUrl=[{}]", picUrl);
        }
    }

    /**
     * @MethodName: background
     * @Description: TODO 用户背景
     * @Param: [picUrl]
     * @Return: void
     * @Author: xubin
     * @Date: 16:40 2020/7/22
     **/
    @Async
    public void background(String picUrl) {
        Map<String, Object> voiceRoomCoverUrl = picInfoMapper.queryBackgroundUrl(picUrl); // 用户背景
        if (ObjectUtil.isNotEmpty(voiceRoomCoverUrl)) {
            Integer id = Integer.parseInt(voiceRoomCoverUrl.get("id").toString());
            picInfoMapper.updateUserHeader(null, null, "", id);
        }
    }

    /**
     * @MethodName: delRoomChatFile
     * @Description: TODO 删除语音房聊天文件
     * @Param: [roomId]
     * @Return: void
     * @Author: xubin
     * @Date: 15:36 2020/7/8
     **/
    @Async
    public void delRoomChatFile(String fileName) {
        List<Map<String, Object>> fileUrls = picInfoMapper.fileUrls(fileName);
        if (ObjectUtil.isNotEmpty(fileUrls)) {
            for (Map<String, Object> fileUrl : fileUrls) {
//                String picUrl = (String) fileUrl.get("picUrl");
                Long id = Long.valueOf(fileUrl.get("id").toString());
                String picNewName = fileUrl.get("picNewName").toString();
                log.info("开始删除语音房聊天文件, fileName=[{}]", fileName);
                AliYunOssClient.delFile(picNewName);
                picInfoMapper.deleteById(id);
            }
        }
    }
}
