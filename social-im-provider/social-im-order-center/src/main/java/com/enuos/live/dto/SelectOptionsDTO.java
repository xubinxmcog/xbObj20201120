package com.enuos.live.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @ClassName SelectOptionsDTO
 * @Description: TODO
 * @Author xubin
 * @Date 2020/4/26
 * @Version V1.0
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties("jz")
public class SelectOptionsDTO {
    private String jz;
    /**
     * 元素名
     */
    private Long id;
    /**
     * 元素值
     */
    private String name;
    /**
     * 子集合
     */
    private List<SelectOptionsDTO> children;

    public SelectOptionsDTO(Long id, String name, List<SelectOptionsDTO> children) {
        this.id = id;
        this.name = name;
        this.children = children;
    }
}
