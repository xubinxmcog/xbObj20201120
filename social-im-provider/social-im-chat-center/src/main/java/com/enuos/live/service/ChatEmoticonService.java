package com.enuos.live.service;

import com.enuos.live.result.Result;

import java.util.Map;

public interface ChatEmoticonService {

    Result getEmoticon(Map<String, Object> params);
}
