package com.gcloud.demo.uploaddemo.controller;

import com.gcloud.demo.uploaddemo.params.*;
import com.gcloud.demo.uploaddemo.service.IAlarmHistory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
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
    IAlarmHistory server;

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
    @PostMapping("/image:id")
    public void image(){

    }
}
