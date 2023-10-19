package com.gcloud.demo.uploaddemo.thread;

import com.gcloud.demo.uploaddemo.params.RequestFaceImageComparisonParams;
import com.gcloud.demo.uploaddemo.service.IUploadToBeijingPlatformService;
import com.gcloud.demo.uploaddemo.util.SpringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Date;


@Slf4j
public class FaceImageComparisonThread extends Thread{
    private RequestFaceImageComparisonParams params;

    @Override
    public void run() {
        // 记录开始时间
        String startDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        log.info(startDate + "任务ID（" + Thread.currentThread().getId() + ") : 启动成功");
        try{
            IUploadToBeijingPlatformService service = SpringUtils.getBean(IUploadToBeijingPlatformService.class);
            service.faceImageComparisonTask(this.params);
            log.info(startDate + "任务ID（" + Thread.currentThread().getId() + ") : 运行完成：");
        }catch (Exception e){
            log.info(startDate + "任务ID（" + Thread.currentThread().getId() + ") : 运行失败：" + e.getMessage());
        }

    }

    public void startTaskByParams(RequestFaceImageComparisonParams p){
        this.setParams(p);
        this.start();
    }

    public RequestFaceImageComparisonParams getParams() {
        return params;
    }

    public void setParams(RequestFaceImageComparisonParams params) {
        this.params = params;
    }
}
