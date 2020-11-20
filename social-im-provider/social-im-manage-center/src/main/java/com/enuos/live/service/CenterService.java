package com.enuos.live.service;

import com.enuos.live.pojo.Feedback;
import com.enuos.live.pojo.Questionnaire;
import com.enuos.live.pojo.UserSettings;
import com.enuos.live.pojo.VersionApp;
import com.enuos.live.result.Result;

public interface CenterService {

    Result versionCheck(String num, String platform);

    Result versionUp(VersionApp versionApp);

    Result addVersionApp(VersionApp versionApp);

    Result feedback(Feedback feedback);

    Result solveAProblem(Integer pageNum, Integer pageSize, Integer keyword);

    Result findAllSolveProblem();

    Result agreement(Integer type);

    Result getUserSettings(Long userId);

    Result upUserSettings(UserSettings record);

    Result saveQuestionnaire(Questionnaire questionnaire);

}
