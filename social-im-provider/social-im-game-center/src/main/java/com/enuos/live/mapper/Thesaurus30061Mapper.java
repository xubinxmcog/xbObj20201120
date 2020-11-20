package com.enuos.live.mapper;

import com.enuos.live.pojo.Thesaurus30061;
import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Param;

/**
 * TODO 你说我猜(Thesaurus)数据库访问层.
 *
 * @author WangCaiWen - missiw@163.com
 * @version 2.0
 * @since 2020/7/16 12:36
 */

public interface Thesaurus30061Mapper {

  /**
   * TODO 插入新词.
   *
   * @param thesaurus30061 新词信息
   * @author WangCaiWen
   * @since 2020/7/16 - 2020/7/16
   */
  void insertNewWords(Thesaurus30061 thesaurus30061);

  /**
   * TODO 查找新词是否存在.
   *
   * @param newWords 新词
   * @return isExist
   * @author WangCaiWen
   * @since 2020/7/16 - 2020/7/16
   */
  Integer findWordsIsExist(@Param("newWords") String newWords);

  /**
   * TODO 获得今日提交数量.
   *
   * @param params 参数
   * @return quantity
   * @author WangCaiWen
   * @since 2020/7/16 - 2020/7/16
   */
  Integer getTodaySubmitQuantity(Map<String, Object> params);

  /**
   * TODO 获得你说我猜词汇.
   *
   * @return words 词汇列表
   * @author WangCaiWen - 1443710411@qq.com
   * @date 2020/8/4 13:13
   * @since 2020/8/4 13:13
   */
  List<Map<String, Object>> getGuessedSaidWords();

}
