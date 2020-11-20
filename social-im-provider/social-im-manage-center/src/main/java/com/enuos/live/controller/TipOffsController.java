package com.enuos.live.controller;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.enuos.live.annotations.Cipher;
import com.enuos.live.constants.Constants;
import com.enuos.live.error.ErrorCode;
import com.enuos.live.mapper.CauseMapper;
import com.enuos.live.pojo.Cause;
import com.enuos.live.pojo.TipOffs;
import com.enuos.live.result.Result;
import com.enuos.live.service.TipOffsService;
import com.enuos.live.service.factory.TipOffsServiceFactory;
import com.enuos.live.utils.RedisUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.ObjectUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

/**
 * @ClassName TipOffsController
 * @Description: TODO 举报管理
 * @Author xubin
 * @Date 2020/4/14
 * @Version V1.0
 **/
@Slf4j
@Api("接收举报")
@RestController
@RequestMapping("/tip-offs")
public class TipOffsController {

    @Autowired
    private CauseMapper reportCauseMapper;

    @Autowired
    private RedisUtils redisUtils;

    @ApiOperation("新增举报信息")
    @Cipher
    @PostMapping("/report")
    public Result report(@Valid @RequestBody TipOffs tipOffs, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return Result.error(ErrorCode.EXCEPTION_CODE, bindingResult.getAllErrors().get(0).getDefaultMessage());
        }
        if (StrUtil.isEmpty(tipOffs.getType())) {
            return Result.error(ErrorCode.EXCEPTION_CODE, "举报对象不能为空");
        }
        TipOffsService tipOffsService = TipOffsServiceFactory.getTipOffsService(tipOffs.getType());
        if (ObjectUtil.isEmpty(tipOffsService)){
            return Result.error(ErrorCode.EXCEPTION_CODE, "无效的举报类型");
        }
        return tipOffsService.insert(tipOffs);
    }


    @ApiOperation("获取举报理由字典")
    @Cipher
    @PostMapping("/getCauseList")
    public Result getReportCauseList(@RequestBody Map<String, Integer> params) {
        Integer type = params.get("type");
        List<Cause> reportCauseList;
        reportCauseList= (List<Cause>) redisUtils.get(Constants.KEY_MANAGE_CAUSELIST + type);
        if (ObjectUtil.isEmpty(reportCauseList)){
            log.info("查询数据库->获取举报理由字典");
            reportCauseList = reportCauseMapper.getCauseList(type);
            if (ObjectUtil.isNotEmpty(reportCauseList)) {
                redisUtils.set(Constants.KEY_MANAGE_CAUSELIST + type,reportCauseList,86400);//24小时
            }
        }
        if (ObjectUtils.isEmpty(reportCauseList)) {
            return Result.error(ErrorCode.CONTENT_EMPTY);
        }

        return Result.success(reportCauseList);
    }


}
