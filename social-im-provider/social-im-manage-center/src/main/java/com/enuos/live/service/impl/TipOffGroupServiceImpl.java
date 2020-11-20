package com.enuos.live.service.impl;

import cn.hutool.core.util.StrUtil;
import com.enuos.live.dto.TipOffsDTO;
import com.enuos.live.mapper.TipOffGroupMapper;
import com.enuos.live.mapper.UserMapper;
import com.enuos.live.pojo.ReportAttach;
import com.enuos.live.pojo.TipOffs;
import com.enuos.live.result.Result;
import com.enuos.live.service.ReportAttachService;
import com.enuos.live.service.TipOffsService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * @ClassName TipOffGroupServiceImpl
 * @Description: TODO 群组举报service
 * @Author xubin
 * @Date 2020/4/15
 * @Version V1.0
 **/
@Slf4j
@Component
public class TipOffGroupServiceImpl implements TipOffsService {

    @Autowired
    private TipOffGroupMapper tipOffGroupMapper;

    @Autowired
    private ReportAttachService reportAttachService;

    @Autowired
    private UserMapper userMapper;

    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public Result insert(TipOffs tipOffs) {

        log.info("群组举报入参=【{}】", tipOffs.toString());
        if (StrUtil.isEmpty(tipOffs.getUserName())) {
            tipOffs.setUserName(userMapper.selectByUserName(tipOffs.getUserId())); // 用户名称
        }
        if (null == tipOffs.getGroupAdminId() || 0 == tipOffs.getGroupAdminId()) {
            tipOffs.setGroupAdminId(tipOffGroupMapper.getGroupAdminId(tipOffs.getGroupId())); // 群主ID
        }
        if (StrUtil.isEmpty(tipOffs.getGroupAdminName())) {
            tipOffs.setGroupAdminName(userMapper.selectByUserName(tipOffs.getGroupAdminId())); // 群主名称
        }
        tipOffGroupMapper.insert(tipOffs);

        ReportAttach reportAttach = new ReportAttach();
        reportAttach.setReportFile(tipOffs.getReportFile());
        // 返回ID传给附件
        reportAttach.setReportId(tipOffs.getId());

        // 保存附件
        reportAttachService.addReportAttachs("tb_report_group_attach", reportAttach);

        return Result.success(tipOffs.getId());
    }

    @Override
    public Result queryTipOffList(TipOffsDTO dto) {
        PageHelper.startPage(dto.getPageNum(), dto.getPageSize());

        return Result.success(new PageInfo(tipOffGroupMapper.queryTipOffs(dto)));
    }

    @Override
    public Result queryByIdTipOff(TipOffsDTO dto) {

        return Result.success(tipOffGroupMapper.queryByIdTipOff(dto.getId()));
    }

    @Override
    public Result handleInfo(TipOffs tipOffs) {
        log.info("群组举报处理=【{}】", tipOffs.toString());
        // 向目标发送处理请求，执行处理

        // 保存处理结果
        tipOffGroupMapper.update(tipOffs);
        return Result.success();
    }

    @Override
    public String getCode() {
        return "REPORT_GROUP";
    }
}
