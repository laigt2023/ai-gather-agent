package com.gcloud.demo.uploaddemo.service.impl;

import com.alibaba.fastjson.JSON;
import com.gcloud.demo.uploaddemo.model.EventInfoVo;
import com.gcloud.demo.uploaddemo.params.RequestUploaddemoParams;
import com.gcloud.demo.uploaddemo.service.IUploadToThirdPartyPlatformService;
import com.gcloud.demo.uploaddemo.util.HttpClientUtil;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class UploadToThirdPartyPlatformServiceImpl implements IUploadToThirdPartyPlatformService {
    @Value("${gcloud.upload.jky.picUploadUrl}")
    private String uploadUrl;
    @Value("${gcloud.upload.jky.eventInfoUploadUrl}")
    private String eventInfoUrl;

    @Override
    public void upload(RequestUploaddemoParams params) {
        MultipartFile picFile = null;
        MultipartFile jsonFile = null;
        for (int i = 0; i < params.getFile().length; i++) {
            log.info("接收到文件名：{}",params.getFile()[i].getOriginalFilename());
            //判断MultipartFile的后缀名是否为json
            if(params.getFile()[i].getOriginalFilename().endsWith(".json")){
                jsonFile = params.getFile()[i];
            }else if(params.getFile()[i].getOriginalFilename().endsWith(".jpeg") || params.getFile()[i].getOriginalFilename().endsWith(".JPEG")){
                picFile = params.getFile()[i];
            }
        }

        Map<String, String> uploadParams = new HashMap<String, String>();
        uploadParams.put("file_name", picFile.getOriginalFilename());
        Map result = HttpClientUtil.doPostFile(picFile, uploadUrl, uploadParams);
        log.info("上传图片成功，fileName：" + picFile.getName() + result);

        EventInfoVo eventInfoVo = JSON.parseObject(HttpClientUtil.readMultipartFile(jsonFile), EventInfoVo.class);
        Map<String, String> evetnParams = new HashMap<String, String>();
        evetnParams.put("fileUrl", result.get("fileUrl").toString());
        evetnParams.put("eventName", eventInfoVo.getApp_name());
        try {
            String rsp = HttpClientUtil.sendPostForm(eventInfoUrl, evetnParams);
            log.info("上传事件信息成功:" + evetnParams);
        } catch (Exception e) {
            log.error("上报事件信息失败，" + evetnParams + e.getMessage());
        }

    }
}
