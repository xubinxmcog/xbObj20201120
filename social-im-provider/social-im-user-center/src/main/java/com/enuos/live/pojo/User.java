package com.enuos.live.pojo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.Size;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

/**
 * @Description 用户
 * @Author wangyingjie
 * @Date 2020/9/11
 * @Modified
 */
@Data
public class User extends Base implements Serializable {

    private static final long serialVersionUID = 2070861511390728534L;

    /** 用户ID */
    private Long toUserId;

    /** 用户性别 [1男 2女] */
    private Integer sex;

    /** 用户年龄 */
    private Integer age;

    /** 用户生日 */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate birth;

    /** 用户地区 [第三方平台授权地区] */
    private String area;

    /** 用户图标 [第三方平台授权头像]ID */
    private Integer iconFileId;

    /** 用户图标 [第三方平台授权头像]URL */
    private String iconUrl;

    /** 用户图标缩略图 [第三方平台授权头像]URL */
    private String thumbIconUrl;

    /** 用户昵称 [第三方平台授权昵称] */
    @Size(max = 10, message = "昵称长度过长")
    private String nickName;

    /** 个性签名 */
    private String signature;

    /** 后台创建人员工号 */
    private Integer createId;

    /** 后台创建人员姓名 */
    private String createName;

    /** 创建时间 */
    private Long createTime;

    /** 更新时间 */
    private Long updateTime;

    /** 在线状态[0：离线；1：在线；2：游戏中；3：直播中；4：免打扰] */
    private Integer onLineStatus;

    /** 登陆设备[1：IOS；2：Android；3：平台] */
    private Integer loginDevice;

    /** 身价 */
    private Long worth;

    /** 背景 */
    private List<Background> backgroundList;

}
