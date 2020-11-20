package com.enuos.live.mapper;

import com.enuos.live.pojo.Thesaurus30041;
import java.util.Map;
import org.apache.ibatis.annotations.Param;

/**
 * TODO 你画我猜(Thesaurus)数据库访问层.
 *
 * @author WangCaiWen - missiw@163.com
 * @version 2.0
 * @since 2020/7/16 14:18
 */

public interface Thesaurus30041Mapper {

  /**
   * TODO 插入新词.
   *
   * @param thesaurus30041 新词信息
   * @author WangCaiWen
   * @since 2020/7/16 - 2020/7/16
   */
  void insertNewWords(Thesaurus30041 thesaurus30041);

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
}
