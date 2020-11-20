package com.enuos.live.pojo;

import java.io.Serializable;
import lombok.Data;
import lombok.ToString;

/**
 * @ClassName ReportAttach
 * @Description: TODO 举报附件实体类
 * @Author xubin
 * @Date 2020/4/14
 * @Version V1.0
 **/
@Data
@ToString
public class ReportAttach implements Serializable {
    /**
     * 主键ID
     */
    private Integer id;

    /**
     * 举报ID
     */
    private Integer reportId;

    /**
     * 举报文件
     */
    private String reportFile;

    private static final long serialVersionUID = 1L;
}