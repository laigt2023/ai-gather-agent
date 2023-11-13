package com.gcloud.demo.uploaddemo.controller;

import com.gcloud.demo.uploaddemo.params.RequestAlarmHistoryPageParams;
import com.gcloud.demo.uploaddemo.params.RequestAlarmHistoryReportParams;
import com.gcloud.demo.uploaddemo.service.IAlarmHistoryService;
import com.gcloud.demo.uploaddemo.service.IFaceInfoService;
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
@RequestMapping("/face_info")
public class FaceInfoController {
    @Resource
    IFaceInfoService server;

    // 告警列表
    @PostMapping("/reload")
    public ResponseEntity reloadFaceDb(String siteId,String loadPath){
        Map<String,Object> result = new HashMap<String,Object>();
        // 刷新人脸库
        server.refreshFaceDb(siteId,loadPath);

        result.put("success","true");
        result.put("code","200");
        return ResponseEntity.ok(result);
    }

    @GetMapping("/image/{uuid}")
    public void face(@PathVariable String uuid){
        // uuid为18位时，当做是身份证号码使用
        String fileUrl = null;
        if(uuid!=null && uuid.length() == 18){
            String idCard = uuid;
            fileUrl = server.getFaceImage(null,idCard);
        }else{
            fileUrl = server.getFaceImage(uuid,null);
        }
        if(fileUrl != null){
            ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            HttpServletResponse response = servletRequestAttributes.getResponse();
            ImageUtil.responsePushImage(response,new File(fileUrl),uuid);
        }
    }
}
