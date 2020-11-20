package com.enuos.live.mapper;

import com.enuos.live.annotations.RoomUserActivityNote;
import com.enuos.live.pojo.SeatPO;
import com.enuos.live.pojo.SeatVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * @Description 房间座位关系表
 * @Author wangyingjie
 * @Date 2020/5/13
 * @Modified
 */
public interface RoomSeatMapper {

    /**
     * 座位
     *
     * @param roomId
     * @param ids
     * @return
     */
    int save(@Param("roomId") Long roomId, @Param("ids") Integer[] ids);

    /**
     * 更新
     *
     * @param seatPO
     * @return
     */
    @RoomUserActivityNote(activityType = 2)
    int update(SeatPO seatPO);

    /**
     * 更新麦
     *
     * @param seatPO
     * @return
     */
    int updateMic(SeatPO seatPO);

    /**
     * 清空座位
     *
     * @param roomId
     * @return
     */
    int clean(Long roomId);

    /**
     * 清空一个
     *
     * @param roomId
     * @param userId
     * @return
     */
    int cleanAny(@Param("roomId") Long roomId, @Param("userId") Long userId);

    /**
     * 删除座位
     *
     * @param ids
     * @return
     */
    int delete(Long... ids);

    /**
     * 是否在座位上
     *
     * @param roomId
     * @param userId
     * @return
     */
    Integer isOnSeat(@Param("roomId") Long roomId, @Param("userId") Long userId);

    // 座位是否锁定
    int isLock(@Param("roomId") Long roomId, @Param("seatId") Integer seatId);

    List<SeatVO> getRoomSeatList(@Param("roomId") Long roomId);

    // 获取麦上用户
    List<Map<String, Object>> getRoomSeatUsers(@Param("roomId") Long roomId);

    // 座位上是否有人
    Integer isSeatEmpty(@Param("roomId") Long roomId, @Param("seatId") Integer seatId);
}
