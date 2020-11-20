package com.enuos.live.mapper;

import com.enuos.live.pojo.Thesaurus30051;
import java.util.Map;
import org.apache.ibatis.annotations.Param;

/**
 * TODO 谁是卧底(Thesaurus)数据库访问层.
 *
 * @author WangCaiWen - missiw@163.com
 * @version 2.0
 * @since 2020/7/1 12:46
 */

public interface Thesaurus30051Mapper {

  /**
   * TODO 插入新词.
   *
   * @param thesaurus30051 新词信息
   * @author WangCaiWen
   * @since 2020/7/16 - 2020/7/16
   */
  void insertNewWords(Thesaurus30051 thesaurus30051);

  /**
   * TODO 获得谁是卧底词汇.
   *
   * @return words
   * @author WangCaiWen
   * @since 2020/7/8 - 2020/7/8
   */
  Map<String, Object> getWhoIsSpyWords();

  /**
   * TODO 查找新词是否存在.
   *
   * @param massWords 平民词
   * @param spyWords 间谍词
   * @return isExist
   * @author WangCaiWen
   * @since 2020/7/16 - 2020/7/16
   */
  Integer findWordsIsExist(@Param("massWords") String massWords, @Param("spyWords") String spyWords);

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
