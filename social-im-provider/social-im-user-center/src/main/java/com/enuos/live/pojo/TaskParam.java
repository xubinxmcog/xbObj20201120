package com.enuos.live.pojo;

import java.time.LocalDate;

/**
 * @Description 任务相关参数
 * @Author wangyingjie
 * @Date 2020/11/11
 * @Modified
 */
public class TaskParam {

    private Long userId;

    private String prefix;

    private String taskCode;

    private String templateCode;

    private String[] templateCodes;

    private Integer category;

    private Integer suffix;

    private Integer progress;

    private LocalDate date;

    public TaskParam() {
        super();
    }

    public TaskParam(String taskCode, String templateCode, Integer suffix) {
        this.taskCode = taskCode;
        this.templateCode = templateCode;
        this.suffix = suffix;
    }

    public TaskParam(Long userId, String taskCode, String templateCode) {
        this.userId = userId;
        this.taskCode = taskCode;
        this.templateCode = templateCode;
    }

    public TaskParam(Long userId, String prefix, String taskCode, String templateCode) {
        this.userId = userId;
        this.prefix = prefix;
        this.taskCode = taskCode;
        this.templateCode = templateCode;
    }

    public TaskParam(Long userId, String prefix, String taskCode, String[] templateCodes) {
        this.userId = userId;
        this.prefix = prefix;
        this.taskCode = taskCode;
        this.templateCodes = templateCodes;
    }

    public TaskParam(Long userId, String taskCode, String[] templateCodes) {
        this.userId = userId;
        this.taskCode = taskCode;
        this.templateCodes = templateCodes;
    }

    public TaskParam(Long userId, String taskCode, String templateCode, LocalDate date) {
        this.userId = userId;
        this.taskCode = taskCode;
        this.templateCode = templateCode;
        this.date = date;
    }

    public TaskParam(Long userId, String prefix, String taskCode, String templateCode, Integer category, Integer suffix, LocalDate date) {
        this.userId = userId;
        this.prefix = prefix;
        this.taskCode = taskCode;
        this.templateCode = templateCode;
        this.category = category;
        this.suffix = suffix;
        this.date = date;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public String getTaskCode() {
        return taskCode;
    }

    public void setTaskCode(String taskCode) {
        this.taskCode = taskCode;
    }

    public String getTemplateCode() {
        return templateCode;
    }

    public void setTemplateCode(String templateCode) {
        this.templateCode = templateCode;
    }

    public Integer getCategory() {
        return category;
    }

    public void setCategory(Integer category) {
        this.category = category;
    }

    public Integer getSuffix() {
        return suffix;
    }

    public void setSuffix(Integer suffix) {
        this.suffix = suffix;
    }

    public Integer getProgress() {
        return progress;
    }

    public void setProgress(Integer progress) {
        this.progress = progress;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }
}
