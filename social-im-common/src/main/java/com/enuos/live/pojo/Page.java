package com.enuos.live.pojo;

import lombok.Data;

/**
 * @Description 分页信息
 * @Author wangyingjie
 * @Date 2020/5/12
 * @Modified
 */
@Data
public class Page extends Base {

    /** 每页条数 */
    public Integer pageSize;

    /** 页码 */
    public Integer pageNum;

}
