package com.enuos.live.mapper;

import com.enuos.live.pojo.RelationPO;
import org.apache.ibatis.annotations.Param;

/**
 * @Description 房间用户关系
 * @Author wangyingjie
 * @Date 2020/5/12
 * @Modified
 */
public interface RoomRelationMapper {

    /**
     * 获取房间和用户的关系
     * @param fromId
     * @param toId
     * @return
     */
    Integer getRelation(@Param("fromId") Long fromId, @Param("toId") Long toId);

    /**
     * 新增
     * @param relationPO
     * @return
     */
    int save(RelationPO relationPO);

    /**
     * 删除
     * @param relationPO
     * @return
     */
    int delete(RelationPO relationPO);

}
