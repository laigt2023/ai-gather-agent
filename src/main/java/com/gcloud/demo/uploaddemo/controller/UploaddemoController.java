package com.gcloud.demo.uploaddemo.controller;

import com.gcloud.demo.uploaddemo.params.EventInfoParams;
import com.gcloud.demo.uploaddemo.params.UploaddemoParams;
import com.gcloud.demo.uploaddemo.service.IUploadToThirdPartyPlatformService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
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

    // 上报数据 （已联调） - 暂时先不上传（待对方调整好后，后续更改为使用此接口） - 主要
    @PostMapping("/upload")
    public ResponseEntity uploadJKY(UploaddemoParams  params){
        // 方案二（目前使用）
        uploadToThirdPartyPlatformService.upload(params);
        return ResponseEntity.ok("upload success");
    }
    // 小盒子配置：服务地址
    @PostMapping("/upload1")
    public ResponseEntity upload(UploaddemoParams  params){
        log.info(params.getFile_name());
        //循环打印file数组
        for (int i = 0; i < params.getFile().length; i++) {
            log.info(params.getFile()[i].getOriginalFilename());
            //把文件保存在本地
            try {
                params.getFile()[i].transferTo(new File("D:\\uploadtest\\" + params.getFile()[i].getOriginalFilename()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return ResponseEntity.ok("{'fileUrl':'http://www.baidu.com'}");
    }

    @PostMapping("/eventInfo")
    public ResponseEntity upload(EventInfoParams params){
        log.info(params.getEventName() + " ," + params.getFileUrl());
        return ResponseEntity.ok("");
    }
}
