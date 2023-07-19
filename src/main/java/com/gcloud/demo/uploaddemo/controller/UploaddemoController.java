package com.gcloud.demo.uploaddemo.controller;

import cn.hutool.http.server.HttpServerRequest;
import com.gcloud.demo.uploaddemo.params.EventInfoParams;
import com.gcloud.demo.uploaddemo.params.UploaddemoParams;
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
import java.util.Map;

@Slf4j
@RestController
public class UploaddemoController {
    @Resource
    IUploadToThirdPartyPlatformService uploadToThirdPartyPlatformService;
    @Resource
    HttpServletRequest request;

    // 上报数据
    @PostMapping("/upload")
    public ResponseEntity uploadJKY(UploaddemoParams  params){
        params.setAppKeyID(request.getHeader("AppKeyID"));
        params.setAppKeySecret(request.getHeader("AppKeySecret"));
        uploadToThirdPartyPlatformService.upload(params);
        return ResponseEntity.ok("upload success");
    }
    @PostMapping("/eventInfo")
    public ResponseEntity upload(EventInfoParams params){
        log.info(params.getEventName() + " ," + params.getFileUrl());
        return ResponseEntity.ok("");
    }
}
