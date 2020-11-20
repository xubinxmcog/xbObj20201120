package com.enuos.live.mapper;

import com.enuos.live.pojo.MicPO;
import com.enuos.live.pojo.MicVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @Description 房间麦序
 * @Author wangyingjie
 * @Date 2020/5/14
 * @Modified
 */
public interface RoomMicMapper {

    /**
     * 麦序列表
     * @param roomId
     * @return
     */
    List<MicVO> getList(Long roomId);

    /**
     * 保存
     * @param micPO
     * @return
     */
    int save(MicPO micPO);

    /**
     * 删除
     * @param roomId
     * @param userId
     * @return
     */
    int delete(@Param("roomId") Long roomId, @Param("userId") Long userId);

}
