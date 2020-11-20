package com.enuos.live.exception;

import com.alibaba.csp.sentinel.adapter.servlet.callback.UrlBlockHandler;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.slots.block.authority.AuthorityException;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeException;
import com.alibaba.csp.sentinel.slots.block.flow.FlowException;
import com.alibaba.csp.sentinel.slots.block.flow.param.ParamFlowException;
import com.alibaba.csp.sentinel.slots.system.SystemBlockException;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.LinkedHashMap;

/**
 * @ClassName CustomBlockHandler
 * @Description: TODO 自定义Sentinel统一处理
 * @Author xubin
 * @Date 2020/8/21
 * @Version V2.0
 **/
@Slf4j
public class CustomBlockHandler implements UrlBlockHandler {
    @Override
    public void blocked(HttpServletRequest request, HttpServletResponse response, BlockException ex) throws IOException {
        JSONObject result = new JSONObject(new LinkedHashMap());
        if (ex instanceof FlowException) {
            // 服务限流统一返回
            log.warn("sentinel服务限流统一返回,url=[{}]", request.getRequestURL());
            result.put("code", 600);
            result.put("msg", "服务繁忙，请稍后再试(601)");
        } else if (ex instanceof DegradeException) {
            // 服务降级
            log.warn("sentinel服务降级,url=[{}]", request.getRequestURL());
            result.put("code", 600);
            result.put("msg", "服务繁忙，请稍后再试(602)");
        } else if (ex instanceof ParamFlowException) {
            // 热点参数限流
            log.warn("sentinel热点参数限流,url=[{}]", request.getRequestURL());
            result.put("code", 600);
            result.put("msg", "服务繁忙，请稍后再试(603)");
        } else if (ex instanceof SystemBlockException) {
            // 系统规则(负载...)不满足规则
            log.warn("sentinel系统规则(负载...)不满足规则,url=[{}]", request.getRequestURL());
            result.put("code", 600);
            result.put("msg", "服务繁忙，请稍后再试(604)");
        } else if (ex instanceof AuthorityException) {
            // 授权规则不通过
            log.warn("sentinel授权规则不通过,url=[{}]", request.getRequestURL());
            result.put("code", 600);
            result.put("msg", "服务繁忙，请稍后再试(605)");
        }
        response.setStatus(500);
        response.setCharacterEncoding("utf-8");
        response.setHeader("Content-Type", "application/json;charset=utf-8");
        response.setContentType("application/json;charset=utf-8");
        PrintWriter printWriter = response.getWriter();
        printWriter.write(result.toString());
    }
}
