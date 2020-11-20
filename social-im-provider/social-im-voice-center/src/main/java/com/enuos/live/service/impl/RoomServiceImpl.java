package com.enuos.live.service.impl;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.aliyuncs.CommonRequest;
import com.aliyuncs.CommonResponse;
import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.exceptions.ServerException;
import com.aliyuncs.http.MethodType;
import com.aliyuncs.profile.DefaultProfile;
import com.enuos.live.constant.SuccessCode;
import com.enuos.live.constants.Constants;
import com.enuos.live.constants.RedisKey;
import com.enuos.live.error.ErrorCode;
import com.enuos.live.feign.UpFileFeign;
import com.enuos.live.feign.UserFeign;
import com.enuos.live.manager.AchievementEnum;
import com.enuos.live.manager.TaskEnum;
import com.enuos.live.mapper.*;
import com.enuos.live.pojo.*;
import com.enuos.live.result.Result;
import com.enuos.live.service.AliRtcAppServer;
import com.enuos.live.service.RoomService;
import com.enuos.live.server.RoomTaskServer;
import com.enuos.live.task.TemplateEnum;
import com.enuos.live.utils.DateUtils;
import com.enuos.live.utils.IDUtils;
import com.enuos.live.utils.RedisUtils;
import com.enuos.live.utils.page.PageInfo;
import com.github.pagehelper.PageHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.DefaultTypedTuple;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @Description 语音房业务
 * @Author wangyingjie
 * @Date 12:55 2020/5/11
 * @Modified
 */
@Slf4j
@Service
public class RoomServiceImpl implements RoomService {

    @Value("${aliyun.appId}")
    private String appID;

    @Value("${aliyun.accessKeyId}")
    private String accessKeyId;

    @Value("${aliyun.streamURL}")
    private String streamURL;

    @Value("${aliyun.accessSecret}")
    private String accessSecret;

    @Value("${aliyun.appKey}")
    private String appKey;

    @Value("${aliyun.gslb}")
    private String gslb;

    @Value("${aliyun.pullUrl}")
    private String pullUrl;

    @Value("${realname.value}")
    private boolean realname;

    private final String ROOM_ID_PREFIX = "R_";

    /**
     * 4座
     */
    private static final Integer[] SEAT4 = {0, 1, 2, 3, 4};

    /**
     * 8座
     */
    private static final Integer[] SEAT8 = {0, 1, 2, 3, 4, 5, 6, 7, 8};

    /**
     * 用户
     */
    private static final String[] USER_COLUMN = {"nickName", "thumbIconUrl", "sex", "level"};

//    private static Map ROOM_THEME = Label.getInstance().get("roomTheme").stream().collect(Collectors.toMap(k -> k.get("id"), v -> v.get("name")));

    @Autowired
    private RedisUtils redisUtils;

    @Autowired
    private UserFeign userFeign;

    @Autowired
    private RoomMapper roomMapper;

    @Autowired
    private RoomRelationMapper roomRelationMapper;

    @Autowired
    private RoomRoleMapper roomRoleMapper;

    @Autowired
    private RoomSeatMapper roomSeatMapper;

    @Autowired
    private RoomMicMapper roomMicMapper;

    @Autowired
    private RoomUserMapper roomUserMapper;

    @Autowired
    private UserCharmMapper userCharmMapper;

    @Autowired
    private UpFileFeign upFileFeign;

    @Autowired
    private RoomTaskServer roomTaskServer;

    @Autowired
    private VoiceRoomUserTimeMapper voiceRoomUserTimeMapper;

    @Autowired
    private VoiceRoomBackgroundMapper voiceRoomBackgroundMapper;

    @Resource(name = "taskFxbDrawExecutor")
    ExecutorService executorService;

    /**
     * 初始化房间信息
     *
     * @param roomPO
     * @return
     */
    @Override
    public Result init(RoomPO roomPO) {
        if (Objects.isNull(roomPO) || Objects.isNull(roomPO.getUserId())) {
            return Result.error(ErrorCode.DATA_ERROR);
        }

        Map<String, Object> roomMap = roomMapper.getBaseByUserId(roomPO.getUserId());

        if (MapUtils.isEmpty(roomMap)) {
            return Result.success(SuccessCode.TO_CREATE_ROOM);
        } else {
            Result valResult = validate(MapUtils.getInteger(roomMap, "status"));
            if (!Objects.isNull(valResult)) {
                return valResult;
            }
            return Result.success(roomMap);
        }
    }

    /**
     * 开播
     *
     * @param roomPO
     * @return
     */
    @Override
    @Transactional
    public Result startBroadcast(RoomPO roomPO) {
        if (Objects.isNull(roomPO) || Objects.isNull(roomPO.getUserId())) {
            return Result.error(ErrorCode.DATA_ERROR);
        }

        Long userId = roomPO.getUserId();
        Integer newSeatType = roomPO.getSeatType(); // 位置[26. 4个 27. 8个]

        Long roomId;

        Map<String, Object> roomMap = roomMapper.getBaseByUserId(userId);

        if (MapUtils.isEmpty(roomMap)) {

            // 判断房间名字长度不得大于8
            if (null != roomPO.getName() && roomPO.getName().length() > 8) {
                return Result.error(ErrorCode.EXCEPTION_CODE, "房间名称长度不得超过8个字");
            }

            // 公告字符长度不得超过20个字
            if (null != roomPO.getNotice() && roomPO.getNotice().length() > 20) {
                return Result.error(ErrorCode.EXCEPTION_CODE, "公告字符长度不得超过20个字");
            }

            // 实名认证才可创建语音房
            if (realname) {
                if (userFeign.isAuthentication(userId) == 0) {
                    return Result.error(ErrorCode.USER_NO_AUTHENTICATION);
                }
            }

            // 生成房间号并校验
            roomId = IDUtils.randomId(6);
            while (ObjectUtil.isNotEmpty(roomMapper.isExists(roomId))) {
                roomId = IDUtils.randomId(6);
            }

            roomPO.setRoomId(roomId);
            roomPO.setIsBroadcast(1);
            roomPO.setMicMode(0);
            roomPO.setIsBanScreen(0);

            int result = roomMapper.save(roomPO);

            if (result > 0) {
                // 初始化房主
                roomRoleMapper.save(roomId, userId, 1);
                // 初始化座位
                roomSeatMapper.save(roomPO.getRoomId(), newSeatType == 26 ? SEAT4 : SEAT8);
            }
        } else {

            roomId = MapUtils.getLong(roomMap, "roomId");

            Long updateTime = DateUtils.getAppointTimeSec(MapUtils.getString(roomMap, "updateTime"));
            long surTime = (System.currentTimeMillis() / 1000) - updateTime; // 语音房当前时间减去上次结束时间
            if (surTime < 30) {
                log.info("语音房距离上次结束时间=[{}],roomId=[{}]", surTime, roomId);
                return Result.error(ErrorCode.EXCEPTION_CODE, "请" + (31 - surTime) + "秒后再试");
            }

            Result valResult = validate(MapUtils.getInteger(roomMap, "status"));
            if (!Objects.isNull(valResult)) {
                return valResult;
            }

            Integer oldSeatType = MapUtils.getInteger(roomMap, "seatType");

            roomPO.setRoomId(roomId);
            roomPO.setIsBroadcast(1);
            roomPO.setStartTime(new Date());

            roomMapper.update(roomPO);

            if (!Objects.equals(oldSeatType, newSeatType)) {
                roomSeatMapper.delete(roomId);
                roomSeatMapper.save(roomId, newSeatType == 26 ? SEAT4 : SEAT8);
            }
        }

        // 推送给好友
//        if (roomPO.getIsPush() == 1) {
//
//        }

        Map<String, Object> userMap = userFeign.getUserBase(userId, USER_COLUMN);

        SeatPO seat = new SeatPO();
        seat.setRoomId(roomId);
        seat.setSeatId(0);
        seat.setUserId(userId);
        seat.setNickName(MapUtils.getString(userMap, "nickName"));
        seat.setThumbIconUrl(MapUtils.getString(userMap, "thumbIconUrl"));
        seat.setSex(MapUtils.getInteger(userMap, "sex"));

        roomSeatMapper.update(seat);
        roomUserMapper.save(roomId, userId);

        RoomDetailVO roomDetailVO = roomMapper.ownerRoomInfo(roomId, userId);
        roomDetailVO.setOnNum(0);
        roomDetailVO.setIsOnSeat(1);
        roomDetailVO.setRole(0);

        roomDetailVO.getSeatList().get(0).setIsBanMic(0);
        roomDetailVO.getSeatList().get(0).setRole(0);

        Result mpuTaskStatus = getMPUTaskStatus(ROOM_ID_PREFIX + roomId);
        if (mpuTaskStatus.getCode() == 0) {
            JSONObject data = (JSONObject) mpuTaskStatus.getData();
            Integer status = data.getInteger("Status");
            if (!Objects.isNull(status) && (status == 0 || status == 1)) {
                return Result.success(roomDetailVO);
            }
        }
        startMPUTask(ROOM_ID_PREFIX + roomId);
        Long finalRoomId = roomId;
        executorService.submit(() -> {
            roomMapper.saveVoiceRoomBroadcastTime(finalRoomId); // 记录开播时间
            voiceRoomUserTime(1, roomPO.getUserId(), finalRoomId);
        });
        return Result.success(roomDetailVO);
    }

    /**
     * 下播
     *
     * @param roomPO
     * @return
     */
    @Override
    public Result endBroadcast(RoomPO roomPO) {
        Long roomId = roomPO.getRoomId();
        // 修改房间状态
        int result = roomMapper.updateBroadcast(roomId, 0);
        // 清空座位和房间用户
        if (result < 1) {
            return Result.error();
        }
//        asyncStopMPUTask(roomId);
        executorService.submit(() -> {
            log.info("开始处理下播操作, roomId=[{}]", roomId);
            voiceRoomUserTime(2, roomPO.getUserId(), roomId);
            asyncStopMPUTask(roomId);
        });
        return Result.success();
    }

    /**
     * 房间列表
     *
     * @param roomPO
     * @return
     */
    @Override
    public Result list(RoomPO roomPO) {
        if (Objects.isNull(roomPO) || Objects.isNull(roomPO.getUserId())) {
            return Result.error(ErrorCode.DATA_ERROR);
        }

        PageHelper.startPage(roomPO.pageNum, roomPO.pageSize);

        List<RoomVO> roomList = roomMapper.getList(roomPO);
        for (RoomVO roomVO : roomList) {
            roomVO.setOnNum(redisUtils.sGetSetSize(RedisKey.KEY_ROOM_USER + roomVO.getRoomId()));
        }

        return Result.success(new PageInfo<>(roomList));
    }

    /**
     * 进入房间
     *
     * @param roomPO
     * @return
     */
    @Override
    @Transactional
    public Result in(RoomPO roomPO) {
        if (Objects.isNull(roomPO) || Objects.isNull(roomPO.getRoomId())) {
            return Result.error(ErrorCode.DATA_ERROR);
        }

        Long roomId = roomPO.getRoomId();

        if (roomMapper.isBroadcast(roomId) == 0) {
            return Result.error(ErrorCode.ROOM_END);
        }

        String password = roomMapper.getPassword(roomId);
        if (StringUtils.isNotBlank(password)) {
            String password1 = roomPO.getPassword();
            if (StringUtils.isEmpty(password1))
                return Result.error(2000, "请输入密码");
            // 获取密码
            if (!StringUtils.equals(password1, password)) {
                return Result.error(ErrorCode.PASSWORD_DIFFERENCE);
            }
        }

        // 新增用户到房间
        int save = roomUserMapper.save(roomId, roomPO.getUserId());
        if (save > 0) {
            executorService.submit(() -> {
                voiceRoomUserTime(1, roomPO.getUserId(), roomId);
            });
        }
        return Result.success();
    }

    /**
     * 退出房间
     *
     * @param roomPO
     * @return
     */
    @Override
    @Transactional
    public Result out(RoomPO roomPO) {
        if (Objects.isNull(roomPO) || Objects.isNull(roomPO.getRoomId())) {
            return Result.error(ErrorCode.DATA_ERROR);
        }

        Long roomId = roomPO.getRoomId();
        Long userId = roomPO.getUserId();

//        int result = roomUserMapper.delete(roomPO); // 删除房间用户
        int result = roomUserMapper.delRoomUserOnlineNum(roomId, userId); // 删除房间用户

//        roomMicMapper.delete(roomId, userId);
        redisUtils.zRemove(RedisKey.KEY_ROOM_MIC + roomId, userId);// 删除排麦列表用户
        int cleanAny = roomSeatMapper.cleanAny(roomId, userId);// 删除座位上用户
        if (cleanAny > 0) {
            // 是为排麦模式
            if (roomMapper.getMicMode(roomId) == 1) {
                Long vUserId = vUserId(roomId);
                if (vUserId > 0) {
                    Integer seatId = roomMapper.getRoomEmptySeat(roomId);
                    if (!(Objects.isNull(seatId)) && seatId > 0) {
                        SeatPO seatPO = new SeatPO();
                        seatPO.setId(seatId);
                        seatPO.setRoomId(roomId);
                        seatPO.setUserId(vUserId);
                        upSeat(seatPO);
                        redisUtils.zRemove(RedisKey.KEY_ROOM_MIC + roomId, vUserId);
                    }
                }
            }
        }
        executorService.submit(() -> {
            voiceRoomUserTime(2, userId, roomId);
            roomTaskServer.room20MinActivity(userId);
        });
        return Result.success();
    }

    /**
     * 房间信息
     *
     * @param roomPO
     * @return
     */
    @Override
    public Result info(RoomPO roomPO) {
        if (Objects.isNull(roomPO) || Objects.isNull(roomPO.getRoomId())) {
            return Result.error(ErrorCode.DATA_ERROR);
        }

        RoomDetailVO roomDetail = roomMapper.getInfo(roomPO);
        if (Objects.isNull(roomDetail)) {
            return Result.error(102, "语音房已结束");
        }

        roomDetail.setOnNum((int) redisUtils.sGetSetSize(RedisKey.KEY_ROOM_USER + roomPO.getRoomId()));
//        roomDetail.setMicList(roomMicMapper.getList(roomPO.getRoomId()));
        roomDetail.setPullUrl(pullUrl + ROOM_ID_PREFIX + roomPO.getRoomId());

        return Result.success(roomDetail);
    }

    /**
     * @MethodName: baseInfo
     * @Description: TODO 房间基础信息
     * @Param: [roomId]
     * @Return: com.enuos.live.result.Result
     * @Author: xubin
     * @Date: 13:43 2020/7/2
     **/
    @Override
    public Result baseInfo(Long roomId, Long userId) {
        return Result.success(roomMapper.getBaseInfo(roomId, userId));
    }

    /**
     * 房间座位配置
     *
     * @param seatPO
     * @return
     */
    @Override
    @Transactional
    public Result seat(SeatPO seatPO) {
        if (Objects.isNull(seatPO.getType())) {
            return Result.error(ErrorCode.EXCEPTION_CODE, "操作类型不能为空");
        }
        Long roomId = seatPO.getRoomId();
        Long userId = seatPO.getUserId();

        // 校验权限
        Integer role = roomRoleMapper.getAdmin(roomId, userId);
        if (!(role == 1 || role == 2)) {
            return Result.error(ErrorCode.NO_PERMISSION);
        }
        int result = 0;
        Integer type = seatPO.getType(); // 0:加锁解锁 1:禁麦 2:报人上麦 3:踢人

        switch (type) {
            case 0:
                // 座位加锁
                if (!Objects.isNull(seatPO.getIsLock())) {
                    // 加锁开启清除座位 关闭位置
                    if (0 == seatPO.getIsLock()) { // 解锁
                        // 是为排麦模式
                        if (roomMapper.getMicMode(roomId) == 1) {
                            Long vUserId = vUserId(roomId);
                            if (vUserId > 0) {
                                seatPO.setRoomId(roomId);
                                seatPO.setUserId(vUserId);
                                result = upSeat(seatPO);
                                redisUtils.zRemove(RedisKey.KEY_ROOM_MIC + roomId, vUserId);
                                return Result.success(new HashMap<String, Object>() {
                                    {
                                        put("autoJoinSeatUserId", vUserId);
                                    }
                                });
                            } else {
                                cleanSeat(seatPO);
                                result = roomSeatMapper.update(seatPO);
                            }
                        } else {
                            cleanSeat(seatPO);
                            result = roomSeatMapper.update(seatPO);
                        }
                    } else { // 加锁
                        cleanSeat(seatPO);
                        result = roomSeatMapper.update(seatPO);
                    }
                }
                break;
            case 1:
                // 是否禁麦
                if (!Objects.isNull(seatPO.getMic())) {
                    result = roomSeatMapper.updateMic(seatPO);
                }
                break;
            case 2:
                Long targetUserId = seatPO.getTargetUserId();

                // 判断用户是否在房间内
                if (Objects.isNull(roomUserMapper.getUserIsRoom(roomId, targetUserId))) {
                    log.info("该用户已离开房间, roomId=[{}], seatId=[{}], targetUserId=[{}]", roomId, seatPO.getSeatId(), seatPO.getTargetUserId());
                    return Result.error(2020, "该用户已离开房间");
                }

                // 判断用户是否在座位上
                if (!Objects.isNull(roomSeatMapper.isOnSeat(roomId, seatPO.getTargetUserId()))) {
                    log.info("该用户已在座位上, roomId=[{}], seatId=[{}], targetUserId=[{}]", roomId, seatPO.getSeatId(), seatPO.getTargetUserId());
                    return Result.error(2020, "该用户已在座位上");
                }

                // 指定位置抱人上麦
                if (!Objects.isNull(seatPO.getSeatId())) {
                    // 判断指定座位上是否有人
                    Integer seatEmpty = roomSeatMapper.isSeatEmpty(roomId, seatPO.getSeatId());
                    if (seatEmpty > 0) {
                        log.info("该位置已经有人, roomId=[{}], seatId=[{}]", roomId, seatPO.getSeatId());
                        return Result.error(2020, "该位置已经有人");
                    }
                    seatPO.setIsLock(0);
                    seatPO.setUserId(targetUserId);
                    result = upSeatPO(seatPO, targetUserId);
                    redisUtils.zRemove(RedisKey.KEY_ROOM_MIC + roomId, targetUserId);
                    break;
                }
                Integer seatId = roomMapper.getRoomEmptySeatUnlock(roomId); // 获取房间空位置-忽略锁
                if (!(Objects.isNull(seatId)) && seatId > 0) { // 有空位直接入座
                    seatPO.setIsLock(0);
                    seatPO.setId(seatId);
                    seatPO.setUserId(targetUserId);
                    result = upSeatPO(seatPO, targetUserId);
                    redisUtils.zRemove(RedisKey.KEY_ROOM_MIC + roomId, targetUserId);
                } else {
                    return Result.error(2020, "没有空余位置");
                }
                break;
            case 3:
                log.info("踢人下座位");
                // 被踢用户是否在座位上
                if (Objects.isNull(roomSeatMapper.isOnSeat(roomId, seatPO.getTargetUserId()))) {
                    log.info("该用户已离开座位, roomId=[{}], seatId=[{}], targetUserId=[{}]", roomId, seatPO.getSeatId(), seatPO.getTargetUserId());
                    return Result.error(2021, "该用户已离开座位");
                }
                // 是否为排麦模式
                if (roomMapper.getMicMode(roomId) == 1) {
                    Long vUserId = vUserId(roomId); // 排麦麦序里的第一个用户
                    if (vUserId > 0) {
                        seatPO.setRoomId(roomId);
                        seatPO.setUserId(vUserId);
                        result = upSeat(seatPO);
                        redisUtils.zRemove(RedisKey.KEY_ROOM_MIC + roomId, vUserId);
                    } else {
                        cleanSeat(seatPO);
                        seatPO.setIsLock(0);
                        result = roomSeatMapper.update(seatPO);
                    }
                } else {
                    cleanSeat(seatPO);
                    seatPO.setIsLock(0);
                    result = roomSeatMapper.update(seatPO);
                }
                break;
            default:
                log.error("座位配置无此操作类型");
        }
        return result > 0 ? Result.success() : Result.error();
    }

    private int upSeatPO(SeatPO seatPO, Long uId) {
        Map<String, Object> userMap = userFeign.getUserBase(uId, USER_COLUMN);
        seatPO.setNickName(MapUtils.getString(userMap, "nickName"));
        seatPO.setThumbIconUrl(MapUtils.getString(userMap, "thumbIconUrl"));
        seatPO.setSex(MapUtils.getInteger(userMap, "sex"));
        roomTaskServer.taskHandler(TaskEnum.PGT0033.getCode(), uId, seatPO.getRoomId());
        return roomSeatMapper.update(seatPO);
    }

    @Override
    public Result joinSeat(SeatPO seatPO) {
        Long roomId = seatPO.getRoomId();
        Long userId = seatPO.getUserId();
        if (Objects.isNull(seatPO.getSeatId()) && Objects.isNull(seatPO.getId())) {
            return Result.error(ErrorCode.EXCEPTION_CODE, "必要参数不可为空");
        }
        log.info("RoomServiceImpl.joinSeat,userId=[{}]", userId);
        int micMode = roomMapper.getMicMode(roomId);// 获取排麦模式
        int result = 0;
        // leave,join
        if (seatPO.getIsJoin() == 0) { // 是否加入座位[0 离开 1 加入]
            cleanSeat(seatPO);
            if (seatPO.getSeatId() == 0) {
                return Result.error(ErrorCode.EXCEPTION_CODE, "房主不可离开座位");
            }
            result = roomSeatMapper.update(seatPO);
            if (micMode == 1) {
                Long vUserId = vUserId(roomId);
                if (vUserId > 0) {
                    seatPO.setUserId(vUserId);
                    upSeat(seatPO);
                    redisUtils.zRemove(RedisKey.KEY_ROOM_MIC + roomId, vUserId);
                }
            }
            // 是否在座位上
            if (Objects.isNull(roomSeatMapper.isOnSeat(roomId, userId))) {

                result = roomSeatMapper.update(seatPO);
                if (micMode == 1) {
                    Long vUserId = vUserId(roomId);
                    if (vUserId > 0) {
                        seatPO.setUserId(vUserId);
                        upSeat(seatPO);
                        redisUtils.zRemove(RedisKey.KEY_ROOM_MIC + roomId, vUserId);
                    }
                }
            }
        } else {

            if (micMode == 1) {
                return Result.error(4003, "当前为排麦模式");
            }
            if (roomSeatMapper.isLock(roomId, seatPO.getSeatId()) == 1) {
                return Result.error(4003, "座位已锁定");
            }
            Map<String, Object> userMap = userFeign.getUserBase(userId, USER_COLUMN);
            seatPO.setNickName(MapUtils.getString(userMap, "nickName"));
            seatPO.setThumbIconUrl(MapUtils.getString(userMap, "thumbIconUrl"));
            seatPO.setSex(MapUtils.getInteger(userMap, "sex"));
            // 换座位
            if (!Objects.isNull(roomSeatMapper.isOnSeat(roomId, userId))) {
                roomSeatMapper.cleanAny(roomId, userId);
            }
            result = roomSeatMapper.update(seatPO);
            roomTaskServer.taskHandler(TaskEnum.PGT0033.getCode(), userId, roomId);

            userFeign.handlerOfDailyTask(userId, 1, TemplateEnum.V04);
        }

        return result > 0 ? Result.success() : Result.error();
    }

    /**
     * @MethodName: queueMic
     * @Description: TODO 修改排麦
     * @Param: [seatPO]
     * @Return: com.enuos.live.result.Result
     * @Author: xubin
     * @Date: 11:07 2020/6/29
     **/
    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public Result upQueueMic(MicPO micPO) {
        Long roomId = micPO.getRoomId();
        Long userId = micPO.getUserId();

        // 校验是否房主
        if (Objects.isNull(roomMapper.isChmod(roomId, userId))) {
            return Result.error(ErrorCode.NO_PERMISSION);
        }
        List<Long> currentUserIds = micPO.getCurrentUserIds(); // 修改后的麦序
        List<Long> originalUserIds = micPO.getOriginalUserIds();// 修改前的麦序
        log.info("修改后的麦序,currentUserIds=[{}]", currentUserIds);
        log.info("修改前的麦序,originalUserIds=[{}]", originalUserIds);

        if (ObjectUtil.isEmpty(originalUserIds)) {
            log.error("修改排麦原用户id列表不能为空,roomId=[{}]", roomId);
            return Result.error(ErrorCode.CONTENT_EMPTY);
        }

        if (ObjectUtil.isEmpty(currentUserIds)) {
//            redisUtils.del(RedisKey.KEY_ROOM_MIC + roomId);
            Long[] originalUserId = new Long[originalUserIds.size()];
            Long[] userIds = originalUserIds.toArray(originalUserId);
            redisUtils.zRemove(RedisKey.KEY_ROOM_MIC + roomId, userIds);
            return Result.success();
        }

        // 删除麦序中指定的用户
        List<Long> diffrents = getDiffrent(currentUserIds, originalUserIds);
        if (ObjectUtil.isNotEmpty(diffrents)) {
            Long[] diffrentUserId = new Long[diffrents.size()];
            Long[] userIds = diffrents.toArray(diffrentUserId);
            redisUtils.zRemove(RedisKey.KEY_ROOM_MIC + roomId, userIds);
        }

        // 当前麦序中的用户 和 修改后的麦序比较
        if (currentUserIds.size() > redisUtils.zSize(RedisKey.KEY_ROOM_MIC + roomId)) {
            // 当前排麦列表中的用户
            List<Long> micList = new ArrayList<>();
            Set<ZSetOperations.TypedTuple> zSet = redisUtils.zRangeWithScores(RedisKey.KEY_ROOM_MIC + roomId, 0, -1);
            if (ObjectUtil.isNotEmpty(zSet)) {
                for (ZSetOperations.TypedTuple typedTuple : zSet) {
                    Long micUserId = Long.valueOf(typedTuple.getValue().toString());
                    micList.add(micUserId);
                }
            }
            List<Long> diffrent = getDiffrent(currentUserIds, micList);
            currentUserIds.removeAll(diffrent);
        }

        int i = 1;
        Set set = new HashSet<>();
        for (Long uId : currentUserIds) {
            DefaultTypedTuple<Long> tuple = new DefaultTypedTuple<Long>(uId, increaseNum() + i++);
            set.add(tuple);
        }
        redisUtils.zSet(RedisKey.KEY_ROOM_MIC + roomId, set);

        return Result.success();
    }

    /**
     * @MethodName: queueMic
     * @Description: TODO 排麦
     * @Param: [seatPO]
     * @Return: com.enuos.live.result.Result
     * @Author: xubin
     * @Date: 11:07 2020/6/29
     **/
    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public Result queueMic(MicPO micPO) {
        Long roomId = micPO.getRoomId();
        Long userId = micPO.getUserId();
        int result = 0;
        if (!Objects.isNull(micPO.getType()) && 0 == micPO.getType()) {
            redisUtils.zRemove(RedisKey.KEY_ROOM_MIC + roomId, userId);
        } else {
            Integer seatId = roomMapper.getRoomEmptySeat(roomId); // 是否有空位
            if (!(Objects.isNull(seatId)) && seatId > 0) { // 有空位直接入座
                SeatPO seatPO = new SeatPO();
                seatPO.setUserId(userId);
                seatPO.setRoomId(roomId);
                seatPO.setId(seatId);
                result = upSeat(seatPO);//有空位直接入座
            } else { // 没有空位进入排麦队列
                redisUtils.zSet(RedisKey.KEY_ROOM_MIC + roomId, userId, increaseNum() + 10000);
                redisUtils.expire(RedisKey.KEY_ROOM_MIC + roomId, 172800); // 指定排麦失效时间2天
            }
        }

        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("autoOnSeat", result);

        return Result.success(resultMap);
    }

    /**
     * @MethodName: queueMicLis
     * @Description: TODO 获取排麦列表
     * @Param: [roomId]
     * @Return: com.enuos.live.result.Result
     * @Author: xubin
     * @Date: 15:48 2020/6/29
     **/
    @Override
    public Result getMicList(Long roomId) {
        if (roomId == null) {
            log.error("获取排麦列表 roomId为空");
            return Result.error(ErrorCode.DATA_ERROR);
        }
        List<Map<String, Object>> list = new ArrayList<>();
        Set<ZSetOperations.TypedTuple> set = redisUtils.zRangeWithScores(RedisKey.KEY_ROOM_MIC + roomId, 0, -1);
        if (ObjectUtil.isEmpty(set))
            return Result.success(list);

        for (ZSetOperations.TypedTuple typedTuple : set) {
            Long userId = Long.valueOf(typedTuple.getValue().toString());
            Map<String, Object> userMap = userFeign.getUserBase(userId, USER_COLUMN);
            userMap.put("userId", userId);
            userMap.put("score", typedTuple.getScore());
            list.add(userMap);
        }
        return Result.success(list);
    }

    /**
     * 房间用户关系[用户-房间：关注；房间-用户：1拉黑，2禁言]
     *
     * @param relationPO
     * @return
     */
    @Override
    public Result relation(RelationPO relationPO) {

        Integer type = relationPO.getType();

        // 禁言
        if (relationPO.getRelation() == 2) {

            // 校验是否房主
            if (Objects.isNull(roomMapper.isChmod(relationPO.getFromId(), relationPO.getUserId()))) {
                return Result.error(ErrorCode.NO_PERMISSION);
            }

            String bannedKey = RedisKey.KEY_VOICE_ROOM.concat(String.valueOf(relationPO.getFromId()).concat(":" + relationPO.getToId()));

            long time = 0;
            TimeUnit unit = TimeUnit.SECONDS;
            // 0=解除禁言 1=1分钟 2=10分钟 3=1小时 4=7天
            // 修改  22:1分钟 23:10分钟 24:1小时 25:7天
            switch (type) {
                case 0:
                    redisUtils.del(bannedKey);
                    return Result.success();
                case 22:
                    time = 1;
                    unit = TimeUnit.MINUTES;
                    break;
                case 23:
                    time = 10;
                    unit = TimeUnit.MINUTES;
                    break;
                case 24:
                    time = 1;
                    unit = TimeUnit.HOURS;
                    break;
                case 25:
                    time = 7;
                    unit = TimeUnit.DAYS;
                    break;
                default:
                    break;
            }
            redisUtils.set(bannedKey, relationPO.getType(), time, unit);
        } else {
            if (type == 0) { // 取消关注
                roomRelationMapper.delete(relationPO);
            } else { // 加关注
                roomRelationMapper.save(relationPO);
                achievementHandler(relationPO.getToId());
                roomTaskServer.taskHandler(TaskEnum.PGT0030.getCode(), relationPO.getFromId(), relationPO.getToId());
            }
        }

        return Result.success();
    }

    /**
     * 设置房间角色
     *
     * @param rolePO
     * @return
     */
    @Override
    public Result role(RolePO rolePO) {
        if (Objects.isNull(rolePO) || Objects.isNull(rolePO.getRoomId())) {
            return Result.error(ErrorCode.DATA_ERROR);
        }
        rolePO.setRole(2);
        if (roomMapper.isOwner(rolePO.getRoomId(), rolePO.getUserId()) == 0) {
            return Result.error(ErrorCode.NO_PERMISSION);
        }

        int result = 0;
        int type = rolePO.getType();
        switch (type) {
            case 0:
                Integer admin = roomRoleMapper.getAdmin(rolePO.getRoomId(), rolePO.getTargetUserId());
                if (!Objects.isNull(admin)) {
                    return Result.error(ErrorCode.ACCOUNT_EXISTS);
                }
                result = roomRoleMapper.save(rolePO.getRoomId(), rolePO.getTargetUserId(), rolePO.getRole());
                break;
            case 1:
                result = roomRoleMapper.delete(rolePO.getRoomId(), rolePO.getTargetUserId(), rolePO.getRole());
                break;
            default:
                log.info("设置房间角色操作类型错误");
                return Result.error(ErrorCode.DATA_ERROR);
        }
        return result > 0 ? Result.success() : Result.error();
    }

    /**
     * @MethodName: getRole
     * @Description: TODO 获取角色列表
     * @Param: [rolePO]
     * @Return: com.enuos.live.result.Result
     * @Author: xubin
     * @Date: 14:26 2020/6/30
     **/
    @Override
    public Result getRole(RolePO rolePO) {
        if (null == rolePO.getRole() || null == rolePO.getRoomId()) {
            log.error("获取角色列表,参数为空");
            return Result.error(ErrorCode.DATA_ERROR);
        }
        return Result.success(roomRoleMapper.getRole(rolePO.getRoomId(), rolePO.getRole()));
    }

    /**
     * 禁言黑名单
     *
     * @param roomPO
     * @return
     */
    @Override
    public Result bannedList(RoomPO roomPO) {
        if (Objects.isNull(roomPO) || Objects.isNull(roomPO.getRoomId())) {
            return Result.error(ErrorCode.DATA_ERROR);
        }

        String bannedKey = RedisKey.KEY_VOICE_ROOM.concat(String.valueOf(roomPO.getRoomId()).concat(":*"));
        Set<String> set = redisUtils.keys(bannedKey);

        List<Map<String, Object>> blacklist = new ArrayList<>();

        if (CollectionUtils.isNotEmpty(set)) {
            blacklist = roomUserMapper.getBannedUser(roomPO.getUserId(), set.stream().map(key -> Long.valueOf(key.split(":")[2])).collect(Collectors.toList()));
        }

        return Result.success(blacklist);
    }

    /**
     * 房间设置
     *
     * @param roomPO
     * @return
     */
    @Override
    @Transactional
    public Result setting(RoomPO roomPO) {

        // 判断房间名字长度不得大于8
        if (null != roomPO.getName() && roomPO.getName().length() > 8) {
            return Result.error(ErrorCode.EXCEPTION_CODE, "房间名称长度不得超过8个字");
        }

        // 公告字符长度不得超过20个字
        if (null != roomPO.getNotice() && roomPO.getNotice().length() > 20) {
            return Result.error(ErrorCode.EXCEPTION_CODE, "公告字符长度不得超过20个字");
        }

        if (Objects.isNull(roomPO) || Objects.isNull(roomPO.getRoomId())) {
            return Result.error(ErrorCode.DATA_ERROR);
        }

        if (roomMapper.isOwner(roomPO.getRoomId(), roomPO.getUserId()) == 0) {
            return Result.error(ErrorCode.NO_PERMISSION);
        }

        // 暂不支持修改位置
        roomPO.setSeatType(null);

        roomMapper.update(roomPO);
        if (!Objects.isNull(roomPO.getMicMode()) && 0 == roomPO.getMicMode()) { // 修改为自由模式则清空排麦列表
            redisUtils.del(RedisKey.KEY_ROOM_MIC + roomPO.getRoomId());
        }

        return Result.success(info(roomPO));
    }

    /**
     * 房间用户信息
     *
     * @param roomPO
     * @return
     */
    @Override
    public Result userInfo(RoomPO roomPO) {
        if (Objects.isNull(roomPO) || Objects.isNull(roomPO.getRoomId())) {
            return Result.error(ErrorCode.DATA_ERROR);
        }
        Long roomId = roomPO.getRoomId();
        Long userId = roomPO.getUserId();
        Long targetId = roomPO.getTargetId();
        int isInRoom = redisUtils.sHasKey(RedisKey.KEY_ROOM_USER + roomId, targetId) == true ? 1 : 0;
        Map<String, Object> userinfo = roomUserMapper.getRoomUserInfo(roomId, userId, targetId);
        if (MapUtils.isNotEmpty(userinfo)) {
            userinfo.put("isBanned", isBanned(roomPO.getRoomId(), roomPO.getTargetId()));
            userinfo.put("isInRoom", isInRoom);
        }

        return Result.success(userinfo);
    }

    /**
     * [OPEN]获取房间用户id
     *
     * @param roomId
     * @return
     */
    @Override
    public List<Long> getRoomUserIdList(Long roomId) {
        if (roomId == null) {
            return new ArrayList<>();
        }

//        List<Long> userIdList = roomUserMapper.getRoomUserIdList(roomId);
        Set set = redisUtils.sGet(RedisKey.KEY_ROOM_USER + roomId);
        List<Long> userIdList = new ArrayList<>(set);
        return userIdList;
    }

    // 获取房间用户列表
    @Override
    public Result getRoomUserList(Long roomId) {
//        Set set = redisUtils.sGet(RedisKey.KEY_ROOM_USER + roomId);
//        List<Long> userIdList = new ArrayList<>(set);
        return Result.success(roomUserMapper.getRoomUsers(roomId));
    }

    // 获取房间座位列表
    @Override
    public Result getRoomSeatList(Long roomId) {
        return Result.success(roomSeatMapper.getRoomSeatList(roomId));
    }

    /**
     * [OPEN]是否在禁言期
     *
     * @param roomId
     * @param userId
     * @return
     */
    @Override
    public Map<String, Object> isBanned(Long roomId, Long userId) {
        String bannedKey = RedisKey.KEY_VOICE_ROOM.concat(String.valueOf(roomId)).concat(":" + userId);
        Integer isBanned = 0;
        String msg = "";
        if (redisUtils.hasKey(bannedKey)) {
            isBanned = 1;
            long time = redisUtils.getExpire(bannedKey);
            if (time != 0) {
                msg = getExpire(time);
            } else {
                msg = "99年364天23小时59分59秒";
            }
        }

        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("isBanned", isBanned);
        resultMap.put("msg", msg);

        return resultMap;
    }

    /**
     * @MethodName: dedicateRanking
     * @Description: TODO 房间贡献榜
     * @Param: [roomId]
     * @Return: com.enuos.live.result.Result
     * @Author: xubin
     * @Date: 10:38 2020/7/8
     **/
    @Override
    public Result dedicateRanking(Long roomId, Integer pageSize) {
        if (Objects.isNull(pageSize)) {
            pageSize = 10;
        }
        return Result.success(userCharmMapper.getDedicateRanking(roomId, pageSize));
    }

    @Override
    public Result getRoomTheme() {
        return Result.success(roomMapper.getRoomTheme());
    }

    /**
     * @MethodName: getOnlineNum
     * @Description: TODO 获取房间人数
     * @Param: [roomId]
     * @Return: java.lang.Integer
     * @Author: xubin
     * @Date: 11:10 2020/8/19
     **/
    @Override
    public Integer getOnlineNum(Long roomId) {
        return (int) redisUtils.sGetSetSize(RedisKey.KEY_ROOM_USER + roomId);
//        return roomUserMapper.getOnlineNum(roomId);
    }

    /**
     * @MethodName: getVoiceRoomBackgrounds
     * @Description: TODO 语音房背景图列表
     * @Param: [pageNum, pageSize]
     * @Return: com.enuos.live.result.Result
     * @Author: xubin
     * @Date: 11:09 2020/8/19
     **/
    @Override
    public Result getVoiceRoomBackgrounds(Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);

        return Result.success(new PageInfo(voiceRoomBackgroundMapper.getVoiceRoomBackgrounds()));
    }

    /**
     * @MethodName: getVoiceRoomUserRole
     * @Description: TODO 获取房间用户角色
     * @Param: [userId, roomId]
     * @Return: com.enuos.live.result.Result
     * @Author: xubin
     * @Date: 13:43 2020/8/27
     **/
    @Override
    public Integer getVoiceRoomUserRole(Long userId, Long roomId) {
        Integer admin = roomRoleMapper.getAdmin(roomId, userId);
        if (Objects.isNull(admin)) {
            return 0;
        }
        return admin;
    }

    /**
     * @MethodName: enterEffects
     * @Description: TODO 获取用户语音房进场特效
     * @Param: [userId]
     * @Return: java.lang.String
     * @Author: xubin
     * @Date: 17:38 2020/10/26
     **/
    @Override
    public String enterEffects(Long userId) {
        String attribute = roomMapper.enterEffects(userId);
        if (StrUtil.isNotEmpty(attribute)) {
            JSONObject jsonObject = JSONObject.parseObject(attribute);
            String enterEffects = jsonObject.getString("enterEffects");
            if (StrUtil.isNotEmpty(enterEffects)) {
                return enterEffects;
            }
        }
        return "";
    }

    /**
     * @MethodName: startMPUTask
     * @Description: TODO 阿里云旁路转推开始任务
     * @Param: [channelId]
     * @Return: com.enuos.live.result.Result
     * @Author: xubin
     * @Date: 17:02 2020/6/24
     **/
    @Override
    public Result startMPUTask(String channelId) {
        DefaultProfile profile = DefaultProfile.getProfile("cn-hangzhou", accessKeyId, accessSecret);
        IAcsClient client = new DefaultAcsClient(profile);

        CommonRequest request = new CommonRequest();
        request.setSysMethod(MethodType.POST);
        request.setSysDomain("rtc.aliyuncs.com");
        request.setSysVersion("2018-01-11");
        request.setSysAction("StartMPUTask");
        request.putQueryParameter("RegionId", "cn-hangzhou");
        request.putQueryParameter("ChannelId", channelId);
        request.putQueryParameter("TaskId", channelId);
        request.putQueryParameter("MediaEncode", "0");
        request.putQueryParameter("LayoutIds.1", "1");
        request.putQueryParameter("StreamURL", streamURL + channelId);
        request.putQueryParameter("AppId", appID);
        request.putQueryParameter("TaskProfile", "Mixed_Audio");
        try {
            CommonResponse response = client.getCommonResponse(request);
            log.info("阿里云旁路转推开始任务");
            log.info(response.getData());
        } catch (ServerException e) {
            e.printStackTrace();
        } catch (ClientException e) {
            e.printStackTrace();
        }
        return Result.success();
    }

    /**
     * 下播异步操作
     *
     * @param taskId
     * @param roomId
     */
    @Transactional
    public void asyncStopMPUTask(Long roomId) {
        roomSeatMapper.clean(roomId);
        roomUserMapper.deleteRommUser(roomId);
        redisUtils.del(RedisKey.KEY_ROOM_MIC + roomId);

        String fileName = Constants.ROOM_CHAT + roomId;
        roomMapper.upVoiceRoomBroadcastTime(roomId);// 记录下播时间

        stopMPUTask(ROOM_ID_PREFIX + roomId);
        new Thread(() -> upFileFeign.delRoomChatFile(fileName)).start();
        // 下播后删除聊天文件
    }

    /**
     * @MethodName: stopMPUTask
     * @Description: TODO 调用StopMPUTask停止任务
     * @Param: [taskId]
     * @Return: com.enuos.live.result.Result
     * @Author: xubin
     * @Date: 17:03 2020/6/24
     **/
    @Override
    public Result stopMPUTask(String taskId) {

        DefaultProfile profile = DefaultProfile.getProfile("cn-hangzhou", accessKeyId, accessSecret);
        IAcsClient client = new DefaultAcsClient(profile);

        CommonRequest request = new CommonRequest();
        request.setSysMethod(MethodType.POST);
        request.setSysDomain("rtc.aliyuncs.com");
        request.setSysVersion("2018-01-11");
        request.setSysAction("StopMPUTask");
        request.putQueryParameter("RegionId", "cn-hangzhou");
        request.putQueryParameter("TaskId", taskId);
        request.putQueryParameter("AppId", appID);
        try {
            CommonResponse response = client.getCommonResponse(request);
            log.info("调用StopMPUTask停止任务,taskId=" + taskId);
            log.info(response.getData());
        } catch (ServerException e) {
            e.printStackTrace();
        } catch (ClientException e) {
            e.printStackTrace();
        }
        return Result.success();
    }

    /**
     * Status: 任务的状态ID。
     *
     * 0：等待channel开始。
     * 1：任务运行中 。
     * 2：任务已停止 。
     * 3：用户停止任务。
     * 4：Channel已停止。
     * 5：CDN网络问题，直播停止。
     * 6：直播URL问题，直播停止。
     */
    /**
     * @MethodName: getMPUTaskStatus
     * @Description: TODO 调用GetMPUTaskStatus获取任务状态
     * @Param: [taskId]
     * @Return: com.enuos.live.result.Result
     * @Author: xubin
     * @Date: 13:06 2020/7/14
     **/
    public Result getMPUTaskStatus(String taskId) {
        DefaultProfile profile = DefaultProfile.getProfile("cn-hangzhou", accessKeyId, accessSecret);
        IAcsClient client = new DefaultAcsClient(profile);

        CommonRequest request = new CommonRequest();
        request.setSysMethod(MethodType.POST);
        request.setSysDomain("rtc.aliyuncs.com");
        request.setSysVersion("2018-01-11");
        request.setSysAction("GetMPUTaskStatus");
        request.putQueryParameter("RegionId", "cn-hangzhou");
        request.putQueryParameter("TaskId", taskId);
        request.putQueryParameter("AppId", appID);
        try {
            CommonResponse response = client.getCommonResponse(request);
            JSONObject data = JSON.parseObject(response.getData());
            log.info(data + ", taskId=" + taskId);
            return Result.success(data);
        } catch (ServerException e) {
            e.printStackTrace();
        } catch (ClientException e) {
            e.printStackTrace();
            log.error(e.getRequestId());
            log.error(e.getErrMsg());
            log.error(e.getErrCode());
        }
        return Result.error();
    }

    /**
     * @MethodName: createToken
     * @Description: TODO 阿里云直播生成token
     * @Param: [params]
     * @Return: com.enuos.live.result.Result
     * @Author: xubin
     * @Date: 17:10 2020/6/24
     **/
    @Override
    public Result createToken(Map<String, String> params) {
        String channelId = params.get("channelId");
        String user = params.get("userId");
        String token = "";
        Long timestamp = (System.currentTimeMillis() / 1000) + 43200; // 过期时间12小时
        String nonce = "AK-" + IdUtil.simpleUUID();
        try {
            token = AliRtcAppServer.createToken(appID, appKey, channelId, user, nonce, timestamp);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return Result.error(500, token);
        }
        Map resultMap = new HashMap();
        resultMap.put("appID", appID);
        resultMap.put("appKey", appKey);
        resultMap.put("channelId", channelId);
        resultMap.put("userId", user);
        resultMap.put("timestamp", timestamp);
        resultMap.put("gslb", gslb);
        resultMap.put("nonce", nonce);
        resultMap.put("token", token);
        return Result.success(resultMap);
    }

    /**
     * 校验房间
     *
     * @param status
     * @return
     */
    private Result validate(Integer status) {
        Result result = null;
        switch (status) {
            case 2:
                result = Result.error(ErrorCode.ROOM_BANNED);
                break;
            case 3:
                result = Result.error(ErrorCode.ROOM_BANNED);
                break;
            default:
                break;
        }
        return result;
    }

    /**
     * 清除座位
     *
     * @param seatPO
     * @return
     */
    private SeatPO cleanSeat(SeatPO seatPO) {
        seatPO.setUserId(null);
        seatPO.setNickName(null);
        seatPO.setThumbIconUrl(null);
        seatPO.setSex(null);
        return seatPO;
    }

    /**
     * 获取过期时间
     *
     * @param time
     * @return
     */
    private String getExpire(long time) {
        long s = 60;
        long m = 60;
        long h = s * m;
        long d = h * 24;

        long days = time / d;
        long hours = (time % d) / h;
        long minutes = (time % h) / m;
        long seconds = time % s;

        StringBuffer sb = new StringBuffer();

        sb.append(days == 0 ? "" : days + "天").append(hours == 0 ? "" : hours + "小时").append(minutes == 0 ? "" : minutes + "分").append(seconds == 0 ? "" : seconds + "秒");

        return sb.toString();
    }


    private double increaseNum() {
        long l = System.currentTimeMillis();
        long y = l % 1000000000;
        double num = y / 1000.0;
        return num;
    }

    /**
     * 排麦模式自动入座
     *
     * @param roomId
     * @param userId
     */
    public int upSeat(SeatPO seatPO) {
        Map<String, Object> userMap = userFeign.getUserBase(seatPO.getUserId(), USER_COLUMN);
        seatPO.setNickName(MapUtils.getString(userMap, "nickName"));
        seatPO.setThumbIconUrl(MapUtils.getString(userMap, "thumbIconUrl"));
        seatPO.setSex(MapUtils.getInteger(userMap, "sex"));
        // 是否在座位上
        if (Objects.isNull(roomSeatMapper.isOnSeat(seatPO.getRoomId(), seatPO.getUserId()))) {
//            roomSeatMapper.cleanAny(seatPO.getRoomId(), seatPO.getUserId());
            roomSeatMapper.update(seatPO);
            roomTaskServer.taskHandler(TaskEnum.PGT0033.getCode(), seatPO.getUserId(), seatPO.getRoomId());
        }

        return 1;
    }

    /**
     * @MethodName: achievementHandler
     * @Description: TODO 关注房间成就进度处理
     * @Param: [roomId]
     * @Return: void
     * @Author: xubin
     * @Date: 17:02 2020/7/14
     **/
    @Async
    private void achievementHandler(Long roomId) {
        Map<String, Object> roomRelationNum = roomMapper.getRoomRelationNum(roomId);
        Object num = roomRelationNum.get("num");
        Object userId = roomRelationNum.get("userId");
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>() {
            {
                add(new HashMap<String, Object>() {
                    {
                        put("code", AchievementEnum.AMT0045.getCode());
                        put("progress", num);
                        put("isReset", 1);
                    }
                });
                add(new HashMap<String, Object>() {
                    {
                        put("code", AchievementEnum.AMT0046.getCode());
                        put("progress", num);
                        put("isReset", 1);
                    }
                });
            }
        };

        new Thread(() -> {
            userFeign.achievementHandlers(new HashMap<String, Object>() {
                {
                    put("userId", userId);
                    put("list", list);
                }
            });
        }).start();

    }

    /**
     * 取出第一个集合中第一个userId
     *
     * @param roomId
     * @return
     */
    private Long vUserId(Long roomId) {
        Set<ZSetOperations.TypedTuple> set = redisUtils.zRangeWithScores(RedisKey.KEY_ROOM_MIC + roomId, 0, 1);
        if (ObjectUtil.isNotEmpty(set)) {
            ZSetOperations.TypedTuple value = (ZSetOperations.TypedTuple) set.toArray()[0];
            Long vUserId = Long.valueOf(value.getValue().toString());
            return vUserId;
        }
        return 0L;
    }

    /**
     * @MethodName: voiceRoomUserTime
     * @Description: TODO 用户进入语音房观看时间处理
     * @Param: [type 1:进入房间 2:退出房间, userId, roomId]
     * @Return: void
     * @Author: xubin
     * @Date: 16:36 2020/7/31
     **/
    private void voiceRoomUserTime(int type, Long userId, Long roomId) {

        VoiceRoomUserTime voiceRoomUserTime = new VoiceRoomUserTime();
        switch (type) {
            case 1:
                log.info("用户进入语音房时间,userId=[{}], roomId=[{}]", userId, roomId);
                voiceRoomUserTime.setRoomId(roomId);
                voiceRoomUserTime.setUserId(userId);
                voiceRoomUserTimeMapper.insert(voiceRoomUserTime);
                break;
            case 2:
                log.info("用户退出语音房时间,userId=[{}], roomId=[{}]", userId, roomId);
                voiceRoomUserTime.setRoomId(roomId);
                voiceRoomUserTime.setUserId(userId);
                int update = voiceRoomUserTimeMapper.update(voiceRoomUserTime);
                if (update == 0) {
                    voiceRoomUserTimeMapper.deleteVoiceRoomUserTime(voiceRoomUserTime);
                } else {
                    roomTaskServer.taskHandler(TaskEnum.PGT0029.getCode(), userId, roomId);
                }
                break;
            default:
                log.warn("用户进入语音房观看时间处理, 无操作项");
        }

    }

    /**
     * 取出两个集合中不同的元素
     *
     * @param list1
     * @param list2
     * @return
     */
    public static List<Long> getDiffrent(List<Long> list1, List<Long> list2) {
        Map<Long, Integer> map = new HashMap<>(list1.size() + list2.size());
        List<Long> diff = new ArrayList<>();
        List<Long> maxList = list1;
        List<Long> minList = list2;
        if (list2.size() > list1.size()) {
            maxList = list2;
            minList = list1;
        }

        for (Long string : maxList) {
            map.put(string, 1);
        }

        for (Long string : minList) {
            Integer cc = map.get(string);
            if (cc != null) {
                map.put(string, ++cc);
                continue;
            }
            map.put(string, 1);
        }

        for (Map.Entry<Long, Integer> entry : map.entrySet()) {
            if (entry.getValue() == 1) {
                diff.add(entry.getKey());
            }
        }
        return diff;
    }


}