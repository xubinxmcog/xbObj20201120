package com.enuos.live.pojo;

import com.enuos.live.pojo.Base;
import java.io.Serializable;
import lombok.Data;

/**
 * @Description 花名册
 * @Author wangyingjie
 * @Date 13:28 2020/4/3
 * @Modified
 */
@Data
public class FriendPO extends Base implements Serializable {

  private static final long serialVersionUID = 569502764381440678L;

  /**
   * 好友ID
   */
  private Long friendId;

  /**
   * 好友昵称
   */
  private String nickName;

  /**
   * 好友备注
   */
  private String remark;

  /**
   * 用户关系 [1 正常 2 黑名单 ]
   */
  private Integer relation;

  /** 其他参数 */
  /**
   * 用户--好友
   */
  private String abRemark;

  /**
   * 好友--用户
   */
  private String baRemark;

}
