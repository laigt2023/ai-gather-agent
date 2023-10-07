package com.gcloud.demo.uploaddemo.controller;

import cn.hutool.http.server.HttpServerRequest;
import com.alibaba.fastjson.JSONObject;
import com.gcloud.demo.uploaddemo.model.PredictVideoParams;
import com.gcloud.demo.uploaddemo.params.EventInfoParams;
import com.gcloud.demo.uploaddemo.params.FaceFileParams;
import com.gcloud.demo.uploaddemo.params.TestReportParams;
import com.gcloud.demo.uploaddemo.params.UploaddemoParams;
import com.gcloud.demo.uploaddemo.service.IPredictVideoService;
import com.gcloud.demo.uploaddemo.service.IUploadToBeijingPlatformService;
import com.gcloud.demo.uploaddemo.service.IUploadToThirdPartyPlatformService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
public class UploaddemoController {
    @Resource
    IUploadToThirdPartyPlatformService uploadToThirdPartyPlatformService;

    @Resource
    IUploadToBeijingPlatformService uploadToBeijingPlatformService;
    @Resource
    IPredictVideoService predictVideoService;


    @Resource
    HttpServletRequest request;

    // 推理视频接口（传参mp4，推理类型）
    @PostMapping("/predictVideo")
    public ResponseEntity upload(PredictVideoParams params){
        log.info("type:" + params.getType() + "  downloadAddress   :" + params.getDownloadAddress());
        Map<String,Object> result = new HashMap<String,Object>();
        result.put("success","true");
        result.put("code","200");
        result.put("result",predictVideoService.downloadAndPredict(params));

        return ResponseEntity.ok(result);
    }

    // 上报数据
    @PostMapping("/upload")
    public ResponseEntity uploadJKY(UploaddemoParams  params){
        params.setAppKeyID(request.getHeader("AppKeyID"));
        params.setAppKeySecret(request.getHeader("AppKeySecret"));
        uploadToThirdPartyPlatformService.upload(params);
        return ResponseEntity.ok("upload success");
    }

    @PostMapping("/beijing/upload")
    public ResponseEntity uploadBeijing(UploaddemoParams  params){
        params.setAppKeyID(request.getHeader("AppKeyID"));
        params.setAppKeySecret(request.getHeader("AppKeySecret"));
        uploadToBeijingPlatformService.upload(params);
        return ResponseEntity.ok("upload success");
    }

    @PostMapping("/beijing/face")
    public ResponseEntity uploadBeijingFace(FaceFileParams params){
        JSONObject result = uploadToBeijingPlatformService.faceComparison(params);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/eventInfo")
    public ResponseEntity upload(EventInfoParams params){
        log.info(params.getEventName() + " ," + params.getFileUrl());
        return ResponseEntity.ok("");
    }


    @PostMapping("/report")
    public ResponseEntity report(@RequestBody TestReportParams params){
        Map<String,Object> result = new HashMap<String,Object>();
        result.put("success","true");
        result.put("code","200");
//        result.put("result",params);

        return ResponseEntity.ok(result);
    }
}
