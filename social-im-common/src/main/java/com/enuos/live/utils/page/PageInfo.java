package com.enuos.live.utils.page;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageSerializable;
import lombok.Data;

import java.util.Collection;
import java.util.List;

/**
 * @Description 分页
 * @Author wangyingjie
 * @Date 10:50 2020/4/29
 * @Modified
 */
@Data
public class PageInfo<T> extends PageSerializable<T> {

    /** 页码 */
    private Integer pageNum;

    /** 每页条数 */
    private Integer pageSize;

    /** 总页数 */
    private Integer pages;

    public PageInfo(List<T> list) {
        super(list);
        if (list instanceof Page) {
            Page page = (Page) list;
            this.pageNum = page.getPageNum();
            this.pageSize = page.getPageSize();
            this.pages = page.getPages();
        } else if (list instanceof Collection) {
            this.pageNum = 1;
            this.pageSize = list.size();
            this.pages = this.pageSize > 0 ? 1 : 0;
        }
    }

}
