package com.enuos.live.service.impl;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.enuos.live.error.ErrorCode;
import com.enuos.live.manager.VerifyEnum;
import com.enuos.live.mapper.*;
import com.enuos.live.pojo.*;
import com.enuos.live.result.Result;
import com.enuos.live.service.CenterService;
import com.enuos.live.utils.page.PageInfo;
import com.github.pagehelper.PageHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @ClassName VersionAppServiceImpl
 * @Description: TODO APP版本管理
 * @Author xubin
 * @Date 2020/5/7
 * @Version V1.0
 **/
@Slf4j
@Service
public class CenterServiceImpl implements CenterService {

    @Autowired
    private VersionAppMapper versionAppMapper;

    @Autowired
    private TbFeedbackMapper feedbackMapper;

    @Autowired
    private SolveProblemMapper solveProblemMapper;

    @Autowired
    private AgreementMapper agreementMapper;

    @Autowired
    private UserSettingsMapper userSettingsMapper;

    @Autowired
    private QuestionnaireMapper questionnaireMapper;

    /**
     * @MethodName: versionCheck
     * @Description: TODO 版本检查更新
     * @Param: [num, platform]
     * @Return: com.enuos.live.result.Result
     * @Author: xubin
     * @Date: 2020/5/7
     **/
    @Override
    public Result versionCheck(String version, String platform) {
        log.info("版本检查更新->参数:version={},platform={}", version, platform);
        if (StrUtil.isEmpty(version) || StrUtil.isEmpty(platform)) {
            return Result.error(ErrorCode.DATA_ERROR);
        }
        VersionApp versionApp = versionAppMapper.selectByPlatform(platform);

        if (ObjectUtil.isEmpty(versionApp)) {
            return Result.success(0, "暂无更新");
        }

        if (version.equals(versionApp.getVersion())) {
            return Result.success(0, "当前最新版本");
        }

        return Result.success(versionApp);
    }

    /**
     * @MethodName: versionInsert
     * @Description: TODO 更新版本信息
     * @Param: [versionApp]
     * @Return: com.enuos.live.result.Result
     * @Author: xubin
     * @Date: 11:02 2020/8/18
    **/
    @Override
    public Result versionUp(VersionApp versionApp) {
        return Result.success(versionAppMapper.updateByPrimaryKeySelective(versionApp));
    }

    /**
     * @MethodName: versionInsert
     * @Description: TODO 新增版本
     * @Param: [versionApp]
     * @Return: com.enuos.live.result.Result
     * @Author: xubin
     * @Date: 11:02 2020/8/18
     **/
    @Override
    public Result addVersionApp(VersionApp versionApp) {
        return Result.success(versionAppMapper.insert(versionApp));
    }

    /**
     * @MethodName: feedback
     * @Description: TODO 意见反馈
     * @Param: [feedback]
     * @Return: com.enuos.live.result.Result
     * @Author: xubin
     * @Date: 2020/5/7
     **/
    @Override
    public Result feedback(Feedback feedback) {
        log.info("意见反馈入参=[{}]", feedback);
        feedbackMapper.insert(feedback);
        return Result.success(feedback.getId());
    }

    /**
     * @MethodName: solveAProblem
     * @Description: TODO 相关问题
     * @Param: [pageNum, pageSize, keyword]
     * @Return: com.enuos.live.result.Result
     * @Author: xubin
     * @Date: 2020/5/7
     **/
    @Override
    public Result solveAProblem(Integer pageNum, Integer pageSize, Integer keyword) {
        log.info("相关问题入参：【{},{},{}】", pageNum, pageSize, keyword);
        PageHelper.startPage(pageNum, pageSize);

        return Result.success(new PageInfo<>(solveProblemMapper.selectByKeyword(keyword)));
    }

    @Override
    public Result findAllSolveProblem() {

        List<SolveProblem> solveProblemList = solveProblemMapper.findAllSolveProblem();
        return Result.success(solveProblemList.stream()
                .filter(solveProblem -> solveProblem.getCategoryId() == 0)
                .map(solveProblem -> SolveProblem.builder()
                        .id(solveProblem.getId())
                        .categoryId(solveProblem.getCategoryId())
                        .category(solveProblem.getCategory())
                        .content(solveProblem.getContent())
                        .children(
                                solveProblemList.stream()
                                        .filter(childchild -> childchild.getCategoryId() == solveProblem.getId())
                                        .map(childchild -> SolveProblem.builder()
                                                .id(childchild.getId())
                                                .categoryId(childchild.getCategoryId())
                                                .category(childchild.getCategory())
                                                .content(childchild.getContent())
                                                .children(
                                                        solveProblemList.stream()
                                                                .filter(child -> child.getCategoryId() == childchild.getId())
                                                                .map(child -> SolveProblem.builder()
                                                                        .id(child.getId())
                                                                        .categoryId(child.getCategoryId())
                                                                        .category(child.getCategory())
                                                                        .content(child.getContent()).build()).collect(Collectors.toList())
                                                ).build()).collect(Collectors.toList())
                        ).build()).collect(Collectors.toList())

        );
    }

    /**
     * @MethodName: agreement
     * @Description: TODO 用户、隐私 协议
     * @Param: [type：协议类型]
     * @Return: com.enuos.live.result.Result
     * @Author: xubin
     * @Date: 2020/5/7
     **/
    @Override
    public Result agreement(Integer type) {
        log.info("用户、隐私 协议入参：【{}】", type);
        if (null == type || 0 == type) {
            return Result.error(ErrorCode.DATA_ERROR);
        }
        return Result.success(agreementMapper.selectByType(type));
    }

    /**
     * @MethodName: getUserSettings
     * @Description: TODO 获取用户设置
     * @Param: [userId]
     * @Return: com.enuos.live.result.Result
     * @Author: xubin
     * @Date: 2020/5/12
     **/
    @Override
    public Result getUserSettings(Long userId) {
        log.info("获取用户设置入参：【{}】", userId);
        if (userId == null || userId == 0) {
            log.error("getUserSettings用户ID不能为空");
            return Result.error(ErrorCode.DATA_ERROR);
        }
        UserSettings userSettings = userSettingsMapper.selectByUserId(userId);
        if (ObjectUtil.isEmpty(userSettings)) {
            userSettings = new UserSettings(0L, userId,
                    0, 1, 1, 1, 1, 1, 1,
                    1, 0, new Date());
        }
        return Result.success(userSettings);
    }

    /**
     * @MethodName: upUserSettings
     * @Description: TODO 修改用户设置
     * @Param: [userSettings]
     * @Return: com.enuos.live.result.Result
     * @Author: xubin
     * @Date: 2020/5/12
     **/
    @Override
    public Result upUserSettings(UserSettings record) {
        log.info("修改用户设置入参：【{}】", record.toString());
        if (ObjectUtil.isEmpty(record) || null == record.getUserId()) {
            log.error("修改用户设置入参为空");
            return Result.error(ErrorCode.DATA_ERROR);
        }
        userSettingsMapper.updateByUserIdSelective(record);
        return Result.success(record);
    }

    /**
     * @MethodName: saveQuestionnaire
     * @Description: TODO 保存调查问卷
     * @Param: [questionnaire]
     * @Return: com.enuos.live.result.Result
     * @Author: xubin
     * @Date: 2020/5/19
     **/
    @Override
    public Result saveQuestionnaire(Questionnaire questionnaire) {
        // 很傻逼的写法
        if (null == questionnaire.getUserId()) {
            return Result.error(VerifyEnum.ERROR_CODE_201.getCode(), "userId不能为空");
        } else if (null == questionnaire.getSex()) {
            return Result.error(VerifyEnum.ERROR_CODE_201.getCode(), "性别不能为空");
        } else if (null == questionnaire.getAge()) {
            return Result.error(VerifyEnum.ERROR_CODE_201.getCode(), "年龄不能为空");
        } else if (StrUtil.isEmpty(questionnaire.getProfession())) {
            return Result.error(VerifyEnum.ERROR_CODE_201.getCode(), "职业不能为空");
        } else if (StrUtil.isEmpty(questionnaire.getSource())) {
            return Result.error(VerifyEnum.ERROR_CODE_201.getCode(), "安装途径不能为空");
        } else if (null == questionnaire.getSatisfaction()) {
            return Result.error(VerifyEnum.ERROR_CODE_201.getCode(), "整体满意度不能为空");
        } else if (null == questionnaire.getPerformance()) {
            return Result.error(VerifyEnum.ERROR_CODE_201.getCode(), "性能满意度不能为空");
        } else if (null == questionnaire.getPowerConsumption()) {
            return Result.error(VerifyEnum.ERROR_CODE_201.getCode(), "耗电量不能为空");
        } else if (null == questionnaire.getFlowConsumption()) {
            return Result.error(VerifyEnum.ERROR_CODE_201.getCode(), "耗流量不能为空");
        } else if (null == questionnaire.getLoadingSpeed()) {
            return Result.error(VerifyEnum.ERROR_CODE_201.getCode(), "加载速度不能为空");
        } else if (null == questionnaire.getNetworkDelay()) {
            return Result.error(VerifyEnum.ERROR_CODE_201.getCode(), "网络顺畅不能为空");
        } else if (null == questionnaire.getBug()) {
            return Result.error(VerifyEnum.ERROR_CODE_201.getCode(), "黑屏、卡顿或卡死的情况不能为空");
        } else if (null == questionnaire.getBeautiful()) {
            return Result.error(VerifyEnum.ERROR_CODE_201.getCode(), "7乐产品美观度不能为空");
        } else if (null == questionnaire.getFluency()) {
            return Result.error(VerifyEnum.ERROR_CODE_201.getCode(), "7乐产品的交互功能和使用流畅满意度不能为空");
        } else if (null == questionnaire.getSatisfy()) {
            return Result.error(VerifyEnum.ERROR_CODE_201.getCode(), "功能完备度不能为空");
        } else if (null == questionnaire.getRich()) {
            return Result.error(VerifyEnum.ERROR_CODE_201.getCode(), "功能丰富度不能为空");
        } else if (null == questionnaire.getFindFriends()) {
            return Result.error(VerifyEnum.ERROR_CODE_201.getCode(), "能够帮助你找到玩得来或聊得来的朋友不能为空");
        } else if (null == questionnaire.getAtmosphere()) {
            return Result.error(VerifyEnum.ERROR_CODE_201.getCode(), "7乐产品的氛围 不能为空");
        } else if (null == questionnaire.getActivity()) {
            return Result.error(VerifyEnum.ERROR_CODE_201.getCode(), "产品的活动是否满意 不能为空");
        } else if (null == questionnaire.getActivityNotice()) {
            return Result.error(VerifyEnum.ERROR_CODE_201.getCode(), "通知及时 不能为空");
        } else if (null == questionnaire.getActivityRich()) {
            return Result.error(VerifyEnum.ERROR_CODE_201.getCode(), "活动丰富 不能为空");
        } else if (null == questionnaire.getActivityInteresting()) {
            return Result.error(VerifyEnum.ERROR_CODE_201.getCode(), "内容有趣 不能为空");
        } else if (null == questionnaire.getActivityParticipate()) {
            return Result.error(VerifyEnum.ERROR_CODE_201.getCode(), "有参与感 不能为空");
        } else if (null == questionnaire.getActivityAttractive()) {
            return Result.error(VerifyEnum.ERROR_CODE_201.getCode(), "有吸引力 不能为空");
        } else if (null == questionnaire.getLikeFeatures()) {
            return Result.error(VerifyEnum.ERROR_CODE_201.getCode(), "您最喜欢的功能 不能为空");
        } else if (null == questionnaire.getRecommend()) {
            return Result.error(VerifyEnum.ERROR_CODE_201.getCode(), "是否愿意将7乐产品推荐给您的朋友 不能为空");
        } else if (null == questionnaire.getUseTime()) {
            return Result.error(VerifyEnum.ERROR_CODE_201.getCode(), "每天使用7乐产品的时间大约 不能为空");
        }
        questionnaireMapper.insert(questionnaire);
        return Result.success();
    }
}
