package com.enuos.live.server.handler;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ObjectUtil;
import com.enuos.live.dto.PetsInfoAndDressUpDTO;
import com.enuos.live.mapper.PetsInfoMapper;
import com.enuos.live.pojo.Currency;
import com.enuos.live.pojo.PetsDressUpQualityConfig;
import com.enuos.live.pojo.PetsInfo;
import com.enuos.live.rest.OrderFeign;
import com.enuos.live.rest.UserRemote;
import com.enuos.live.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @ClassName PetsUtils
 * @Description: TODO 宠物处理
 * @Author xubin
 * @Date 2020/10/28
 * @Version V2.0
 **/
@Slf4j
@Lazy
@Component
public class PetsHandler {

    @Autowired
    private PetsInfoMapper petsInfoMapper;

    @Autowired
    private UserRemote userRemote;

    @Autowired
    private OrderFeign orderFeign;


    public Object upPetsInfo(Map<String, Object> params) {
        PetsInfoAndDressUpDTO dressUpDTO = petsInfoMapper.getPetsInfoAndDressUp(MapUtil.getLong(params, "userId"), MapUtil.getLong(params, "petsId"));
        if (ObjectUtil.isNotEmpty(dressUpDTO)) {

            PetsInfo petsInfo = new PetsInfo();
            petsInfo.setId(dressUpDTO.getPetsId());

            Integer petLevel = dressUpDTO.getPetLevel();// 宠物等级
//            Double allMoodNum = dressUpDTO.getAllMoodNum(); // 总心情
//            Double allSaturat = dressUpDTO.getAllSaturat(); // 总饱食
            Double allNum = allSaturatOrMood(petLevel); // 总心情 // 总饱食

            Date saturatUpTime = dressUpDTO.getSaturatUpTime(); // 饱食度更新时间
            Date moodUpTime = dressUpDTO.getMoodUpTime(); // 心情更新时间

            Double currentMoodNum = dressUpDTO.getCurrentMoodNum(); // 当前心情
            Double currentSaturat = dressUpDTO.getCurrentSaturat(); // 当前饱食

            Integer effectGold = 0; // 金币获取速率(单位%)
            Integer effectBeFull = 0; // 饱食消耗速率(单位%)
            Integer effectMood = 0; // 心情消耗速率(单位%)
            Integer effectFood = 0; // 食物使用效果(单位%)
            Integer effectToys = 0; //  玩具使用效果(单位%)

            if (ObjectUtil.isNotEmpty(dressUpDTO.getEffectQualitys())) {
                for (PetsDressUpQualityConfig effectQuality : dressUpDTO.getEffectQualitys()) {
                    effectGold = effectGold + effectQuality.getEffectGold();
                    effectBeFull = effectBeFull + effectQuality.getEffectBeFull();
                    effectMood = effectMood + effectQuality.getEffectMood();
                    effectFood = effectFood + effectQuality.getEffectFood();
                    effectToys = effectToys + effectQuality.getEffectToys();
                }
            }

            Double beFullRate = minImpairment(petLevel, effectBeFull); // 每分钟扣减饱食度值
            Double moodRate = minImpairment(petLevel, effectMood); // 每分钟扣减心情值

            double afterSaturat = 0;
            if (currentSaturat > 0) {
                afterSaturat = afterSaturatMoodNum(saturatUpTime, currentSaturat, beFullRate); // 扣减后的饱食度
            }
            double afterMoodNum = 0;
            if (currentMoodNum > 0) {
                afterMoodNum = afterSaturatMoodNum(moodUpTime, currentMoodNum, moodRate);// 扣减后的心情值
            }

            // 判断饱食度和心情值范围, 符合范围才能获取金币
            if ((99 < currentMoodNum && currentMoodNum < (allNum - 50)) && (99 < currentSaturat && currentSaturat < (allNum - 50))) {
                Double goldRate = obtainGold(petLevel, effectGold); // 每分钟获取的金币
                long gainGold = (long) (timeMinDv(moodUpTime) * goldRate); // 获取的金币
                addGold(dressUpDTO.getUserId(), gainGold);
            }

            if (currentSaturat > 0 || currentMoodNum > 0) {
                petsInfo.setCurrentSaturat(afterSaturat);
                petsInfo.setCurrentMoodNum(afterMoodNum);
                petsInfo.setSaturatUpTime(new Date());
                petsInfo.setMoodUpTime(new Date());

                petsInfoMapper.updateByPrimaryKeySelective(petsInfo);
            }
        }

        return dressUpDTO;
    }

    public void addGold(Long userId, Long newGold) {
        Result result = userRemote.getCurrency(userId);
        Map<String, Object> userMap = result.getCode().equals(0) ? (Map<String, Object>) result.getData() : null;
        if (ObjectUtil.isEmpty(userMap)) {
            log.error("宠物成长,未查询到用户金币和钻石数据,userId=[{}],newGold=[{}]", userId, newGold);
            throw new DataIntegrityViolationException("数据异常,未查询到用户金币和钻石数据");
        }
        final long gold = MapUtil.getLong(userMap, "gold"); // 用户金币
        final long diamond = MapUtil.getLong(userMap, "diamond"); // 用户钻石
        Map<String, Object> billMap = new HashMap(); // 入账
        billMap.put("productName", "宠物成长金币");
        Currency currency = new Currency();
        currency.setUserId(userId);
        currency.setOriginalDiamond(diamond);
        currency.setOriginalGold(gold);

        long surplusGold = gold + newGold; // 新增后金币
        currency.setGold(surplusGold);
        billMap.put("price", newGold);
        billMap.put("priceType", 3); // 2钻石 3:金币

        Result result1 = userRemote.upUserCurrency(currency);
        if ((result1.getCode() != 0)) {
            log.warn("宠物成长金币发放失败, userId=[{}], newGold=[{}]", userId, newGold);
            throw new DataIntegrityViolationException("数据异常");
        }
        billMap.put("status", 1);
        billMap.put("userId", userId);
        orderFeign.entryBill(billMap);
    }

    public static void main(String[] args) throws ParseException {

        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date d2 = df.parse("2020-11-6 15:00:00");

//        System.out.println(timeMinDv(d2));
    }

    /**
     * 返回扣减后的 心情值 或 饱食度
     *
     * @param time    饱食度或心情 更新时间
     * @param current 当前心情或饱食度
     * @param rate    每分钟 扣减心情值 或 饱食度值
     * @return 扣减后的值
     */
    public double afterSaturatMoodNum(Date time, Double current, Double rate) {
        double afterNum = 0;
        long saturatMinDv = timeMinDv(time); // 分钟差值
        if (saturatMinDv > 0) {
            afterNum = current - (rate * saturatMinDv); // 扣减后的饱食
            if (afterNum < 0) {
                afterNum = 0;
            }
        }
        return afterNum;
    }

    /**
     * 计算目标时间和当前系统时间分钟的差值
     *
     * @param date 时间
     * @return 分钟
     */
    public long timeMinDv(Date date) {
        Date d = new Date();
        long time = d.getTime();
        long time1 = date.getTime();
        return (time - time1) / (1000 * 60);
    }

    /**
     * 根据宠物等级获取饱食度和心情值上限
     *
     * @param level 等级
     * @return
     */
    public Double allSaturatOrMood(Integer level) {

        return Double.valueOf(48 * level + 1152);

    }

    /**
     * 饱食/心情每分钟减（2+0.08*lv）点
     *
     * @param level 等级
     * @param rate  速率
     * @return
     */
    public static Double minImpairment(Integer level, Integer rate) {
        double v = 2 + 0.08 * level;
        double i = rate / 100.0;
        return (v - (v * i));
    }

    /**
     * 每分钟获得（10+0.4*lv）金币
     *
     * @param level 等级
     * @param rate  速率
     * @return
     */
    public Double obtainGold(Integer level, Integer rate) {
        double v = 10 + 0.4 * level;
        double i = rate / 100.0;

        return v + (v * i);
    }

    public void petsDressUpQualityNON_NULL(PetsDressUpQualityConfig effectQuality) {
        if (ObjectUtil.isNotEmpty(effectQuality)) {
            if (effectQuality.getEffectBeFull() == 0) {
                effectQuality.setEffectBeFull(null);
            }
            if (effectQuality.getEffectFood() == 0) {
                effectQuality.setEffectFood(null);
            }
            if (effectQuality.getEffectGold() == 0) {
                effectQuality.setEffectGold(null);
            }
            if (effectQuality.getEffectMood() == 0) {
                effectQuality.setEffectMood(null);
            }
            if (effectQuality.getEffectToys() == 0) {
                effectQuality.setEffectToys(null);
            }
        }
    }
}
