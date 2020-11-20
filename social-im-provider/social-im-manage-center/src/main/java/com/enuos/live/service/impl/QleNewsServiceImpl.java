package com.enuos.live.service.impl;

import com.enuos.live.error.ErrorCode;
import com.enuos.live.mapper.QleNewsMapper;
import com.enuos.live.pojo.QleNews;
import com.enuos.live.result.Result;
import com.enuos.live.service.QleNewsService;
import com.enuos.live.utils.page.PageInfo;
import com.github.pagehelper.PageHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @ClassName QleNewsServiceImpl
 * @Description: TODO 资讯
 * @Author xubin
 * @Date 2020/7/22
 * @Version V2.0
 **/
@Slf4j
@Service
public class QleNewsServiceImpl implements QleNewsService {

    @Autowired
    private QleNewsMapper qleNewsMapper;

    /**
     * @MethodName: insert
     * @Description: TODO 新增资讯
     * @Param: [record]
     * @Return: Result
     * @Author: xubin
     * @Date: 13:22 2020/7/22
     **/
    @Override
    public Result insert(QleNews record) {
        log.info("新增资讯");

        int insert = qleNewsMapper.insert(record);

        if (insert < 1) {
            return Result.error();
        }
        return Result.success(insert);
    }

    /**
     * @MethodName: insert
     * @Description: TODO 资讯详情
     * @Param: [id]
     * @Return: Result
     * @Author: xubin
     * @Date: 13:22 2020/7/22
     **/
    @Override
    public Result selectByPrimaryKey(Integer id) {
        log.info("资讯详情, id=[{}]", id);

        return Result.success(qleNewsMapper.selectByPrimaryKey(id));
    }

    /**
     * @MethodName: insert
     * @Description: TODO 更新资讯
     * @Param: [record]
     * @Return: Result
     * @Author: xubin
     * @Date: 13:23 2020/7/22
     **/
    @Override
    public Result updateByPrimaryKeySelective(QleNews record) {
        log.info("更新资讯");

        if (record.getId() == null) {
            return Result.error(ErrorCode.EXCEPTION_CODE, "ID为空");
        }

        int insert = qleNewsMapper.updateByPrimaryKeySelective(record);

        if (insert < 1) {
            return Result.error();
        }
        return Result.success(insert);
    }

    /**
     * @MethodName: insert
     * @Description: TODO 资讯列表
     * @Param: [pageNum, pageSize]
     * @Return: Result
     * @Author: xubin
     * @Date: 13:23 2020/7/22
     **/
    @Override
    public Result selectNewsList(Integer pageNum, Integer pageSize) {
        log.info("资讯列表");

        PageHelper.startPage(pageNum, pageSize);

        return Result.success(new PageInfo(qleNewsMapper.selectNewsList()));
    }

    /**
     * @MethodName: insert
     * @Description: TODO 资讯搜索
     * @Param: [title: 标题模糊搜索]
     * @Return: Result
     * @Author: xubin
     * @Date: 13:23 2020/7/22
     **/
    @Override
    public Result selectNewsTitle(String title) {
        log.info("资讯搜索, title=[{}]", title);
        return Result.success(qleNewsMapper.selectNewsTitle(title));
    }
}
