package com.enuos.live.pojo;

import lombok.Data;

/**
 * @Description 动态业务POJO
 * @Author wangyingjie
 * @Date 10:46 2020/4/26
 * @Modified
 */
@Data
public class PostNum {

    /** 主键 */
    private Integer id;

    /** 点赞数 */
    private Integer praiseNum;

    /** 评论数 */
    private Integer commentNum;

    /** 回复数 */
    private Integer replyNum;

    /**
     * 加
     * @param column
     */
    public void add(String column) {
        switch (column) {
            case "praise_num":
                this.praiseNum++;
                break;
            case "comment_num":
                this.commentNum++;
                break;
            case "reply_num":
                this.replyNum++;
                break;
            default:
                break;
        }
    }

    /**
     * 减
     * @param column
     */
    public void subtract(String column) {
        switch (column) {
            case "praise_num":
                this.praiseNum--;
                break;
            case "comment_num":
                this.commentNum--;
                break;
            case "reply_num":
                this.replyNum--;
                break;
            default:
                break;
        }
    }

}
