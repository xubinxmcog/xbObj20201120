package com.enuos.live.constant;

/**
 * @Description 消息枚举
 * @Author wangyingjie
 * @Date 2020/9/3
 * @Modified
 */
public enum MessageEnum {

    LOG_POST_CREATE(1,"发布动态{0}[{1}]"),
    LOG_POST_VIEW(2,"查看动态{0}[{1}]"),
    LOG_POST_PRAISE(3,"点赞{0}[{1}]"),
    LOG_POST_COMMENT(4,"评论{0}[{1}]");

    private Integer id;

    private String template;

    MessageEnum(Integer id, String template) {
        this.id = id;
        this.template = template;
    }

    public String getTemplate() {
        return template;
    }
}
