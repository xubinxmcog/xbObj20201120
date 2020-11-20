package com.enuos.live.service.impl;

import com.enuos.live.cipher.JWTEncoder;
import com.enuos.live.constant.CodeEnum;
import com.enuos.live.constants.RedisKey;
import com.enuos.live.error.ErrorCode;
import com.enuos.live.feign.ChatFeign;
import com.enuos.live.feign.ManageFeign;
import com.enuos.live.mapper.*;
import com.enuos.live.pojo.Account;
import com.enuos.live.pojo.BindAccount;
import com.enuos.live.pojo.Logout;
import com.enuos.live.pojo.User;
import com.enuos.live.result.Result;
import com.enuos.live.service.*;
import com.enuos.live.utils.*;
import com.enuos.live.utils.sensitive.DFAWordUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * @Description 账户业务
 * @Author wangyingjie
 * @Date 17:25 2020/4/1
 * @Modified
 */
@Slf4j
@Service
public class AccountServiceImpl implements AccountService {

    /** 表名 */
    private static final String[] TABLES = {"tb_user_account_attach", "tb_day_exp", "user_settings"};

    @Value("${user.default.icon_file_id}")
    private Integer dIconFileId;

    @Value("${user.default.icon_url}")
    private String dIconUrl;

    @Value("${user.default.thumb_icon_url}")
    private String dThumbIconUrl;

    @Autowired
    private ChatFeign chatFeign;

    @Autowired
    private ManageFeign manageFeign;

    @Autowired
    private LogService logService;

    @Autowired
    private CommonService commonService;

    @Autowired
    private DeviceService deviceService;

    @Autowired
    private InviteService inviteService;

    @Autowired
    private AccountMapper accountMapper;

    @Autowired
    private AccountAttachMapper accountAttachMapper;

    @Autowired
    private DayExpMapper dayExpMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private LogoutMapper logoutMapper;

    @Autowired
    private UserSettingMapper userSettingMapper;

    @Autowired
    private RedisUtils redisUtils;

    @Autowired
    private DFAWordUtils dfaWordUtils;
    
    /** 
     * @Description: 账号注册 
     * @Param: [account]
     * @Return: com.enuos.live.result.Result 
     * @Author: wangyingjie
     * @Date: 2020/7/6 
     */ 
    @Override
    @Transactional
    public Result regist(Account account) {
        if (Objects.isNull(account)) {
            return Result.empty();
        }

        User user = account.getUser();

        if (Optional.ofNullable(user.getNickName()).orElse("").length() > 10) {
            return Result.error(ErrorCode.EXCEPTION_CODE, "昵称不超过10个字");
        }

        if (Optional.ofNullable(user.getSignature()).orElse("").length() > 100) {
            return Result.error(ErrorCode.EXCEPTION_CODE, "个性签名不超过100个字");
        }

        Integer registType = account.getRegistType();
        String act;
        switch (registType) {
            case 0:
                act = account.getPhone();
                break;
            case 1:
                act = account.getUnionId();
                break;
            case 2:
                act = account.getOpenId();
                break;
            case 3:
                act = account.getAppleUserId();
                break;
            default:
                act = "";
                break;
        }

        account.setAccount(act);

        // 手机注册校验
        if (registType == 0) {
            Result result = validator(account);
            if (!Objects.isNull(result)) {
                return result;
            }
        }

        // 账号唯一性校验
        if (accountMapper.isExistsAccount(act, registType) != null) {
            return Result.error(ErrorCode.ACCOUNT_EXISTS);
        }

        // userId生成并校验
        Long userId = IDUtils.generator();
        while (accountMapper.isExistsUserId(userId) != null) {
            userId = IDUtils.generator();
        }

        // init
        initUser(account, userId);

        // 1.账户信息
        accountMapper.saveAccount(account);

        // 2.用户账号附属信息
        accountAttachMapper.initAccountAttach(userId);

        // 3.用户信息
        userMapper.saveUser(user);

        // 4.保存用户背景
        if (CollectionUtils.isNotEmpty(user.getBackgroundList())) {
            userMapper.batchSaveBackground(user);
        }

        // 5.在线状态及位置
        user.setOnLineStatus(1);
        commonService.refreshOnLineStatus(user);
        commonService.refreshPoint(account);

        // 6.初始化每日经验
        dayExpMapper.initDayExp(userId);

        // 7.初始化默认设置
        userSettingMapper.insert(userId);

        // 8.保存设备号
        deviceService.save(userId, account.getDeviceNumber(), account.getLoginDevice());

        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("userId", userId);

        // 返回token
        String key = String.valueOf(userId);
        String token = JWTEncoder.encode(key);

        redisUtils.set(RedisKey.KEY_TOKEN.concat(key), token, CodeEnum.CODE7.getCode(), TimeUnit.DAYS);

        resultMap.put("token", token);

        // LOG[LOGIN]
        logService.sendLogin(account);

        return Result.success(resultMap);
    }

    /**
     * @Description: 账号绑定列表
     * @Param: [account]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/7/6
     */
    @Override
    public Result bindList(Account account) {
        if (Objects.isNull(account) || Objects.isNull(account.getUserId())) {
            return Result.empty();
        }

        BindAccount bindAccount = accountMapper.getAccountBindInfo(account.getUserId());
        if (bindAccount.getIsBindPhone() == 1) {
            bindAccount.setPhone(com.enuos.live.utils.StringUtils.hideString(bindAccount.getPhone()));
        }

        return Result.success(bindAccount);
    }

    /**
     * @Description: 绑定
     * @Param: [bindAccount]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/7/6
     */
    @Override
    @Transactional
    public Result bind(BindAccount bindAccount) {
        Long userId = bindAccount.getUserId();
        Integer type = bindAccount.getType();

        Account account = accountMapper.getAccountByUserId(userId);

        String newAccount, oldAccount;
        if (type == 0) {
            newAccount = bindAccount.getPhone();
            // 手机验证码
            String redisKey = RedisKey.KEY_SMS_CODE.concat(newAccount);
            if (redisUtils.hasKey(redisKey)) {
                String redisCode = redisUtils.get(redisKey).toString();
                if (!redisCode.equals(bindAccount.getCode())) {
                    return Result.error(ErrorCode.SMS_CODE_DIFFERENCE);
                }
            } else {
                return Result.error(ErrorCode.SMS_CODE_EXPIRED);
            }

            oldAccount = account.getPhone();
        } else if (type == 1) {
            // 微信绑定验证
            newAccount = bindAccount.getUnionId();
            oldAccount = account.getUnionId();
        } else {
            // QQ绑定
            newAccount = bindAccount.getOpenId();
            oldAccount = account.getQOpenId();
        }

        // 账号唯一性
        if (accountMapper.isExistsAccount(newAccount, type) != null) {
            return Result.error(ErrorCode.ACCOUNT_BINDING);
        }

        // 不允许账号覆盖
        if (StringUtils.isNotEmpty(oldAccount)) {
            if (oldAccount.equals(newAccount)) {
                return Result.success();
            } else {
                return Result.error(ErrorCode.ACCOUNT_TO_UNBINDING);
            }
        } else {
            accountMapper.update(bindAccount);
        }

        return Result.success();
    }

    /** 
     * @Description: 注销账户
     * @Param: [logout]
     * @Return: com.enuos.live.result.Result 
     * @Author: wangyingjie
     * @Date: 2020/7/6 
     */ 
    @Override
    @Transactional
    public Result logout(Logout logout) {
        Long userId = logout.getUserId();
        // 变更账户和用户的状态
        int ar = accountMapper.logout(userId, "tb_user_account");
        int ur = accountMapper.logout(userId, "tb_user");
        // 保存注销理由
        if (ar > 0 && ur > 0) {
            logoutMapper.save(logout);
            chatFeign.logoutToDeleteChat(userId);
        }

        accountMapper.logoutToDelete(userId, TABLES);

        // 清除Redis登陆token
        redisUtils.remove(RedisKey.KEY_TOKEN.concat(String.valueOf(userId)));

        return Result.success();
    }

    /** [PRIVATE] */

    /** 
     * @Description: 注册校验
     * @Param: [account]
     * @Return: com.enuos.live.result.Result 
     * @Author: wangyingjie
     * @Date: 2020/6/5 
     */ 
    private Result validator(Account account) {
        Result result = null;
        // 手机注册手机号校验
        if (!StringUtils.isPhone(account.getPhone())) {
            result = Result.error(ErrorCode.ACCOUNT_ILLEGAL);
        }

        if (!Objects.isNull(account.getUser())) {
            String nickName = account.getUser().getNickName();
            String signature = account.getUser().getSignature();

            // 敏感词校验
            if (dfaWordUtils.matchWords(nickName)) {
                result = Result.error(ErrorCode.CONTENT_SENSITIVE);
            }

            // 个性签名校验
            if (dfaWordUtils.matchWords(signature)) {
                result = Result.error(ErrorCode.CONTENT_SENSITIVE);
            }
        }

        return result;
    }

    /** 
     * @Description: 初始化用户信息 
     * @Param: [account, userId]
     * @Return: void 
     * @Author: wangyingjie
     * @Date: 2020/6/5 
     */ 
    private void initUser(Account account, Long userId) {
        if (Objects.isNull(account.getUser())) {
            account.setUser(new User());
        }

        // 默认用户名业务需求支持空格
        if (Objects.isNull(account.getUser().getNickName())) {
            String userIdStr = String.valueOf(userId);
            account.getUser().setNickName("用户" + userIdStr.substring(userIdStr.length() - 4));
        }

        // 默认图片资源
        if (StringUtils.isEmpty(account.getUser().getIconUrl())) {
            account.getUser().setIconFileId(dIconFileId);
            account.getUser().setIconUrl(dIconUrl);
            account.getUser().setThumbIconUrl(dThumbIconUrl);
        } else {
            // 解决qq头像在游戏中跨域不能展示的问题，在此处上传
            if (account.getRegistType() == 2) {
                String iconUrl = account.getUser().getIconUrl();
                Result result = manageFeign.uploadFileUrl(iconUrl, "header/");
                if (result.getCode() == 0) {
                    String url = MapUtils.getString(BeanUtils.toBean(result.getData(), HashMap.class), "picUrl");
                    account.getUser().setIconUrl(url);
                    account.getUser().setThumbIconUrl(url);
                }
            }
        }

        // 默认性別
        if (Objects.isNull(account.getUser().getSex())) {
            account.getUser().setSex(2);
        }

        // 默认生日
        if (Objects.isNull(account.getUser().getBirth())) {
            account.getUser().setBirth(DateUtils.getCurrentDate());
        }

        account.setUserId(userId);
        account.setPlatform(account.getLoginDevice());

        account.getUser().setUserId(userId);
    }
}
