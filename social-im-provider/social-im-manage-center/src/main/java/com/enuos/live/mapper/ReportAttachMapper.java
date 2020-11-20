package com.enuos.live.mapper;

import com.enuos.live.pojo.ReportAttach;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ReportAttachMapper {

    int deleteByPrimaryKey(Integer id);

    int insert(ReportAttach record);

    int insertReportAttach(@Param("tableName") String tableName, ReportAttach record);

    void addReportAttachs(@Param("tableName") String tableName, @Param("list") List<ReportAttach> list);

    ReportAttach selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(ReportAttach record);

    List<ReportAttach> getTipOffFile(@Param("tableName") String tableName, @Param("reportId") Integer reportId);

}