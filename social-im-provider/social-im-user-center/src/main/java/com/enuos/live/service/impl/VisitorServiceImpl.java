package com.enuos.live.service.impl;

import com.enuos.live.error.ErrorCode;
import com.enuos.live.mapper.VisitorMapper;
import com.enuos.live.result.Result;
import com.enuos.live.service.VisitorService;
import com.enuos.live.utils.DateUtils;
import com.enuos.live.utils.page.PageInfo;
import com.github.pagehelper.PageHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @Description 访客业务
 * @Author wangyingjie
 * @Date 2020/7/14
 * @Modified
 */
@Slf4j
@Service
public class VisitorServiceImpl implements VisitorService {

    @Autowired
    private VisitorMapper visitorMapper;

    /**
     * @Description: 保存访客记录
     * @Param: [params]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/7/15
     */
    @Override
    public Result save(Map<String, Object> params) {
        if (MapUtils.isEmpty(params)) {
            return Result.empty();
        }

        Long userId = MapUtils.getLong(params, "toUserId");
        Long visitorId = MapUtils.getLong(params, "userId");
        // 自己的访客记录不保存
        if (Objects.equals(userId, visitorId)) {
            return Result.success();
        }

        // 同一用户当天只产生一条访客记录
        // 查询今天是否存在该用户的访客记录：存在则更新访问时间；不存在则新增
        Map<String, Object> visitMap = visitorMapper.getVisitor(userId, visitorId);
        if (MapUtils.isEmpty(visitMap)) {
            visitorMapper.save(userId, visitorId);
        } else {
            visitorMapper.updateVisitTime(MapUtils.getInteger(visitMap, "id"));
        }

        return Result.success();
    }

    /**
     * @Description: vip专享访客记录
     * @Param: [params]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/7/14
     */
    @Override
    public Result list(Map<String, Object> params) {
        if (MapUtils.isEmpty(params)) {
            return Result.empty();
        }

        Long userId = MapUtils.getLong(params, "userId");
        // 判断当前用户是不是vip
        Map<String, Object> map = visitorMapper.getMember(userId);
        if (MapUtils.isEmpty(map)) {
            return Result.error(ErrorCode.NO_DATA);
        }

        int vip = MapUtils.getIntValue(map, "vip");
        LocalDateTime expirationTime = DateUtils.getLocalDateTime(MapUtils.getString(map, "expirationTime"));

        // 非会员无权限
        if (vip == 0 || (vip > 0 && !expirationTime.isAfter(LocalDateTime.now()))) {
            return Result.error(ErrorCode.NO_PERMISSION);
        }

        PageHelper.startPage(MapUtils.getIntValue(params, "pageNum"), MapUtils.getIntValue(params, "pageSize"));

        List<Map<String, Object>> list = visitorMapper.list(userId);

        list.forEach(m -> m.put("isMember", MapUtils.getIntValue(m, "vip") > 0 ? DateUtils.getLocalDateTime(MapUtils.getString(m, "expirationTime")).isAfter(DateUtils.getCurrentDateTime()) ? 1 : -1 : 0));

        return Result.success(new PageInfo<>(list));
    }
}