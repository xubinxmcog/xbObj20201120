package com.enuos.live.handle.activity;

/**
 * TODO
 *
 * @author wangcaiwen|1443****11@qq.com
 * @version v1.0.0
 * @since 2020/11/4 13:46
 */
public enum ActivityEnum {

  /** 丹枫迎秋. */
  MidAutumn2020("com.enuos.live.handle.activity.MidAutumn2020");

  private String path;

  ActivityEnum(String path) {
    this.path = path;
  }

  public String getPath() {
    return path;
  }
}
