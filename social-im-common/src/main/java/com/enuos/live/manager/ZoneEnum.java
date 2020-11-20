package com.enuos.live.manager;

/**
 * @Description 时区
 * @Author wangyingjie
 * @Date 2020/8/7
 * @Modified
 */
public enum ZoneEnum {

    E8("+8","东8区[北京时间]");

    private String zone;

    private String description;

    ZoneEnum(String zone, String description) {
        this.zone = zone;
        this.description = description;
    }

    public String getZone() {
        return zone;
    }

}
