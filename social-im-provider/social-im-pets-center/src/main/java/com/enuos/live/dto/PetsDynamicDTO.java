package com.enuos.live.dto;

import lombok.Data;
import lombok.ToString;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @ClassName PetsDynamicDTO
 * @Description: TODO
 * @Author xubin
 * @Date 2020/10/20
 * @Version V2.0
 **/
@Data
@ToString
public class PetsDynamicDTO {

    @NotNull(message = "用户ID不能为空")
    private Long userId;

    @NotNull(message = "消息状态不能为空")
    private Integer isRead;

    private Integer pageNum = 1;

    private Integer pageSize = 100;

    private List<Long> ids;


}
