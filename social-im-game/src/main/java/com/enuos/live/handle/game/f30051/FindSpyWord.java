package com.enuos.live.handle.game.f30051;

import lombok.Data;

/**
 * TODO 谁是卧底词汇.
 *
 * @author wangcaiwen|1443****11@qq.com
 * @version v1.0.0
 * @since 2020/7/6 10:19
 */

@Data
@SuppressWarnings("WeakerAccess")
public class FindSpyWord {
  /** 词汇编号. */
  private Integer lexiconNo;
  /** 平民词汇. */
  private String lexiconMass;
  /** 卧底词汇. */
  private String lexiconSpy;
}
