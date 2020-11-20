package com.enuos.live.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.NotNull;
import java.util.Date;

/**
 * @ClassName TipOffsDTO
 * @Description: TODO 举报管理查询条件
 * @Author xubin
 * @Date 2020/4/15
 * @Version V1.0
 **/
@Data
public class TipOffsDTO {

    /**
     * 起始页
     */
    private Integer pageNum = 1;

    /**
     * 当前页条数
     */
    private Integer pageSize = 10;

    /**
     * id
     */
    private Integer id;

    /**
     * 举报类别 1：用户，2：房间，3：群组
     */
    @NotNull(message = "举报类别不能为空，1：用户，2：房间，3：群组")
    private Integer tipOffType;

    /**
     * 处理状态 [0 未处理 1 已处理 2 不予处理 3置后]
     */
    private Integer handleStatus;

    /**
     * 处理操作 [1警告 2定时停封  3永久停封]
     */
    private Integer handleAction;

    /**
     * 时间
     */
    private String beginTime;
    private String endTime;
}
