package com.gcloud.demo.uploaddemo.thread;

import com.gcloud.demo.uploaddemo.exception.GcException;
import com.gcloud.demo.uploaddemo.params.RequestFaceImageComparisonParams;
import com.gcloud.demo.uploaddemo.service.IUploadToBeijingPlatformService;
import com.gcloud.demo.uploaddemo.util.SpringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.apache.commons.codec.binary.Base64;

import java.text.SimpleDateFormat;
import java.util.Date;


@Slf4j
public class FaceImageComparisonThread extends Thread{
    private RequestFaceImageComparisonParams params;
    private String BASE64_IMAGE_PRE="data:image/jpeg;base64,";
    @Override
    public void run() {
        // 记录开始时间
        String startDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        log.info(startDate + "任务ID（" + Thread.currentThread().getId() + ") : 启动成功");
        try{
            IUploadToBeijingPlatformService service = SpringUtils.getBean(IUploadToBeijingPlatformService.class);
            service.faceImageComparisonTask(this.params);

            String base64Message = this.params.getAlarmPicture();
            if(base64Message != null && base64Message.length() > 60){
                log.info(startDate + "当前任务 ba64数据参数【alarmPicture】（" + Thread.currentThread().getId() + "）  : "+base64Message.substring(0,30) + "..." + base64Message.substring(base64Message.length() - 30, base64Message.length()));
            }
            log.info(startDate + "任务ID（" + Thread.currentThread().getId() + "） : 运行完成：");


        }catch (Exception e){
            log.info(startDate + "任务ID（" + Thread.currentThread().getId() + "）: 运行失败：" + e.getMessage());
        }

    }

    public boolean isBase64ImageComplete(String base64Image) {
        // 检查Base64字符串长度
        if (base64Image.length() % 4 != 0) {
            return false;
        }

        // 解码Base64字符串为字节数组
        byte[] imageData = Base64.decodeBase64(base64Image);

        // 检查解码后的字节数组是否为空
        if (imageData == null || imageData.length == 0) {
            return false;
        }

        // 可以进一步检查字节数组是否符合图片格式的规定

        return true;
    }

    public String startTaskByParams(RequestFaceImageComparisonParams p){
        String message = "SUCCESS";

        if(p == null){
            throw new GcException("参数不能为空");
        }

        if(p.getAlarmPicture() == null){
            throw new GcException("【alarmPicture】参数不能为空");
        }

        // 如果误传了data:image/jpeg;base64, 前缀则清除掉
        if(p.getAlarmPicture() != null && p.getAlarmPicture().indexOf(BASE64_IMAGE_PRE) == 0){
            p.setAlarmPicture(p.getAlarmPicture().replace(BASE64_IMAGE_PRE,""));
        }

        if(isBase64ImageComplete(BASE64_IMAGE_PRE + p.getAlarmPicture())){
            throw new GcException("【alarmPicture】base64格式解析错误");
        }

        this.setParams(p);
        this.start();

        return message;
    }


    public RequestFaceImageComparisonParams getParams() {
        return params;
    }

    public void setParams(RequestFaceImageComparisonParams params) {
        this.params = params;
    }
}
