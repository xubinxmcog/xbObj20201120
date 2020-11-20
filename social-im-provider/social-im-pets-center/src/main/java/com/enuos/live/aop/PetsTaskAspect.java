package com.enuos.live.aop;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.enuos.live.mapper.PetsUserTaskMapper;
import com.enuos.live.annotations.PetsTask;
import com.enuos.live.pojo.PetsUserTask;
import com.enuos.live.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;

/**
 * @ClassName PetsTaskAspect
 * @Description: TODO 任务处理切面
 * @Author xubin
 * @Date 2020/10/22
 * @Version V2.0
 **/
@Slf4j
@Aspect
@Component
public class PetsTaskAspect {

    @Autowired
    private PetsUserTaskMapper petsUserTaskMapper;

    @Resource(name = "eAsyncServiceExecutor")
    private ThreadPoolTaskExecutor executorService;

    /**
     * 织入点
     *
     * @param petsTask
     */
    @Pointcut("@annotation(petsTask)")
    public void pointCut(PetsTask petsTask) {
    }

    /**
     * @MethodName: around
     * @Description: TODO 保存用户任务进度
     * @Param: [point, petsTask, keys]
     * @Return: void
     * @Author: xubin
     * @Date: 15:45 2020/10/22
     **/
    @AfterReturning(value = "pointCut(petsTask)", returning = "keys")
    public void around(JoinPoint point, PetsTask petsTask, Object keys) {
        try {
            Result result = (Result) keys;
            System.out.println(result);
            Integer code = result.getCode();
            if (0 == code) {

                List<Object> args = Arrays.asList(point.getArgs()); // 入参参数
                String paramStr = JSON.toJSONString(args.get(0));
                JSONObject params = JSONObject.parseObject(paramStr);
                Long userId = params.getLong("userId");

                System.out.println(paramStr);

                int task = petsTask.task(); // 任务id
                executorService.submit(() -> {
                    // 任务处理
                    Integer petsTaskType = petsUserTaskMapper.getPetsTaskType(task, userId);
                    if (null != petsTaskType && 0 == petsTaskType) {
                        log.info("宠物任务保存,userId=[{}],task=[{}]", userId, task);
                        PetsUserTask petsUserTask = new PetsUserTask();
                        petsUserTask.setTaskId(task);
                        petsUserTask.setUserId(userId);
                        petsUserTask.setFinishValue(1);

                        // 插入返回结果为0则有数据, 执行更新
                        int i = petsUserTaskMapper.insertNotExists(petsUserTask);
                        if (i == 0) {
                            petsUserTaskMapper.updateFinishValue(petsUserTask);
                        }
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
