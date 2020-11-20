package com.enuos.live.task;

import javax.validation.constraints.NotNull;

/**
 * @Description 任务类
 * @Author wangyingjie
 * @Date 2020/11/9
 * @Modified
 */
public class Task {
    /** 模板 */
    @NotNull(message = "任务模板不能为空")
    private TemplateEnum template;

    /** 目标 */
    private String target;

    /** 用户ID */
    @NotNull(message = "用户ID不能为空")
    private Long userId;

    /** 值 */
    private Integer value;

    public Task() {
        super();
    }

    public Task(TemplateEnum template, Long userId) {
        this.template = template;
        this.userId = userId;
        this.value = 1;
    }

    public Task(TemplateEnum template, Long userId, Integer value) {
        this.template = template;
        this.userId = userId;
        this.value = value;
    }

    public Task(TemplateEnum template, String target, Long userId, Integer value) {
        this.template = template;
        this.target = target;
        this.userId = userId;
        this.value = value;
    }

    public Task(TemplateEnum template) {
        this.template = template;
    }

    public TemplateEnum getTemplate() {
        return template;
    }

    public void setTemplate(TemplateEnum template) {
        this.template = template;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Integer getValue() {
        return value;
    }

    public void setValue(Integer value) {
        this.value = value;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }
}
