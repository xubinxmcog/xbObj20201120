package com.enuos.live.pojo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Date;

/**
 * @ClassName TipOffs
 * @Description: TODO 举报实体类
 * @Author xubin
 * @Date 2020/4/14
 * @Version V1.0
 **/
@Data
@ToString
public class TipOffs implements Serializable {

    /**
     * id
     */
    private Integer id;

    /**
     * 用户ID
     */
    @NotNull(message = "用户ID is null")
    private Long userId;

    /**
     * 用户名称
     */
    private String userName;

    /**
     * 举报理由
     */
    @NotNull(message = "举报理由 is null")
    private String reportCause;

    /**
     * 举报内容
     */
    private String content;

    /**
     * 处理人员ID  新增默认为0
     */
    private Long handleId = 0L;

    /**
     * 处理人员姓名 新增默认为“”
     */
    private String handleName = "";

    /**
     * ;// 处理操作 [1警告 2定时停封  3永久停封] 新增默认为0
     */
    private Integer handleAction = 0;

    /**
     * 处理状态 [0 未处理 1 已处理 2 不予处理 3置后] 新增默认为0
     */
    private Integer handleStatus = 0;

    /**
     * 举报时间
     */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    /**
     * 处理时间
     */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;

    /**
     * 举报文件
     */
    private String reportFile;




// 用户举报
    /**
     * 目标ID
     */
    private Long targetId;

    /**
     * 目标名称
     */
    private String targetName;

// 群举报
    /**
     * 群组ID
     */
    private Long groupId;

    /**
     * 群主ID
     */
    private Long groupAdminId;

    /**
     * 群主名称
     */
    private String groupAdminName;

// 房间举报
    /**
     * 房间ID
     */
    private Integer roomId;

    /**
     * 房主ID
     */
    private Long roomAdminId;

    /**
     * 房主名称
     */
    private String roomAdminName;


    /**
     * 举报对象
     */
    private String type;
}
