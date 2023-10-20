package com.gcloud.demo.uploaddemo.controller;

import com.alibaba.fastjson.JSONObject;
import com.gcloud.demo.uploaddemo.model.PredictVideoParams;
import com.gcloud.demo.uploaddemo.params.*;
import com.gcloud.demo.uploaddemo.service.IPredictVideoService;
import com.gcloud.demo.uploaddemo.service.IUploadToBeijingPlatformService;
import com.gcloud.demo.uploaddemo.service.IUploadToThirdPartyPlatformService;
import com.gcloud.demo.uploaddemo.thread.FaceImageComparisonThread;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

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
    public ResponseEntity uploadJKY(RequestUploaddemoParams params){
        params.setAppKeyID(request.getHeader("AppKeyID"));
        params.setAppKeySecret(request.getHeader("AppKeySecret"));
        uploadToThirdPartyPlatformService.upload(params);
        return ResponseEntity.ok("upload success");
    }

    @PostMapping("/beijing/upload")
    public ResponseEntity uploadBeijing(RequestUploaddemoParams params){
        params.setAppKeyID(request.getHeader("AppKeyID"));
        params.setAppKeySecret(request.getHeader("AppKeySecret"));
        uploadToBeijingPlatformService.upload(params);
        return ResponseEntity.ok("upload success");
    }

    @PostMapping("/beijing/face")
    public ResponseEntity uploadBeijingFace(RequestFaceFileParams params){
        JSONObject result = uploadToBeijingPlatformService.faceComparison(params);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/image/face_report")
    public ResponseEntity uploadBeijingFaceImageReport(@RequestBody RequestFaceImageComparisonParams params){
        FaceImageComparisonThread task = new FaceImageComparisonThread();
        task.startTaskByParams(params);

        JSONObject resultMessage = new JSONObject();
        resultMessage.put("status",true);
        resultMessage.put("message","图片识别任务已开启,请耐心等待数据上报");
        resultMessage.put("result",null);
        resultMessage.put("timestamp",System.currentTimeMillis());
        return ResponseEntity.ok(resultMessage);
    }

    @PostMapping("/eventInfo")
    public ResponseEntity upload(RequestEventInfoParams params){
        log.info(params.getEventName() + " ," + params.getFileUrl());
        return ResponseEntity.ok("");
    }


    @PostMapping("/report")
    public ResponseEntity report(@RequestBody RequestTestReportParams params){
        Map<String,Object> result = new HashMap<String,Object>();
        result.put("success","true");
        result.put("code","200");
//        result.put("result",params);

        return ResponseEntity.ok(result);
    }
}
