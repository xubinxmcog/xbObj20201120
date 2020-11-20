package com.enuos.live.handle.game.f30061;

import lombok.Data;

/**
 * TODO 你说我猜词汇.
 *
 * @author wangcaiwen|1443****11@qq.com
 * @version v1.0.0
 * @since 2020/8/4 10:47
 */

@Data
@SuppressWarnings("WeakerAccess")
public class GuessedSaidWord {
  /** 猜测词汇. */
  private String lexicon;
  /** 词汇提示. */
  private String lexiconHint;
  /** 词汇字数. */
  private Integer lexiconWords;
}
