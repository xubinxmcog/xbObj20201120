package com.enuos.live.handle.game.f30051;

import com.google.common.collect.Maps;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 * TODO 谁是卧底.词汇.应急方案.
 *
 * @author wangcaiwen|1443****11@qq.com
 * @version v1.0.0
 * @since 2020/7/6 9:38
 */

public class FindSpyPlan {

  private static HashMap<Integer, String> MASS_MAP = Maps.newHashMap();
  static {
    MASS_MAP.put(1, "CS");
    MASS_MAP.put(2, "安踏");
    MASS_MAP.put(3, "暗恋");
    MASS_MAP.put(4, "白菜");
    MASS_MAP.put(5, "白发巫女");
    MASS_MAP.put(6, "班主任");
    MASS_MAP.put(7, "包青天");
    MASS_MAP.put(8, "保安");
    MASS_MAP.put(9, "比萨");
    MASS_MAP.put(10, "壁纸");
    MASS_MAP.put(11, "冰棍");
    MASS_MAP.put(12, "病毒");
    MASS_MAP.put(13, "玻璃");
    MASS_MAP.put(14, "成功");
    MASS_MAP.put(15, "成吉思汗");
    MASS_MAP.put(16, "橙子");
    MASS_MAP.put(17, "丑小鸭");
    MASS_MAP.put(18, "初吻");
    MASS_MAP.put(19, "唇膏");
    MASS_MAP.put(20, "大白兔");
    MASS_MAP.put(21, "蛋糕");
    MASS_MAP.put(22, "地铁酷跑");
    MASS_MAP.put(23, "电影");
    MASS_MAP.put(24, "董永");
    MASS_MAP.put(25, "豆奶");
    MASS_MAP.put(26, "端午节");
    MASS_MAP.put(27, "反弹琵琶");
    MASS_MAP.put(28, "反贼");
    MASS_MAP.put(29, "福尔摩斯");
    MASS_MAP.put(30, "富二代");
    MASS_MAP.put(31, "高跟鞋");
    MASS_MAP.put(32, "高跟鞋");
    MASS_MAP.put(33, "高血糖");
    MASS_MAP.put(34, "歌手");
    MASS_MAP.put(35, "公交");
    MASS_MAP.put(36, "宫保鸡丁");
    MASS_MAP.put(37, "贵妃醉酒");
    MASS_MAP.put(38, "郭德纲");
    MASS_MAP.put(39, "果粒橙");
    MASS_MAP.put(40, "过山车");
    MASS_MAP.put(41, "孩子");
    MASS_MAP.put(42, "海苔");
    MASS_MAP.put(43, "海豚");
    MASS_MAP.put(44, "海蟹");
    MASS_MAP.put(45, "汉堡包");
    MASS_MAP.put(46, "杭州");
    MASS_MAP.put(47, "红烧牛肉面");
    MASS_MAP.put(48, "红烧牛肉面");
    MASS_MAP.put(49, "荒野乱斗");
    MASS_MAP.put(50, "皇帝");
    MASS_MAP.put(51, "黄金");
    MASS_MAP.put(52, "婚纱");
    MASS_MAP.put(53, "激情四射");
    MASS_MAP.put(54, "吉他");
    MASS_MAP.put(55, "间谍");
    MASS_MAP.put(56, "奖牌");
    MASS_MAP.put(57, "降龙十八掌");
    MASS_MAP.put(58, "饺子");
    MASS_MAP.put(59, "结婚");
    MASS_MAP.put(60, "金庸");
    MASS_MAP.put(61, "警察");
    MASS_MAP.put(62, "烤肉");
    MASS_MAP.put(63, "口香糖");
    MASS_MAP.put(64, "裤子");
    MASS_MAP.put(65, "垃圾桶");
    MASS_MAP.put(66, "辣椒");
    MASS_MAP.put(67, "兰州拉面");
    MASS_MAP.put(68, "老佛爷");
    MASS_MAP.put(69, "连连看");
    MASS_MAP.put(70, "连连看");
    MASS_MAP.put(71, "流星花园");
    MASS_MAP.put(72, "裸婚");
    MASS_MAP.put(73, "绿豆");
    MASS_MAP.put(74, "绿豆粥");
    MASS_MAP.put(75, "妈");
    MASS_MAP.put(76, "麻辣");
    MASS_MAP.put(77, "麻婆豆腐");
    MASS_MAP.put(78, "麻雀");
    MASS_MAP.put(79, "买一送一");
    MASS_MAP.put(80, "麦克风");
    MASS_MAP.put(81, "玫瑰");
    MASS_MAP.put(82, "眉毛");
    MASS_MAP.put(83, "梅花");
    MASS_MAP.put(84, "孟非");
    MASS_MAP.put(85, "面包");
    MASS_MAP.put(86, "摩托车");
    MASS_MAP.put(87, "魔兽争霸");
    MASS_MAP.put(88, "魔术师");
    MASS_MAP.put(89, "木瓜");
    MASS_MAP.put(90, "沐浴露");
    MASS_MAP.put(91, "那英");
    MASS_MAP.put(92, "你说我猜");
    MASS_MAP.put(93, "牛奶");
    MASS_MAP.put(94, "牛肉干");
    MASS_MAP.put(95, "女儿");
    MASS_MAP.put(96, "女主播");
    MASS_MAP.put(97, "拍照");
    MASS_MAP.put(98, "泡泡糖");
    MASS_MAP.put(99, "飘柔");
    MASS_MAP.put(100, "葡萄");
  }

  private static HashMap<Integer, String> SPY_MAP = Maps.newHashMap();
  static {
    SPY_MAP.put(1, "CF");
    SPY_MAP.put(2, "特步");
    SPY_MAP.put(3, "相思");
    SPY_MAP.put(4, "生菜");
    SPY_MAP.put(5, "黑发魔女");
    SPY_MAP.put(6, "辅导员");
    SPY_MAP.put(7, "狄仁杰");
    SPY_MAP.put(8, "保镖");
    SPY_MAP.put(9, "馅饼");
    SPY_MAP.put(10, "壁画");
    SPY_MAP.put(11, "雪糕");
    SPY_MAP.put(12, "细菌");
    SPY_MAP.put(13, "水晶");
    SPY_MAP.put(14, "胜利");
    SPY_MAP.put(15, "努尔哈赤");
    SPY_MAP.put(16, "橘子");
    SPY_MAP.put(17, "灰姑娘");
    SPY_MAP.put(18, "初恋");
    SPY_MAP.put(19, "口红");
    SPY_MAP.put(20, "金丝猴");
    SPY_MAP.put(21, "蛋挞");
    SPY_MAP.put(22, "神庙逃亡");
    SPY_MAP.put(23, "电视");
    SPY_MAP.put(24, "许仙");
    SPY_MAP.put(25, "豆浆");
    SPY_MAP.put(26, "中秋节");
    SPY_MAP.put(27, "乱弹棉花");
    SPY_MAP.put(28, "内奸");
    SPY_MAP.put(29, "工藤新一");
    SPY_MAP.put(30, "高富帅");
    SPY_MAP.put(31, "增高鞋");
    SPY_MAP.put(32, "长统靴");
    SPY_MAP.put(33, "高血压");
    SPY_MAP.put(34, "艺人");
    SPY_MAP.put(35, "地铁");
    SPY_MAP.put(36, "辣子鸡丁");
    SPY_MAP.put(37, "黛玉葬花");
    SPY_MAP.put(38, "周立波");
    SPY_MAP.put(39, "鲜橙多");
    SPY_MAP.put(40, "碰碰车");
    SPY_MAP.put(41, "子女");
    SPY_MAP.put(42, "海带");
    SPY_MAP.put(43, "海狮");
    SPY_MAP.put(44, "海虾");
    SPY_MAP.put(45, "肉夹馍");
    SPY_MAP.put(46, "苏州");
    SPY_MAP.put(47, "香辣牛肉面");
    SPY_MAP.put(48, "老坛酸菜面");
    SPY_MAP.put(49, "绝地求生");
    SPY_MAP.put(50, "玉帝");
    SPY_MAP.put(51, "白金");
    SPY_MAP.put(52, "礼服");
    SPY_MAP.put(53, "活蹦乱跳");
    SPY_MAP.put(54, "琵琶");
    SPY_MAP.put(55, "奸细");
    SPY_MAP.put(56, "金牌");
    SPY_MAP.put(57, "九阴白骨爪");
    SPY_MAP.put(58, "包子");
    SPY_MAP.put(59, "订婚");
    SPY_MAP.put(60, "古龙");
    SPY_MAP.put(61, "捕快");
    SPY_MAP.put(62, "涮肉");
    SPY_MAP.put(64, "袜子");
    SPY_MAP.put(65, "回收站");
    SPY_MAP.put(66, "芥末");
    SPY_MAP.put(67, "沙县小吃");
    SPY_MAP.put(68, "老天爷");
    SPY_MAP.put(69, "消消乐");
    SPY_MAP.put(70, "泡泡龙");
    SPY_MAP.put(71, "花样男子");
    SPY_MAP.put(72, "闪婚");
    SPY_MAP.put(73, "黄豆");
    SPY_MAP.put(74, "红豆粥");
    SPY_MAP.put(75, "娘");
    SPY_MAP.put(76, "香辣");
    SPY_MAP.put(77, "皮蛋豆腐");
    SPY_MAP.put(78, "乌鸦");
    SPY_MAP.put(79, "再来一瓶");
    SPY_MAP.put(80, "扩音器");
    SPY_MAP.put(81, "月季");
    SPY_MAP.put(82, "胡须");
    SPY_MAP.put(83, "樱花");
    SPY_MAP.put(84, "乐嘉");
    SPY_MAP.put(85, "蛋糕");
    SPY_MAP.put(86, "电动车");
    SPY_MAP.put(87, "魔兽世界");
    SPY_MAP.put(88, "魔法师");
    SPY_MAP.put(89, "黄瓜");
    SPY_MAP.put(90, "沐浴盐");
    SPY_MAP.put(91, "韩红");
    SPY_MAP.put(92, "你画我猜");
    SPY_MAP.put(93, "豆浆");
    SPY_MAP.put(94, "猪肉脯");
    SPY_MAP.put(95, "闺女");
    SPY_MAP.put(96, "女主持");
    SPY_MAP.put(97, "录像");
    SPY_MAP.put(98, "棒棒糖");
    SPY_MAP.put(99, "清扬");
    SPY_MAP.put(100, "提子");
  }

  /**
   * TODO 获得谁是卧底.词汇
   *
   * @return [词汇信息]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/7/6 21:24
   * @update 2020/7/6 21:24
   */
  public static Map<String, Object> getVocabulary() {
    Map<String, Object> result = Maps.newHashMap();
    int index = ThreadLocalRandom.current().nextInt(100) + 1;
    result.put("lexiconNo", index);
    result.put("lexiconMass", MASS_MAP.get(index));
    result.put("lexiconSpy", SPY_MAP.get(index));
    return result;
  }

}
