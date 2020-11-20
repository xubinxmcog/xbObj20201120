package com.enuos.live.schedule;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.enuos.live.containers.ServiceContainer;
import com.enuos.live.mapper.ExceptionInfoMapper;
import com.enuos.live.mapper.ExceptionSendMsgConfigMapper;
import com.enuos.live.pojo.ExceptionServiceInfo;
import com.enuos.live.service.MailService;
import com.enuos.live.utils.TimeDateUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @ClassName MonitorJob
 * @Description: TODO 监测任务
 * @Author xubin
 * @Date 2020/9/15
 * @Version V2.0
 **/
@Slf4j
@Component
public class MonitorJob {

    @Autowired
    private ExceptionInfoMapper exceptionInfoMapper;

    @Autowired
    private ExceptionSendMsgConfigMapper sendMsgConfigMapper;

    @Autowired
    private MailService mailService;

    /**
     * @MethodName: codeException
     * @Description: TODO 代码异常统计
     * @Param: []
     * @Return: void
     * @Author: xubin
     * @Date: 16:29 2020/9/15
     **/
    @Scheduled(cron = "0 0 9,21 * * ?") // 每天9点21点发一次
//    @Scheduled(cron = "0 0/1 * * * ?")
    public void codeException() {
        LocalDateTime localDateTime = LocalDateTime.now(ZoneOffset.of("+8"));
        String startTime = localDateTime.plusHours(-9).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String endTime = localDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        log.info("开始统计时间段异常");
        List<Map<String, Object>> exceptionMsg = exceptionInfoMapper.getExceptionMsg(startTime, endTime);
        if (ObjectUtil.isNotEmpty(exceptionMsg)) {
            StringBuffer contnet = new StringBuffer();
            contnet.append("<h1><font size=\"3\" face=\"verdana\" color=\"#8B3A3A\">代码异常统计报告</font></h1>\n" +
                    "<h2><font size=\"3\" face=\"verdana\" color=\"#00868B\">时间：" + startTime + "-" + endTime + "</font></h2>" +
                    "<table cellpadding=\"0\" cellspacing=\"1\" border=\"0\" style=\"width:800px;text-align:center;font:12px arial;color:#000000;background:#000000;\">\n" +
                    "  <thead>\n" +
                    "  <tr>\n" +
                    "    <th style=\"background:#f0f0f0;padding:10px;width:95px\">异常编码</th>\n" +
                    "    <th style=\"background:#f0f0f0;padding:10px;width:85px;\">数量</th>\n" +
                    "    <th style=\"background:#f0f0f0;padding:10px;width:85px\">描述</th>\n" +
                    "  </tr>\n" +
                    "  </thead>\n" +
                    "  <tbody>\n" +
                    " ");
            for (Map<String, Object> map : exceptionMsg) {
                contnet.append("<tr><td bgcolor=\"#ffffff\" style=\"text-align:center;padding: 10px;\">")
                        .append(map.get("code"))
                        .append("</td>");
                contnet.append("<td bgcolor=\"#ffffff\" style=\"text-align:center\">")
                        .append(map.get("num"))
                        .append("</td>");
                contnet.append("<td bgcolor=\"#ffffff\" style=\"text-align:center\">")
                        .append(map.get("describe"))
                        .append("</td></tr>");
            }
            contnet.append("</tbody></table></br>");
            List<String> maps = sendMsgConfigMapper.selectUserEmail(1);
            if (ObjectUtil.isNotEmpty(maps)) {
                log.info("收件人=[{}]", maps);
                String[] eMails = maps.toArray(new String[0]);
                mailService.sendHtmlMailList(eMails, "7乐服务代码异常统计报告", contnet.toString());
            }
        }

    }

    @Scheduled(cron = "0 0/4 * * * ?")
    public void serviceException() {

        try {

            Map<String, Long> serviceMap = ServiceContainer.getServiceMap();
            log.info("监测服务列表=[{}]", serviceMap);

            if (!serviceMap.isEmpty()) {
                List<Map<String, String>> list = new ArrayList();
                List<ExceptionServiceInfo> records = new ArrayList();
                for (Map.Entry<String, Long> vo : serviceMap.entrySet()) {
                    String serviceName = vo.getKey();
                    long overTime = vo.getValue();
                    long currentTime = System.currentTimeMillis();
                    if (overTime < currentTime) {

                        Map<String, String> msgMap = new LinkedHashMap();
                        String[] s = serviceName.split("_");
                        String dateTimeStr = TimeDateUtils.getDateTimeStr(overTime);

                        msgMap.put("code", s[0]);
                        msgMap.put("name", exceptionInfoMapper.getServiceName(s[0]));
                        msgMap.put("ip", s[1]);
                        msgMap.put("time", dateTimeStr);
                        list.add(msgMap);

                        ExceptionServiceInfo record = new ExceptionServiceInfo();
                        record.setName(s[0]);
                        record.setIp(s[1]);
                        record.setLastConnectionTime(dateTimeStr);
                        records.add(record);

                        serviceMap.remove(serviceName);
                    }
                }
                if (!list.isEmpty()) {
                    StringBuffer contnet = new StringBuffer();
                    contnet.append("<h1><font size=\"8\" face=\"verdana\" color=\"red\">异常服务列表:</font></h1>");
                    contnet.append("<table cellpadding=\"0\" cellspacing=\"1\" border=\"0\" style=\"width:800px;text-align:center;font:12px arial;color:#000000;background:#000000;\">\n" +
                            "  <thead>\n" +
                            "  <tr>\n" +
                            "    <th style=\"background:#f0f0f0;padding:10px;width:95px\">服务名</th>\n" +
                            "    <th style=\"background:#f0f0f0;padding:10px;width:85px;\">服务编码</th>\n" +
                            "    <th style=\"background:#f0f0f0;padding:10px;width:85px\">IP</th>\n" +
                            "    <th style=\"background:#f0f0f0;padding:10px;width:80px\">上次连接时间</th>\n" +
                            "  </tr>\n" +
                            "  </thead>\n" +
                            "  <tbody>\n" +
                            " ");
                    for (Map<String, String> map : list) {
                        contnet.append("<tr><td bgcolor=\"#ffffff\" style=\"text-align:center;padding: 10px;\">")
                                .append(map.get("name"))
                                .append("</td>");
                        contnet.append("<td bgcolor=\"#ffffff\" style=\"text-align:center\">")
                                .append(map.get("code"))
                                .append("</td>");
                        contnet.append("<td bgcolor=\"#ffffff\" style=\"text-align:center\">")
                                .append(map.get("ip"))
                                .append("</td>");
                        contnet.append("<td bgcolor=\"#ffffff\" style=\"text-align:center\">")
                                .append(map.get("time"))
                                .append("</td></tr>");
                    }
                    contnet.append("</tbody></table>");
                    contnet.append("<font size=\"5\" face=\"verdana\" color=\"red\">以上服务可能已经停止运行，请及时处理。</font>");
                    List<String> maps = sendMsgConfigMapper.selectUserEmail(2);
                    if (ObjectUtil.isNotEmpty(maps)) {
                        log.info("收件人=[{}]", maps);
                        String[] eMails = maps.toArray(new String[0]);
                        mailService.sendHtmlMailList(eMails, "7乐服务异常告警", contnet.toString());
                    }
                    list.clear();
                }

                // 保存异常到数据库
                if (!records.isEmpty()) {
                    exceptionInfoMapper.insertServiceException(records);
                    records.clear();
                }

            }

        } catch (Exception e) {
            String message = e.getMessage();
            if (StrUtil.isEmpty(message)) {
                message = "NULL exception";
            }
            message.replaceAll("\n", "");
            message.replaceAll("\t", "");
            message.replaceAll("\r", "");
            mailService.sendHtmlMail("971597044@qq.com", "7乐服务服务异常监控任务发生异常", message);
        }

    }

    /**
     * @MethodName: cleanCodeException
     * @Description: TODO 定时清理代码异常统计数据
     * @Param: [ 每月1号凌晨执行]
     * @Return: void
     * @Author: xubin
     * @Date: 9:08 2020/10/13
     **/
    @Scheduled(cron = "0 0 0 1 * ?")
    public void cleanCodeException() {
        // 清理一个月之前的数据
        exceptionInfoMapper.deleteByCreateTime();

    }


}
