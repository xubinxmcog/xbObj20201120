package com.enuos.live.mapper;

import com.enuos.live.pojo.PicInfo;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface PicInfoMapper {
    int deleteByPrimaryKey(String picNewName);

    int deleteById(@Param("id") Long id);

    int insert(PicInfo record);

    PicInfo selectByPrimaryKey(Long id);

    Map<String, Object> selectPicUrl(@Param("picNewName") String picNewName);

    int updateByPrimaryKeySelective(PicInfo record);

    int updateIsViolation(PicInfo record);

    Map<String, Object> queryPostResource(@Param("url") String url);

    Map<String, Object> queryVoiceRoomCoverUrl(@Param("url") String url);

    Map<String, Object> queryBackgroundUrl(@Param("url") String url);

    Map<String, Object> queryVoiceRoomBackgroundUrl(@Param("url") String url);

    Map<String, Object> queryUrlUserHeader(@Param("iconUrl") String iconUrl);

    List<Map<String, Object>> fileUrls(@Param("fileName") String fileName);

    int updatePostResourceUrl(@Param("url") String url, @Param("coverUrl") String coverUrl, @Param("thumbUrl") String thumbUrl, @Param("id") String id);

    int updateRoomUrl(@Param("coverUrl") String coverUrl, @Param("backgroundUrl") String backgroundUrl, @Param("id") Integer id);

    int updateUserHeader(@Param("iconUrl") String iconUrl, @Param("thumbIconUrl") String thumbIconUrl, @Param("backgroundUrl") String backgroundUrl, @Param("id") Integer id);

}