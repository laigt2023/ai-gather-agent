package com.gcloud.demo.uploaddemo.service.impl;

import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson2.util.DateUtils;
import com.gcloud.demo.uploaddemo.model.EventInfo;
import com.gcloud.demo.uploaddemo.params.UploaddemoParams;
import com.gcloud.demo.uploaddemo.util.HttpClientUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import com.gcloud.demo.uploaddemo.service.IUploadToThirdPartyPlatformService;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

//   方案一实现
@Primary
@Slf4j
@Service
public class UploadToThirdPartyPlatformJsonServiceImpl implements IUploadToThirdPartyPlatformService{
    @Value("${gcloud.upload.jky.picUploadUrl}")
    private String uploadUrl;
    @Value("#{${gcloud.event-name}}")
    private Map<String,String> eventNameMap;

    @Override
    public void upload(UploaddemoParams params) {
        MultipartFile picFile = null;
        MultipartFile jsonFile = null;

        for (int i = 0; i < params.getFile().length; i++) {
            log.info("接收到文件名：{}",params.getFile()[i].getOriginalFilename());
            String fileName = params.getFile()[i].getOriginalFilename();

            if(fileName != null && fileName.toLowerCase().endsWith(".json")){
                jsonFile = params.getFile()[i];
            }else if(fileName != null && (fileName.toLowerCase().endsWith(".jpg") || fileName.toLowerCase().endsWith(".jpeg"))){
                picFile = params.getFile()[i];
            }
        }
//        EventInfo eventInfo = JSON.parseObject(HttpClientUtil.readMultipartFile(jsonFile), EventInfo.class);
//        Map<String,String> paramMap = new HashMap<String,String>();

//        paramMap.put("description",eventNameMap.containsKey(eventInfo.getApp_id())?eventNameMap.get(eventInfo.getApp_id()):eventInfo.getApp_name() + "-设备名称：" + eventInfo.getSrc_name() + "，时间：" + DateUtil.format(DateUtil.date(eventInfo.getCreated()*1000L), "YYYY-MM-dd HH:mm:ss"));
        try {
//            log.info("上报事件信息：{}",JSON.toJSON(paramMap).toString());
         //   HttpClientUtil.sendPostJson(uploadUrl, picFile, paramMap);
        } catch (Exception e) {
            log.error("上报事件信息失败，" + picFile.getOriginalFilename() + e.getMessage());
        }
    }
}
