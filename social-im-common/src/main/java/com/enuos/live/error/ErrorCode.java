package com.enuos.live.error;

import com.enuos.live.result.BaseCodeMsg;

/**
 * @author WangCaiWen
 * Created on 2020/3/19 9:27
 */
public class ErrorCode {

    /** common */
    public static final int EXCEPTION_CODE = 201;

    public static final int ERROR_CODE = 201;

    public static final BaseCodeMsg DATA_ERROR = BaseCodeMsg.app(100, "数据产生错误，请前往官方网站联系客服");

    public static final BaseCodeMsg NO_PERMISSION = BaseCodeMsg.app(101, "没有权限");
    public static final BaseCodeMsg NO_DATA = BaseCodeMsg.app(102, "查无数据");
    public static final BaseCodeMsg ERROR_OPERATION = BaseCodeMsg.app(103, "操作异常");

    public static final BaseCodeMsg NETWORK_ERROR = BaseCodeMsg.app(-1, "请求失败，请稍后尝试或检查网络");

    public static final BaseCodeMsg NOT_ENOUGH_DIAMOND = BaseCodeMsg.app(10000, "钻石不足");
    public static final BaseCodeMsg NOT_ENOUGH_GOLD = BaseCodeMsg.app(10001, "金币不足");
    public static final BaseCodeMsg NOT_ENOUGH_GASHAPON = BaseCodeMsg.app(10002, "扭蛋券不足");
    public static final BaseCodeMsg NOT_ENOUGH_CURRENCY_ACT0001 = BaseCodeMsg.app(10003, "枫叶奖章数量不足");
    public static final BaseCodeMsg NOT_ENOUGH_TICKET = BaseCodeMsg.app(10004, "券不足");


    /** user */
    public static final BaseCodeMsg SMS_CODE_EXPIRED = BaseCodeMsg.app(1000, "验证码已过期，请重新获取");
    public static final BaseCodeMsg SMS_CODE_DIFFERENCE = BaseCodeMsg.app(1001, "验证码错误，请重新获取");
    public static final BaseCodeMsg SMS_SEND_ERROR = BaseCodeMsg.app(1002, "请求频繁稍后尝试");

    public static final BaseCodeMsg LOGIN_EXPIRED = BaseCodeMsg.app(1010, "登录过期");
    public static final BaseCodeMsg LOGIN_APPLE_FAIL = BaseCodeMsg.app(1011, "Apple授权校验失败");

    public static final BaseCodeMsg ACCOUNT_EXISTS = BaseCodeMsg.app(1020, "该账号已存在");
    public static final BaseCodeMsg ACCOUNT_NOT_EXISTS = BaseCodeMsg.app(1021, "该账号不存在");
    public static final BaseCodeMsg ACCOUNT_BINDING = BaseCodeMsg.app(1022, "该账号已绑定");
    public static final BaseCodeMsg ACCOUNT_TO_UNBINDING = BaseCodeMsg.app(1023, "已存在绑定账号，请解绑原账号");
    public static final BaseCodeMsg ACCOUNT_LOGINOUT = BaseCodeMsg.app(1024, "该账号已注销");
    public static final BaseCodeMsg ACCOUNT_ILLEGAL = BaseCodeMsg.app(1025, "手机号不合法");
    public static final BaseCodeMsg ACCOUNT_EXISTS_INVITE = BaseCodeMsg.app(1026, "该用户已被他人邀请");
    public static final BaseCodeMsg ACCOUNT_UNBINDING_WECHAT = BaseCodeMsg.app(1027, "该账号未绑定微信");

    public static final BaseCodeMsg IDCARD_ILLEGAL = BaseCodeMsg.app(1030, "证件号码不合法");
    public static final BaseCodeMsg IDCARD_AUTHENTICATION = BaseCodeMsg.app(1031, "该证件号已认证");
    public static final BaseCodeMsg USER_NO_AUTHENTICATION = BaseCodeMsg.app(1032, "该用户没有实名认证");
    public static final BaseCodeMsg IDCARD_NAME_ILLEGAL = BaseCodeMsg.app(1033, "姓名与证件不符");

    public static final BaseCodeMsg CONTENT_EMPTY = BaseCodeMsg.app(1040, "内容为空");
    public static final BaseCodeMsg CONTENT_SENSITIVE = BaseCodeMsg.app(1041, "内容不合法，存在非法字符或敏感词汇");

    public static final BaseCodeMsg SIGN_YET = BaseCodeMsg.app(1050, "签到异常，已签到");
    public static final BaseCodeMsg SIGN_BACK_ERROR = BaseCodeMsg.app(1051, "补签时间异常");
    public static final BaseCodeMsg SIGN_RECORD_EXISTS = BaseCodeMsg.app(1052, "存在补签记录");

    public static final BaseCodeMsg FRIEND_IS_EXISTS = BaseCodeMsg.app(1060, "好友已存在");
    public static final BaseCodeMsg BLACKLIST_IS_EXISTS = BaseCodeMsg.app(1061, "该用户已被拉黑");
    public static final BaseCodeMsg FRIEND_LIMIT = BaseCodeMsg.app(1062, "已达好友上限，提升会员等级解锁更多好友位置");

    public static final BaseCodeMsg REWARD_IS_GOT = BaseCodeMsg.app(1070, "奖励已领取");
    public static final BaseCodeMsg REWARD_NOT_EXISTS = BaseCodeMsg.app(1071, "奖励不存在");
    public static final BaseCodeMsg REWARD_FAIL_GOT = BaseCodeMsg.app(1072, "领奖失败");
    public static final BaseCodeMsg REWARD_NO_PERMISSION = BaseCodeMsg.app(1073, "该奖励无权领取");

    public static final BaseCodeMsg EXP_GAME_TODAY_MAX = BaseCodeMsg.app(1074, "今日可获取的经验值已达上限");

    public static final BaseCodeMsg BADGE_WEAR_MAX = BaseCodeMsg.app(1080, "徽章佩戴已满");

    public static final BaseCodeMsg MEMBER_EXPIRED = BaseCodeMsg.app(1090, "会员已过期");

    public static final BaseCodeMsg MONEY_ILLEGAL = BaseCodeMsg.app(1110, "金额小于最低限额");
    public static final BaseCodeMsg MONEY_NOT_ENOUGT = BaseCodeMsg.app(1111, "当前可提现金额不足！");
    public static final BaseCodeMsg MONEY_TODAY_NOT_ENOUGT = BaseCodeMsg.app(1112, "当日可提现金额不足！");

    public static final BaseCodeMsg ACTIVITY_NOT_START = BaseCodeMsg.app(1113, "活动未开始，敬请期待");
    public static final BaseCodeMsg ACTIVITY_YET_END = BaseCodeMsg.app(1114, "活动已结束");
    public static final BaseCodeMsg ACTIVITY_NOT_EXISTS = BaseCodeMsg.app(1115, "活动不存在");
    public static final BaseCodeMsg IS_UNLOCK = BaseCodeMsg.app(1116, "已解锁");


    /** voice */
    public static final BaseCodeMsg PASSWORD_DIFFERENCE = BaseCodeMsg.app(2000, "密码有误");

    public static final BaseCodeMsg ROOM_BANNED = BaseCodeMsg.app(2010, "该房间已被查封，请联系客服");
    public static final BaseCodeMsg ROOM_END = BaseCodeMsg.app(2011, "语音房已结束");


    /** post */
    public static final BaseCodeMsg PRAISE_YET = BaseCodeMsg.app(3000, "已赞");
    public static final BaseCodeMsg POST_NOT_EXISTS = BaseCodeMsg.app(3001, "该动态已被删除");


    /** home */
    public static final BaseCodeMsg HOME_PARAM_NULL = BaseCodeMsg.app(9000, "请检查数据完整性");

    /** chat */
    public static final BaseCodeMsg CHAT_PARAM_NULL = BaseCodeMsg.app(9001, "请检查数据完整性！");
    public static final BaseCodeMsg CHAT_PARAM_ERROR = BaseCodeMsg.app(9002, "请检查请求参数是否正确！");
    public static final BaseCodeMsg CHAT_PARAM_EXIST = BaseCodeMsg.app(9003, "请求数据不存在！");
    public static final BaseCodeMsg GROUP_CHAT_INVITE_NUM_NULL = BaseCodeMsg.app(9004, "邀请失败！请选择邀请用户！");
    public static final BaseCodeMsg GROUP_CHAT_INVITE_NUM_MAX = BaseCodeMsg.app(9005, "邀请失败！当前邀请人数不可超过49人！");
    public static final BaseCodeMsg GROUP_CHAT_NUM_MAX_1 = BaseCodeMsg.app(9006, "邀请失败！当前群人数已满.可酌情清理群人员！");
    public static final BaseCodeMsg GROUP_CHAT_NUM_MAX_2 = BaseCodeMsg.app(9007, "邀请失败！当前群人数已满.可前往群管理升级群人数！");
    public static final BaseCodeMsg GROUP_CHAT_AUTHORITY = BaseCodeMsg.app(9008, "无权限修改群信息！");
    public static final BaseCodeMsg GROUP_INFO_NAME = BaseCodeMsg.app(9009, "请输入群名称！");
    public static final BaseCodeMsg GROUP_CHAT_AUTHORITY_SET = BaseCodeMsg.app(9010, "无权限设置管理员！");
    public static final BaseCodeMsg GROUP_CHAT_AUTHORITY_NUM_1 = BaseCodeMsg.app(9011, "设置失败！当前管理员人数已满.可前往群管理申请升级群人数.最高支持6名管理员！");
    public static final BaseCodeMsg GROUP_CHAT_AUTHORITY_NUM_2 = BaseCodeMsg.app(9012, "设置失败！当前管理员人数已满.可酌情撤消/更换现有管理人员！");
    public static final BaseCodeMsg GROUP_CHAT_AUTHORITY_NULL = BaseCodeMsg.app(9013, "无权限进行此操作！");
    public static final BaseCodeMsg GROUP_CHAT_AUTHORITY_DELETE = BaseCodeMsg.app(9014, "无权限删除成员！");
    public static final BaseCodeMsg GROUP_CHAT_DELETE_ERROR = BaseCodeMsg.app(9014, "错误操作！");
    public static final BaseCodeMsg GROUP_CHAT_DELETE_USER = BaseCodeMsg.app(9015, "请选择删除用户！");
    public static final BaseCodeMsg GROUP_INFO_NOTICE = BaseCodeMsg.app(9016, "请输入公告信息！");
    public static final BaseCodeMsg GROUP_ADMIN_LIMIT = BaseCodeMsg.app(9017, "转移失败！目标用户身份与群等级不符合！");
    public static final BaseCodeMsg GROUP_UPGRADE_FAILED = BaseCodeMsg.app(9018, "升级失败！需要开通会员身份.获得升级特权！");
    public static final BaseCodeMsg GROUP_INFO_NAME_LENGTH = BaseCodeMsg.app(9019, "群名称不可超过10个字！");
    public static final BaseCodeMsg GROUP_INFO_NOTICE_LENGTH = BaseCodeMsg.app(9020, "群公告不可超过100个字！");
    public static final BaseCodeMsg GROUP_INFO_INTRO_LENGTH = BaseCodeMsg.app(9020, "群简介不可超过100个字！");

    /** Questions */
    public static final BaseCodeMsg QUESTIONS_CODE = BaseCodeMsg.app(9110,"未知编码，请检查数据！");
    public static final BaseCodeMsg QUESTIONS_PARAM = BaseCodeMsg.app(9111,"请检查数据完整性！");
    public static final BaseCodeMsg QUESTIONS_REPEAT = BaseCodeMsg.app(9112,"词汇已被提交过，换一个试试！");
    public static final BaseCodeMsg QUESTIONS_SUBMIT = BaseCodeMsg.app(9113,"每日可提交10个题目，今日已达上限！");
    public static final BaseCodeMsg QUESTIONS_TYPE = BaseCodeMsg.app(9114,"请选择相关分类！");
    public static final BaseCodeMsg QUESTIONS_LEGALITY = BaseCodeMsg.app(9115,"词汇内容存在敏感内容，请重新输入！");
    public static final BaseCodeMsg QUESTIONS_GUESS_WORD = BaseCodeMsg.app(9116,"请输入词汇！1-10个字！");
    public static final BaseCodeMsg QUESTIONS_GUESS_HINT = BaseCodeMsg.app(9117,"请输入提示内容！1-10个字！");
    public static final BaseCodeMsg QUESTIONS_GUESS_WORD_COUNT = BaseCodeMsg.app(9118,"词汇内容超出限制，请输入1-10个字！");
    public static final BaseCodeMsg QUESTIONS_SPY_WORD = BaseCodeMsg.app(9119,"请输入词汇！1-5个字！");
    public static final BaseCodeMsg QUESTIONS_SPY_WORD_COUNT = BaseCodeMsg.app(9120,"词汇内容超出限制，请输入1-5个字！");
    public static final BaseCodeMsg QUESTIONS_STAND_WORD = BaseCodeMsg.app(9121,"请输入题目内容！1-80个字！");
    public static final BaseCodeMsg QUESTIONS_STAND_COUNT = BaseCodeMsg.app(9122,"题目内容超出限制，请输入80个字！");
    public static final BaseCodeMsg QUESTIONS_STAND_WORD_COUNT = BaseCodeMsg.app(9123,"答案内容超出限制，请输入15个字！");
    public static final BaseCodeMsg QUESTIONS_STAND_ANSWER = BaseCodeMsg.app(9124,"请输入正确答案！");
    public static final BaseCodeMsg QUESTIONS_STAND_ERROR = BaseCodeMsg.app(9125,"请输入错误答案1！");
    public static final BaseCodeMsg QUESTIONS_STAND_PROBLEM = BaseCodeMsg.app(9126,"题目已被提交过，换一个试试！");

    /** pets */
    public static final BaseCodeMsg NUM_NOT_ENOUGH = BaseCodeMsg.app(6001,"物品数量不足");
}
