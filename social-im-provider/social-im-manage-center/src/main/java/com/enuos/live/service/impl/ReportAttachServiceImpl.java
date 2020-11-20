package com.enuos.live.service.impl;

import cn.hutool.core.util.StrUtil;
import com.enuos.live.mapper.ReportAttachMapper;
import com.enuos.live.pojo.ReportAttach;
import com.enuos.live.service.ReportAttachService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

//import com.enuos.live.utils.FtpUtils;

/**
 * @ClassName ReportAttachServiceImpl
 * @Description: TODO  举报附件 service
 * @Author xubin
 * @Date 2020/4/15
 * @Version V1.0
 **/
@Slf4j
@Service
public class ReportAttachServiceImpl implements ReportAttachService {

    @Autowired
    private ReportAttachMapper reportAttachMapper;

//    @Autowired
//    private FtpUtils ftpUtils;

    @Override
    public int deleteByPrimaryKey(Integer id) {
        return 0;
    }

    @Override
    public int insert(ReportAttach record) {
        return 0;
    }

    @Override
    public int insertReportAttach(String tableName, ReportAttach record) {
        return 0;
    }

    /**
     * @MethodName: addReportAttachs
     * @Description: TODO 批量添加 附件
     * @Param: [tableName, reportAttachs]
     * @Return: void
     * @Author: xubin
     * @Date: 2020/4/15
     **/
    @Override
    public void addReportAttachs(String tableName, ReportAttach reportAttach) {

        if (StrUtil.isEmpty(tableName) && ObjectUtils.isEmpty(reportAttach)) {
            log.error("批量添加 附件参数不能为空tableNma={}, reportAttach={}", tableName, reportAttach);
            return;
        }
        if (StrUtil.isEmpty(reportAttach.getReportFile())) {
            log.warn("举报文件为空,不做添加操作! ");
            return;
        }
        String[] reportFIles = reportAttach.getReportFile().split(",");
        List<ReportAttach> reportAttaches = new CopyOnWriteArrayList<>();
        for (String reportFIle : reportFIles) {
            ReportAttach reportAttach1 = new ReportAttach();
            reportAttach1.setReportId(reportAttach.getReportId());
            reportAttach1.setReportFile(reportFIle);
            reportAttaches.add(reportAttach1);
        }
        reportAttachMapper.addReportAttachs(tableName, reportAttaches);

    }

    @Override
    public ReportAttach selectByPrimaryKey(Integer id) {
        return null;
    }

    @Override
    public int updateByPrimaryKeySelective(ReportAttach record) {
        return 0;
    }

}
