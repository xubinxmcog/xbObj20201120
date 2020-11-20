package com.enuos.live.teens.service.impl;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.enuos.live.constants.Constants;
import com.enuos.live.error.ErrorCode;
import com.enuos.live.mapper.TeensModelMapper;
import com.enuos.live.mapper.UserSettingsMapper;
import com.enuos.live.pojo.UserSettings;
import com.enuos.live.result.Result;
import com.enuos.live.teens.pojo.TeensModel;
import com.enuos.live.teens.service.TeensService;
import com.enuos.live.utils.RandomUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.crypto.hash.SimpleHash;
import org.apache.shiro.util.ByteSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * @ClassName TeensServiceImpl
 * @Description: TODO
 * @Author xubin
 * @Date 2020/5/6
 * @Version V1.0
 **/
@Service
@Slf4j
public class TeensServiceImpl implements TeensService {

    @Autowired
    private TeensModelMapper teensModelMapper;

    @Autowired
    private UserSettingsMapper userSettingsMapper;

    /**
     * @MethodName: savePwd
     * @Description: TODO 设置密码 并开启青少年模式
     * @Param: [teensModel]
     * @Return: com.enuos.live.result.Result
     * @Author: xubin
     * @Date: 2020/5/6
     **/
    @Override
    public Result savePwd(TeensModel teensModel) {
//        if (!teensModel.getTeensPwd().equals(teensModel.getPwd())) {
//            log.info("重复密码不一致！");
//            return Result.error(2222, "重复密码不一致！");
//        }
        TeensModel teensM = teensModelMapper.selectByPrimaryKey(teensModel.getUserId());
        if (ObjectUtil.isNotEmpty(teensM)) {
            teensM.setTeensPwd(null);
            teensM.setSalt(null);
            teensM.setPwd(null);
            return Result.success(teensM);
        }
        int insert = teensModelMapper.insert(completePassword(teensModel));
        if (insert > 0) {
            UserSettings userSettings = new UserSettings();
            userSettings.setUserId(teensModel.getUserId());
            userSettings.setTeensModel(1);// 修改设置状态为开启
            userSettingsMapper.updateByUserIdSelective(userSettings);
            teensModel.setTeensPwd(null);
            teensModel.setSalt(null);
            teensModel.setPwd(null);
            teensModel.setStatus(1);
            return Result.success(teensModel);
        } else {
            log.error("开启青少年模式设置密码失败");
            return Result.error(ErrorCode.ERROR_OPERATION);
        }
    }

    /**
     * @MethodName: verification
     * @Description: TODO 验证密码
     * @Param: [userId, oldPwd]
     * @Return: com.enuos.live.result.Result
     * @Author: xubin
     * @Date: 2020/5/28
    **/
    @Override
    public Result verification(Long userId, String oldPwd) {
        Boolean passwordRight = false;
        Map m = completePassword(userId, oldPwd, null);
        passwordRight = teensModelMapper.isPasswordRight(m);
        if (passwordRight) {
            return Result.success();
        } else
            return Result.error(2223, "原密码不正确！");
    }

    /**
     * @MethodName: updatePwd
     * @Description: TODO 修改密码
     * @Param: [userId, oldPwd, newPwd]
     * @Return: com.enuos.live.result.Result
     * @Author: xubin
     * @Date: 2020/5/6
     **/
    @Override
    public Result updatePwd(Long userId, String oldPwd, String newPwd) {
        Boolean passwordRight = true;
        if (StrUtil.isEmpty(oldPwd)) {
            return Result.error(2223, "请输入原密码！");
        }
        if (StrUtil.isEmpty(newPwd)) {
            return Result.error(2224, "请输入新密码！");
        }
        Map m = completePassword(userId, oldPwd, null);
        passwordRight = teensModelMapper.isPasswordRight(m);

        if (passwordRight) {

            TeensModel teensModel = new TeensModel();
            teensModel.setTeensPwd(newPwd);
            teensModel.setUserId(userId);

            int update = teensModelMapper.update(completePassword(teensModel));

            if (update > 0) {
                return Result.success();
            } else {
                return Result.error(ErrorCode.ERROR_OPERATION);
            }

        } else {
            log.info("原密码不正确，未查询到该账号密码！");
            return Result.error(2223, "原密码不正确！");
        }

    }

    /**
     * @MethodName: openORclose
     * @Description: TODO 关闭青少年模式
     * @Param: [teensModel]
     * @Return: com.enuos.live.result.Result
     * @Author: xubin
     * @Date: 2020/5/6
     **/
    @Override
    public Result openORclose(TeensModel teensModel) {
        if (null == teensModel.getUserId() || 0 >= teensModel.getUserId()) {
            log.info("开启或关闭青少年模式userId为空！");
            return Result.error(ErrorCode.DATA_ERROR);
        }
        TeensModel teensModel1 = teensModelMapper.selectByPrimaryKey(teensModel.getUserId());
        if (ObjectUtil.isEmpty(teensModel1)) {
            Result.error(2224, "请先开启青少年模式");
        }
        Boolean passwordRight = true;
        if (StrUtil.isNotBlank(teensModel.getTeensPwd())) {
            Map m = completePassword(teensModel.getUserId(), teensModel.getTeensPwd(), teensModel1.getSalt());
            passwordRight = teensModelMapper.isPasswordRight(m);
        } else {
            return Result.error(2223, "请输入密码！");
        }

        if (passwordRight) {

            teensModel.setTeensPwd(null);
            teensModel.setSalt(null);
//            int update = teensModelMapper.update(teensModel);

            int del = teensModelMapper.delTeen(teensModel.getUserId()); // 删除青少年模式表数据

            UserSettings userSettings = new UserSettings();
            userSettings.setUserId(teensModel.getUserId());
            userSettings.setTeensModel(0);// 修改设置青少年模式状态为关闭
            userSettingsMapper.updateByUserIdSelective(userSettings);

            if (del > 0) {
                teensModel1.setTeensPwd(null);
                teensModel1.setSalt(null);
                return Result.success(teensModel1);
            } else {
                return Result.error(ErrorCode.ERROR_OPERATION);
            }

        } else {
            return Result.error(2223, "密码不正确！");
        }
    }

    /**
     * @MethodName: getTeensModel
     * @Description: TODO 获取青少年模式状态
     * @Param: [teensModel]
     * @Return: com.enuos.live.result.Result
     * @Author: xubin
     * @Date: 2020/5/6
     **/
    @Override
    public Result getTeensModel(TeensModel teensModel) {
        Long userId = teensModel.getUserId();
        if (null == userId || 0 == userId) {
            log.error("获取青少年模式状态userId为空！");
            return Result.error(ErrorCode.DATA_ERROR);
        }
        TeensModel teensModel1 = teensModelMapper.selectByPrimaryKey(userId);
        if (ObjectUtil.isNotEmpty(teensModel1)) {
            teensModel1.setSalt(null);
            teensModel1.setTeensPwd(null);
        }
        return Result.success(teensModel1);
    }

    @Override
    public TeensModel completePassword(TeensModel teensModel) {
        String hashAlgorithmName = Constants.MD5;
        String credentials = teensModel.getTeensPwd();
        int hashIterations = Constants.HASHITERATIONS;
        String randomSixNum = RandomUtil.getRandomSixNum();
        ByteSource credentialsSalt = ByteSource.Util.bytes(randomSixNum);
        Object obj = new SimpleHash(hashAlgorithmName, credentials, credentialsSalt, hashIterations);
        teensModel.setTeensPwd(obj.toString());
        teensModel.setSalt(randomSixNum);
        return teensModel;
    }

    public Map completePassword(Long id, String password, String salt) {
        String hashAlgorithmName = Constants.MD5;
        String credentials = password;
        int hashIterations = Constants.HASHITERATIONS;
        //获取库中原有salt
        if (StrUtil.isEmpty(salt)) {
            salt = teensModelMapper.getSalt(id);
        }
        ByteSource credentialsSalt = ByteSource.Util.bytes(salt);
        Object obj = new SimpleHash(hashAlgorithmName, credentials, credentialsSalt, hashIterations);
        Map map = new HashMap();
        map.put("teensPwd", obj.toString());
        map.put("salt", salt);
        return map;
    }

}
