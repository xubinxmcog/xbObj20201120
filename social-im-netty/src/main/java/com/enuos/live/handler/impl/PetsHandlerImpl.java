package com.enuos.live.handler.impl;

import cn.hutool.core.util.ObjectUtil;
import com.enuos.live.action.ChannelSet;
import com.enuos.live.action.PetsActionSet;
import com.enuos.live.constants.RedisKey;
import com.enuos.live.core.Packet;
import com.enuos.live.handler.PetsHandler;
import com.enuos.live.manager.ChannelManager;
import com.enuos.live.proto.p50001msg.P50001;
import com.enuos.live.rest.GameRemote;
import com.enuos.live.result.Result;
import com.enuos.live.util.RedisUtils;
import com.google.protobuf.InvalidProtocolBufferException;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * @ClassName PetsHandlerImpl
 * @Description: TODO 宠物处理
 * @Author xubin
 * @Date 2020/8/24
 * @Version V2.0
 **/
@Slf4j
@Component
public class PetsHandlerImpl implements PetsHandler {

    @Resource
    private GameRemote gameRemote;
    @Resource
    private RedisUtils redisUtils;

    /**
     * @MethodName: play
     * @Description: TODO 进入游戏
     * @Param: [channel, packet]
     * @Return: void
     * @Author: xubin
     * @Date: 13:15 2020/8/24
     **/
    @Override
    public void play(Channel channel, Packet packet) {
        long userId = packet.getUserId();
        Result result = gameRemote.getPetsInfo(userId);
        List<Map<String, Object>> petsInfos = result.getCode().equals(0) ? (List<Map<String, Object>>) result.getData() : null;
        if (ObjectUtil.isNotEmpty(petsInfos)) {
            P50001.P50001S2C.Builder builder = P50001.P50001S2C.newBuilder();
            P50001.PetsInfo.Builder sendBuilder = P50001.PetsInfo.newBuilder();
            for (int i = 0; i < petsInfos.size(); i++) {
                Map<String, Object> map = petsInfos.get(i);
                sendBuilder.setPetNick(String.valueOf(map.get("petNick"))); // 宠物昵称
                sendBuilder.setPetGrades(Integer.valueOf(map.get("petGrades").toString())); // 宠物等级
                sendBuilder.setPetId(Integer.valueOf(map.get("petId").toString())); // 宠物ID
                sendBuilder.setAllSaturat(Integer.valueOf(map.get("allSaturat").toString())); // 总饱食
                sendBuilder.setCurrentSaturat(Integer.valueOf(map.get("currentSaturat").toString())); // 当前饱食
                sendBuilder.setAllMoodNum(Integer.valueOf(map.get("allMoodNum").toString())); // 总心情
                sendBuilder.setCurrentMoodNum(Integer.valueOf(map.get("currentMoodNum").toString())); // 当前心情
                builder.setPets(i, sendBuilder);
            }

            builder.setResult(result.getCode());
            ChannelManager.sendPacketToUserId(new Packet(ChannelSet.CMD_PETS, PetsActionSet.P_PLAY, builder.build().toByteArray()), userId);
            if (!redisUtils.sHasKey(RedisKey.KEY_PETS_ONLINE, userId)) {
                redisUtils.sSet(RedisKey.KEY_PETS_ONLINE, userId); // 保存在宠物游戏中的用户到redis
            }
        }
    }

    /**
     * @MethodName: operation
     * @Description: TODO 操作
     * @Param: [channel, packet]
     * @Return: void
     * @Author: xubin
     * @Date: 17:06 2020/8/24
     **/
    @Override
    public void operation(Channel channel, Packet packet) {
        long userId = packet.getUserId();
        try {
            P50001.P500011C2S request = P50001.P500011C2S.parseFrom(packet.bytes);
            P50001.P500011S2C.Builder builder = P50001.P500011S2C.newBuilder();
            int operation = request.getOperation();//1.喂食  2.互动
            builder.setResult(operation);// 操作结果码
            Integer category = operation == 1 ? 2 : operation == 2 ? 3 : 0;
            if (category == 0) {
                builder.setResult(-1);
            } else {
                Result result = gameRemote.getOperation(userId, category);
                List<Map<String, Object>> categorys = result.getCode().equals(0) ? (List<Map<String, Object>>) result.getData() : null;
                if (ObjectUtil.isNotEmpty(categorys)) {
                    switch (operation) {
                        case 1:
                            for (int i = 0; i < categorys.size(); i++) {
                                String productCode = String.valueOf(categorys.get(i).get("productCode"));
                                int foodId;
                                switch (productCode) {
                                    case "nut_01":
                                        foodId = 1;
                                        break;
                                    case "biscuit_01":
                                        foodId = 2;
                                        break;
                                    case "honey_01":
                                        foodId = 3;
                                        break;
                                    case "roast_fish":
                                        foodId = 4;
                                        break;
                                    case "steak":
                                        foodId = 5;
                                        break;
                                    default:
                                        foodId = 0;
                                }
                                builder.setFoodIDs(i, foodId);// 当前可以使用食物 1.坚果 2.饼干 3.蜂蜜 4.烤鱼 5.肉排
                            }
                            break;
                        case 2:
                            for (int i = 0; i < categorys.size(); i++) {
                                String productCode = String.valueOf(categorys.get(i).get("productCode"));
                                int toysId;
                                switch (productCode) {
                                    case "bamboo":
                                        toysId = 1;
                                        break;
                                    case "balloon":
                                        toysId = 2;
                                        break;
                                    case "rope_skipping":
                                        toysId = 3;
                                        break;
                                    case "mic":
                                        toysId = 4;
                                        break;
                                    case "games_c":
                                        toysId = 5;
                                        break;
                                    default:
                                        toysId = 0;
                                }
                                builder.setToysIDs(i, toysId);// 当前可以使用玩具 1.竹子 2.气球 3.跳绳 4.麦克风 5.游戏机

                            }
                            break;
                        default:
                            log.warn("宠物没有该操作项,operation=[{}]", operation);
                    }
                }
            }

            ChannelManager.sendPacketToUserId(new Packet(ChannelSet.CMD_PETS, PetsActionSet.P_OPERATION, builder.build().toByteArray()), userId);
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }

    }

    /**
     * @MethodName: food
     * @Description: TODO 喂食
     * @Param: [channel, packet]
     * @Return: void
     * @Author: xubin
     * @Date: 17:29 2020/8/24
     **/
    @Override
    public void food(Channel channel, Packet packet) {
        long userId = packet.getUserId();
        try {
            P50001.P500012C2S request = P50001.P500012C2S.parseFrom(packet.bytes);
            P50001.P500012S2C.Builder builder = P50001.P500012S2C.newBuilder();
            P50001.PetsInfo.Builder sendBuilder = P50001.PetsInfo.newBuilder();
            int foodID = request.getFoodID(); // 1.坚果 2.饼干 3.蜂蜜 4.烤鱼 5.肉排

            builder.setResult(1);
            builder.setFoodID(foodID); // 喂食物ID
            builder.setPets(sendBuilder);
            ChannelManager.sendPacketToUserId(new Packet(ChannelSet.CMD_PETS, PetsActionSet.P_FOOD, builder.build().toByteArray()), userId);
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }

    }

    /**
     * @MethodName: toys
     * @Description: TODO 玩具
     * @Param: [channel, packet]
     * @Return: void
     * @Author: xubin
     * @Date: 17:34 2020/8/24
     **/
    @Override
    public void toys(Channel channel, Packet packet) {
        long userId = packet.getUserId();
        try {
            P50001.P500013C2S request = P50001.P500013C2S.parseFrom(packet.bytes);
            P50001.P500013S2C.Builder builder = P50001.P500013S2C.newBuilder();
            P50001.PetsInfo.Builder sendBuilder = P50001.PetsInfo.newBuilder();
            int foodID = request.getToysID(); //1.竹子 2.气球 3.跳绳 4.麦克风 5.游戏机

            builder.setResult(1);
            builder.setToysID(foodID);
            builder.setPets(sendBuilder);
            ChannelManager.sendPacketToUserId(new Packet(ChannelSet.CMD_PETS, PetsActionSet.P_TOYS, builder.build().toByteArray()), userId);
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
    }
}
