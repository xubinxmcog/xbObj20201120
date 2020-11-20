package com.enuos.live.mapper;

import com.enuos.live.pojo.Thesaurus30071;
import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Param;

/**
 * TODO 一站到底(Thesaurus)数据库访问层.
 *
 * @author WangCaiWen - missiw@163.com
 * @since 2020/7/8 16:01
 */

public interface Thesaurus30071Mapper {

  /**
   * TODO 插入新题.
   *
   * @param thesaurus30071 新题信息
   * @author WangCaiWen
   * @since 2020/7/16 - 2020/7/16
   */
  void insertNewProblem(Thesaurus30071 thesaurus30071);


  /**
   * TODO 获得一站到底题目.
   *
   * @return problem
   * @author WangCaiWen
   * @since 2020/7/8 - 2020/7/8
   */
  List<Map<String, Object>> getMustStandProblem();

  /**
   * TODO 查找新题是否存在.
   *
   * @param newProblem 新题
   * @return isExist
   * @author WangCaiWen
   * @since 2020/7/16 - 2020/7/16
   */
  Integer findProblemIsExist(@Param("newProblem") String newProblem);

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
