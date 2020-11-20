package com.enuos.live.service;

import com.enuos.live.pojo.QleNews;
import com.enuos.live.result.Result;

/**
 * 资讯
 */
public interface QleNewsService {

    /**
     * @MethodName: insert
     * @Description: TODO 新增资讯
     * @Param: [record]
     * @Return: Result
     * @Author: xubin
     * @Date: 13:18 2020/7/22
    **/
    Result insert(QleNews record);

    /**
     * @MethodName: insert
     * @Description: TODO 资讯详情
     * @Param: [id]
     * @Return: Result
     * @Author: xubin
     * @Date: 13:18 2020/7/22
     **/
    Result selectByPrimaryKey(Integer id);

    /**
     * @MethodName: insert
     * @Description: TODO 更新资讯
     * @Param: [record]
     * @Return: Result
     * @Author: xubin
     * @Date: 13:18 2020/7/22
     **/
    Result updateByPrimaryKeySelective(QleNews record);

    /**
     * @MethodName: insert
     * @Description: TODO 资讯列表
     * @Param: [pageNum, pageSize]
     * @Return: Result
     * @Author: xubin
     * @Date: 13:18 2020/7/22
     **/
    Result selectNewsList(Integer pageNum, Integer pageSize);

    /**
     * @MethodName: insert
     * @Description: TODO 资讯搜索
     * @Param: [title]
     * @Return: Result
     * @Author: xubin
     * @Date: 13:18 2020/7/22
     **/
    Result selectNewsTitle(String title);
}
