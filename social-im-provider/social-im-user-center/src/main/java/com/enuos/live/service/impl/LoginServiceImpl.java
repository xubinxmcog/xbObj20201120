package com.enuos.live.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.aliyuncs.CommonRequest;
import com.aliyuncs.CommonResponse;
import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.exceptions.ServerException;
import com.aliyuncs.http.MethodType;
import com.aliyuncs.profile.DefaultProfile;
import com.enuos.live.bean.AliyunBean;
import com.enuos.live.cipher.JWTEncoder;
import com.enuos.live.constant.CodeEnum;
import com.enuos.live.constant.SuccessCode;
import com.enuos.live.constants.RedisKey;
import com.enuos.live.error.ErrorCode;
import com.enuos.live.mapper.AccountMapper;
import com.enuos.live.pojo.Account;
import com.enuos.live.result.Result;
import com.enuos.live.service.*;
import com.enuos.live.utils.RedisUtils;
import com.enuos.live.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * @Description 用户登陆注册相关service
 * @Author wangyingjie
 * @Date 16:09 2020/3/31
 * @Modified
 */
@Slf4j
@Service
public class LoginServiceImpl implements LoginService {

    @Autowired
    private AliyunBean aliyunBean;

    @Autowired
    private RedisUtils redisUtils;

    @Autowired
    private LogService logService;

    @Autowired
    private CommonService commonService;

    @Autowired
    private DeviceService deviceService;

    @Autowired
    private AppleService appleService;

    @Autowired
    private AccountMapper accountMapper;

    /**
     * @Description: 发送短信
     * @Param: [account]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/7/6
     */
    @Override
    public Result sendSMS(Account account) {
        if (Objects.isNull(account)) {
            return Result.empty();
        }

        String phone = account.getPhone();
        if (StringUtils.isEmpty(phone)) {
            return Result.error(ErrorCode.DATA_ERROR);
        }

        // 电话校验
        if (!StringUtils.isPhone(phone)) {
            return Result.error(ErrorCode.ACCOUNT_ILLEGAL);
        }

        String code = "";
        // 白名单不发送短信
        List<String> whiteList = accountMapper.getWhiteList();
        if (CollectionUtils.isEmpty(whiteList) || !whiteList.contains(phone)) {
            // 生成验证码
            code = RandomStringUtils.randomNumeric(4);
            // 发送短信
            boolean result = true;
            // 测试环境不发送短信
            if (aliyunBean.onOff) {
                result = sendSMS(phone, code);
            }

            if (!result) {
                return Result.error(ErrorCode.SMS_SEND_ERROR);
            }

            // 发送成功后将验证码存入redis
            String redisKey = RedisKey.KEY_SMS_CODE.concat(phone);
            redisUtils.set(redisKey, code, CodeEnum.CODE60.getCode());
        }

        if (aliyunBean.onOff) {
            return Result.success();
        } else {
            Map<String, Object> resultMap = new HashMap<>();
            resultMap.put("code", code);
            return Result.success(resultMap);
        }
    }

    /**
     * @Description: 手机短信验证登陆
     * @Param: [account]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/7/6
     */
    @Override
    @Transactional
    public Result loginWithSMS(Account account) {
        if (Objects.isNull(account)) {
            return Result.empty();
        }

        String phone = account.getPhone();
        String code = account.getCode();

        List<String> whiteList = accountMapper.getWhiteList();
        if (CollectionUtils.isEmpty(whiteList) || !whiteList.contains(phone)) {
            Result result = validateCode(phone, code);
            if (result != null) {
                return result;
            }
        }

        // 验证成功，查询是否存在该账户，存在则登陆，不存在则注册
        account.setRegistType(0);
        account.setAccount(phone);
        return login(account);
    }

    /**
     * @Description: 微信登陆
     * @Param: [account]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/7/6
     */
    @Override
    public Result loginWithWeChat(Account account) {
        if (Objects.isNull(account)) {
            return Result.empty();
        }

        account.setRegistType(1);
        account.setAccount(account.getUnionId());
        return login(account);
    }

    /**
     * @Description: QQ登陆
     * @Param: [account]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/7/6
     */
    @Override
    @Transactional
    public Result loginWithQQ(Account account) {
        if (Objects.isNull(account)) {
            return Result.empty();
        }

        account.setRegistType(2);
        account.setAccount(account.getOpenId());
        return login(account);
    }

    /**
     * @Description: apple登陆
     * @Param: [account]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/6/1
     */
    @Override
    @Transactional
    public Result loginWithApple(Account account) {
        if (Objects.isNull(account)) {
            return Result.empty();
        }

        /**
        boolean result = appleService.verify(account.getIdentityToken());
        if (!result) {
            return Result.error(ErrorCode.LOGIN_APPLE_FAIL);
        }
        */

        account.setRegistType(3);
        account.setAccount(account.getAppleUserId());
        return login(account);
    }

    /**
     * @Description: web登陆
     * @Param: [account]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/7/1
     */
    @Override
    public Result loginForWebWithSMS(Account account) {
        if (Objects.isNull(account)) {
            return Result.empty();
        }

        String phone = account.getPhone();
        String code = account.getCode();

        Result result = validateCode(phone, code);
        if (result != null) {
            return result;
        }

        // 验证成功，查询是否存在该账户，存在则返回用户信息，不存在则返回失败。
        Map<String, Object> userMap = accountMapper.getUserBaseForWebByPhone(phone);
        if (MapUtils.isEmpty(userMap)) {
            return Result.error(ErrorCode.ACCOUNT_NOT_EXISTS);
        } else {
            String userId = MapUtils.getString(userMap, "userId");
            String tokenKey = RedisKey.KEY_TOKEN.concat(userId);
            String token;
            if (!redisUtils.hasKey(tokenKey)) {
                token = JWTEncoder.encode(userId);
                redisUtils.set(tokenKey, token);
            } else {
                token = redisUtils.get(tokenKey).toString();
            }

            userMap.put("token", token);
            return Result.success(userMap);
        }
    }

    /**
     * @Description: 登陆 
     * @Param: [account]
     * @Return: com.enuos.live.result.Result 
     * @Author: wangyingjie
     * @Date: 2020/6/1
     * @Modified 2020/7/6 业务修改，背景为多个背景
     */ 
    @Transactional
    Result login(Account account) {
        Account act = accountMapper.getAccount(account);
        if (Objects.isNull(act)) {
            return Result.success(SuccessCode.LOGIN_TO_COMPLETE_USERINFO);
        }

        if (act.getStatus() == 1) {
            return Result.success(SuccessCode.LOGIN_TO_ACCOUNT_SUSPENDED);
        }

        Long userId = act.userId;
        String token = JWTEncoder.encode(String.valueOf(userId));

        redisUtils.set(RedisKey.KEY_TOKEN.concat(String.valueOf(userId)), token, CodeEnum.CODE7.getCode(), TimeUnit.DAYS);

        // 存储定位
        account.setUserId(userId);
        commonService.refreshPoint(account);

        // 保存设备号
        deviceService.save(userId, account.getDeviceNumber(), account.getLoginDevice());

        // LOG[LOGIN]
        logService.sendLogin(account);

        return Result.success(new HashMap<String, Object>() {
            {
                put("userId", userId);
                put("token", token);
            }
        });
    }

    /**
     * @Description: 短信验证码校验
     * @Param: [phone, code]
     * @Return: com.enuos.live.result.Result
     * @Author: wangyingjie
     * @Date: 2020/8/31
     */
    private Result validateCode(String phone, String code) {
        String redisKey = RedisKey.KEY_SMS_CODE.concat(phone);

        if (!redisUtils.hasKey(redisKey)) {
            return Result.error(ErrorCode.SMS_CODE_EXPIRED);
        }

        String redisCode = redisUtils.get(redisKey).toString();

        if (!Objects.equals(code, redisCode)) {
            return Result.error(ErrorCode.SMS_CODE_DIFFERENCE);
        }

        return null;
    }

    /**
     * @Description: 阿里云发送短信业务
     * @Param: [phoneNumbers, code]
     * @Return: boolean
     * @Author: wangyingjie
     * @Date: 2020/7/6
     */
    private boolean sendSMS(String phoneNumbers, String code) {
        DefaultProfile profile = DefaultProfile.getProfile(aliyunBean.regionId, aliyunBean.accessKeyId, aliyunBean.secret);
        IAcsClient client = new DefaultAcsClient(profile);

        CommonRequest request = new CommonRequest();

        request.setMethod(MethodType.POST);
        request.setDomain(aliyunBean.domain);
        request.setVersion(aliyunBean.version);
        request.setAction(aliyunBean.action);
        request.putQueryParameter("RegionId", aliyunBean.regionId);
        request.putQueryParameter("PhoneNumbers", phoneNumbers);
        request.putQueryParameter("SignName", aliyunBean.signName);
        request.putQueryParameter("TemplateCode", aliyunBean.loginTemplateCode);
        request.putQueryParameter("TemplateParam", "{\"code\":\"" + code + "\"}");

        try {
            CommonResponse response = client.getCommonResponse(request);

            log.info("Aliyun.sendSMS [response data: {}]", response.getData());

            if (Objects.equals(JSONObject.parseObject(response.getData()).getString("Message"), "OK")) {
                return true;
            }
        } catch (ServerException e) {
            log.error("Aliyun.sendSMS has ServerException [response data: {}]", e);
        } catch (ClientException e) {
            log.error("Aliyun.sendSMS has ClientException [response data: {}]", e);
        }
        return false;
    }
}
