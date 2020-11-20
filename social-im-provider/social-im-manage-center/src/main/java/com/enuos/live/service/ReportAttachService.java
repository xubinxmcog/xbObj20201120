package com.enuos.live.service;

import com.enuos.live.pojo.ReportAttach;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

public interface ReportAttachService {

    int deleteByPrimaryKey(Integer id);

    int insert(ReportAttach record);

    int insertReportAttach( String tableName, ReportAttach record);

    void addReportAttachs( String tableName, ReportAttach reportAttach);

    ReportAttach selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(ReportAttach record);

}
