package com.enuos.live.pojo;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

/**
 * @ClassName VersionApp
 * @Description: TODO 相关问题实体类
 * @Author xubin
 * @Date 2020/5/7
 * @Version V1.0
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SolveProblem implements Serializable {
    private Integer id;

    /**
     * 问题类别
     */
    private String category;

    /**
     * 类别ID
     */
    private Integer categoryId;

    /**
     * 问题内容
     */
    private String content;

    /**
     * 创建时间
     */
//    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
//    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
//    private Date createTime;

    private List<SolveProblem> children;

    private static final long serialVersionUID = 1L;
}