package com.gcloud.demo.uploaddemo.controller;

import com.gcloud.demo.uploaddemo.params.*;
import com.gcloud.demo.uploaddemo.service.IAlarmFileService;
import com.gcloud.demo.uploaddemo.service.IAlarmHistoryService;
import com.gcloud.demo.uploaddemo.util.ImageUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
/**
 * @Author: laigt
 * @Date: 2023-11-07 16:02
 * @Desperation: TODO 告警接口Controller
 */
@Slf4j
@RestController
@RequestMapping("/alarm")
public class AlarmController {
    @Resource
    IAlarmHistoryService server;

    @Resource
    IAlarmFileService alarmFileServer;

    // 告警列表
    @PostMapping("/list")
    public ResponseEntity list(RequestAlarmHistoryPageParams params){
        Map<String,Object> result = new HashMap<String,Object>();
        result.put("success","true");
        result.put("code","200");
        result.put("result",server.list(params));

        return ResponseEntity.ok(result);
    }

    // 告警分页
    @PostMapping("/page")
    public ResponseEntity page(RequestAlarmHistoryPageParams params){
        return ResponseEntity.ok(server.page(params));
    }

    // 告警上报接口
    @PostMapping("/report")
    public ResponseEntity report(@RequestBody RequestAlarmHistoryReportParams params){
        Map<String,Object> result = new HashMap<String,Object>();
        result.put("success","true");
        result.put("code","200");
        result.put("result",server.report(params));

        return ResponseEntity.ok(result);
    }

    //  展示图片
    @GetMapping("/image/{uuid}")
    public void face(@PathVariable String uuid){
        // uuid为18位时，当做是身份证号码使用
        String fileUrl = alarmFileServer.getImagePathByUUID(uuid);

        if(fileUrl != null){
            ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            HttpServletResponse response = servletRequestAttributes.getResponse();
            ImageUtil.responsePushImage(response,new File(fileUrl),uuid);
        }
    }
}
