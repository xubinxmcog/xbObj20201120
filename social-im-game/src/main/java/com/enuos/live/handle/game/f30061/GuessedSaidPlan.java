package com.enuos.live.handle.game.f30061;

import com.enuos.live.utils.StringUtils;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 * TODO 你说我猜.词汇.应急方案.
 *
 * @author wangcaiwen|1443****11@qq.com
 * @version v1.0.0
 * @since 2020/8/4 11:03
 */

public class GuessedSaidPlan {

  private static HashMap<Integer, String> WORD = Maps.newHashMap();
  static {
    WORD.put(1, "KTV");
    WORD.put(2, "WC");
    WORD.put(3, "拔苗助长");
    WORD.put(4, "抱头鼠窜");
    WORD.put(5, "蹦极");
    WORD.put(6, "别墅");
    WORD.put(7, "冰雹");
    WORD.put(8, "不入虎穴焉得虎子");
    WORD.put(9, "布娃娃");
    WORD.put(10, "餐巾纸");
    WORD.put(11, "仓库");
    WORD.put(12, "插排");
    WORD.put(13, "城管");
    WORD.put(14, "愁眉苦脸");
    WORD.put(15, "除草剂");
    WORD.put(16, "呲牙咧嘴");
    WORD.put(17, "瓷器");
    WORD.put(18, "打草惊蛇");
    WORD.put(19, "打火机");
    WORD.put(20, "大脖子病");
    WORD.put(21, "大头鱼");
    WORD.put(22, "丁字裤");
    WORD.put(23, "东张西望");
    WORD.put(24, "豆沙包");
    WORD.put(25, "对牛弹琴");
    WORD.put(26, "耳机");
    WORD.put(27, "放风筝");
    WORD.put(28, "飞碟");
    WORD.put(29, "蜂蜜");
    WORD.put(30, "凤姐");
    WORD.put(31, "负荆请罪");
    WORD.put(32, "狗急跳墙");
    WORD.put(33, "汉堡");
    WORD.put(34, "呼风唤雨");
    WORD.put(35, "虎背熊腰");
    WORD.put(36, "虎口拔牙");
    WORD.put(37, "虎头蛇尾");
    WORD.put(38, "花前月下");
    WORD.put(39, "花仙子");
    WORD.put(40, "画饼充饥");
    WORD.put(41, "画龙点睛");
    WORD.put(42, "画蛇添足");
    WORD.put(43, "灰太狼");
    WORD.put(44, "挥汗如雨");
    WORD.put(45, "回眸一笑");
    WORD.put(46, "火烧眉毛");
    WORD.put(47, "击剑");
    WORD.put(48, "鸡飞狗跳");
    WORD.put(49, "鸡鸣狗盗");
    WORD.put(50, "激光");
    WORD.put(51, "监狱");
    WORD.put(52, "见钱眼开");
    WORD.put(53, "僵尸");
    WORD.put(54, "结婚证");
    WORD.put(55, "金鸡独立");
    WORD.put(56, "惊弓之鸟");
    WORD.put(57, "酒鬼");
    WORD.put(58, "刻舟求剑");
    WORD.put(59, "空格键");
    WORD.put(60, "口红");
    WORD.put(61, "癞蛤蟆");
    WORD.put(62, "狼吞虎咽");
    WORD.put(63, "雷人");
    WORD.put(64, "泪流满面");
    WORD.put(65, "刘翔");
    WORD.put(66, "落井下石");
    WORD.put(67, "盲人摸象");
    WORD.put(68, "眉飞色舞");
    WORD.put(69, "孟婆汤");
    WORD.put(70, "米老鼠");
    WORD.put(71, "目不转睛");
    WORD.put(72, "目瞪口呆");
    WORD.put(73, "奶牛");
    WORD.put(74, "牛奶");
    WORD.put(75, "牛头马面");
    WORD.put(76, "捧腹大笑");
    WORD.put(77, "七上八下");
    WORD.put(78, "秦始皇兵马俑");
    WORD.put(79, "人民币");
    WORD.put(80, "日环食");
    WORD.put(81, "如鱼得水");
    WORD.put(82, "三头六臂");
    WORD.put(83, "三峡大坝");
    WORD.put(84, "三长两短");
    WORD.put(85, "沙漏");
    WORD.put(86, "沙僧");
    WORD.put(87, "山药");
    WORD.put(88, "山楂");
    WORD.put(89, "射雕英雄传");
    WORD.put(90, "升旗");
    WORD.put(91, "圣经");
    WORD.put(92, "手术刀");
    WORD.put(93, "手纸");
    WORD.put(94, "守株待兔");
    WORD.put(95, "鼠目寸光");
    WORD.put(96, "树苗");
    WORD.put(97, "帅哥");
    WORD.put(98, "水漫金山");
    WORD.put(99, "司马光");
    WORD.put(100, "台灯");
    WORD.put(101, "天文望远镜");
    WORD.put(102, "同流合污");
    WORD.put(103, "痛哭流涕");
    WORD.put(104, "头破血流");
    WORD.put(105, "娃哈哈");
    WORD.put(106, "外星人");
    WORD.put(107, "亡羊补牢");
    WORD.put(108, "望梅止渴");
    WORD.put(109, "五体投地");
    WORD.put(110, "瞎猫遇见死老鼠");
    WORD.put(111, "仙人掌");
    WORD.put(112, "胸无点墨");
    WORD.put(113, "掩耳盗铃");
    WORD.put(114, "眼高手低");
    WORD.put(115, "眼镜框");
    WORD.put(116, "羊入虎口");
    WORD.put(117, "羊驼");
    WORD.put(118, "咬牙切齿");
    WORD.put(119, "一手遮天");
    WORD.put(120, "游泳衣");
  }

  private static HashMap<Integer, String> HINT = Maps.newHashMap();
  static {
    HINT.put(1, "场所");
    HINT.put(2, "场所");
    HINT.put(3, "成语");
    HINT.put(4, "成语");
    HINT.put(5, "体育运动");
    HINT.put(6, "建筑");
    HINT.put(7, "天气");
    HINT.put(8, "俗语");
    HINT.put(9, "玩具");
    HINT.put(10, "餐具");
    HINT.put(11, "建筑");
    HINT.put(12, "家用电器");
    HINT.put(13, "职业");
    HINT.put(14, "成语");
    HINT.put(15, "药物");
    HINT.put(16, "表情");
    HINT.put(17, "容器");
    HINT.put(18, "成语");
    HINT.put(19, "日用品");
    HINT.put(20, "病症");
    HINT.put(21, "水产类");
    HINT.put(22, "衣物");
    HINT.put(23, "成语");
    HINT.put(24, "食品");
    HINT.put(25, "成语");
    HINT.put(26, "电子产品");
    HINT.put(27, "运动");
    HINT.put(28, "交通工具");
    HINT.put(29, "食品");
    HINT.put(30, "网络名人");
    HINT.put(31, "成语");
    HINT.put(32, "成语");
    HINT.put(33, "食品");
    HINT.put(34, "成语");
    HINT.put(35, "成语");
    HINT.put(36, "成语");
    HINT.put(37, "成语");
    HINT.put(38, "场景");
    HINT.put(39, "神话人物");
    HINT.put(40, "成语");
    HINT.put(41, "成语");
    HINT.put(42, "成语");
    HINT.put(43, "动画人物");
    HINT.put(44, "成语");
    HINT.put(45, "成语");
    HINT.put(46, "成语");
    HINT.put(47, "体育比赛");
    HINT.put(48, "成语");
    HINT.put(49, "成语");
    HINT.put(50, "武器");
    HINT.put(51, "场所");
    HINT.put(52, "成语");
    HINT.put(53, "神话人物");
    HINT.put(54, "证件");
    HINT.put(55, "成语");
    HINT.put(56, "成语");
    HINT.put(57, "一类人");
    HINT.put(58, "成语");
    HINT.put(59, "电子部件");
    HINT.put(60, "化妆品");
    HINT.put(61, "益虫");
    HINT.put(62, "成语");
    HINT.put(63, "网络语");
    HINT.put(64, "成语");
    HINT.put(65, "人名");
    HINT.put(66, "行为");
    HINT.put(67, "成语");
    HINT.put(68, "成语");
    HINT.put(69, "饮料");
    HINT.put(70, "动画人物");
    HINT.put(71, "成语");
    HINT.put(72, "成语");
    HINT.put(73, "动物");
    HINT.put(74, "饮料");
    HINT.put(75, "成语");
    HINT.put(76, "成语");
    HINT.put(77, "成语");
    HINT.put(78, "文物古迹");
    HINT.put(79, "货币");
    HINT.put(80, "天文现象");
    HINT.put(81, "成语");
    HINT. put(82, "成语");
    HINT.put(83, "水利类");
    HINT.put(84, "成语");
    HINT.put(85, "工具");
    HINT.put(86, "神话人物");
    HINT.put(87, "蔬菜");
    HINT.put(88, "水果");
    HINT.put(89, "小说");
    HINT.put(90, "行为");
    HINT.put(91, "书籍");
    HINT.put(92, "医疗器械");
    HINT.put(93, "日用品");
    HINT.put(94, "成语");
    HINT.put(95, "成语");
    HINT.put(96, "植物");
    HINT.put(97, "一类人");
    HINT.put(98, "神话故事");
    HINT.put(99, "古代名人");
    HINT.put(100, "电器");
    HINT.put(101, "光学仪器");
    HINT.put(102, "成语");
    HINT.put(103, "成语");
    HINT.put(104, "成语");
    HINT.put(105, "饮料");
    HINT.put(106, "生物");
    HINT.put(107, "成语");
    HINT.put(108, "成语");
    HINT.put(109, "成语");
    HINT.put(110, "俗语");
    HINT.put(111, "植物");
    HINT.put(112, "成语");
    HINT.put(113, "成语");
    HINT.put(114, "成语");
    HINT.put(115, "饰品");
    HINT.put(116, "成语");
    HINT.put(117, "动物");
    HINT.put(118, "成语");
    HINT.put(119, "成语");
    HINT.put(120, "服装");
  }

  /**
   * TODO 获得你说我猜.词汇
   *
   * @return [词汇信息]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/8/4 13:08
   * @update 2020/8/4 13:08
   */
  public static List<Map<String, Object>> getVocabulary() {
    List<Map<String, Object>> resultList = Lists.newArrayList();
    List<Integer> indexList = Lists.newLinkedList();
    while (indexList.size() < 6) {
      int index = ThreadLocalRandom.current().nextInt(120) + 1;
      if (!indexList.contains(index)) {
        indexList.add(index);
      }
    }
    Map<String, Object> wordMap;
    for (Integer integer : indexList) {
      wordMap = Maps.newHashMap();
      wordMap.put("lexicon", WORD.get(integer));
      wordMap.put("lexiconHint", HINT.get(integer));
      wordMap.put("lexiconWords", StringUtils.nvl(WORD.get(integer)).length());
      resultList.add(wordMap);
    }
    return resultList;
  }
}
