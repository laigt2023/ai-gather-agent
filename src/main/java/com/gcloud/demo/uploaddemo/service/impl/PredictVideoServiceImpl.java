package com.gcloud.demo.uploaddemo.service.impl;

import com.gcloud.demo.uploaddemo.thread.DownloadAndPredictThread;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.Map;

import com.gcloud.demo.uploaddemo.model.PredictVideoParams;
import com.gcloud.demo.uploaddemo.service.IPredictVideoService;
import org.springframework.util.StringUtils;

@Primary
@Slf4j
@Service
public class PredictVideoServiceImpl implements IPredictVideoService {

    @Value("${gcloud.video-save-dir}")
    private String videoSaveDir;
    @Value("${gcloud.predict-frame-rate}")
    private String predictFrameRate;
    // 推理类型对应的推理接口地址Map
    @Value("#{${gcloud.video-predict-type}}")
    private Map<String,String> predictUrlTypeMap;


    @Override
    public String downloadAndPredict(PredictVideoParams params) {
        String outDir = videoSaveDir;
        if(outDir.endsWith("/") || outDir.endsWith("\\")){
            outDir = outDir.substring(0,outDir.length()-1);
        }

        String downloadAddress = params.getDownloadAddress();
        //  发起http请求，调用第三方平台的接口，进行视频下载
        String[] array = downloadAddress.split("/");
        String fileName = array[array.length - 1];
        String alarmDate = array[array.length - 2];
        String projectId = array[array.length - 3];

        String videoPath = outDir + File.separator + projectId + File.separator + alarmDate + File.separator + fileName;


        String aiApiUrl = predictUrlTypeMap.get(params.getType());
        if(!params.getType().equals("all") && (aiApiUrl == null || aiApiUrl.equals(""))){
            return "error: 【" + params.getType() +"】 类型不存在，未找到对应的解析配置接口！";
        }

        try {
            return downloadFile(downloadAddress, videoPath,params.getType());
        } catch (IOException e) {
            e.printStackTrace();
            return "system error:" + e.getMessage();
        }
    }

/**
     * 下载文件
     * @param url  下载地址
     * @param filePath  文件保存路径
     * @param aiType  推理类型  all-全部解析  helmet-安全帽  vest-安全服
     * @return
     * @throws IOException
     */
    public String downloadFile(String url, String filePath, String aiType) throws IOException {
        // 文件下载成功后生成文件名，如：PU_23020036_00_20230429_163238_LA0800.mp4.success
        String downloadingFileName = filePath + ".downloading";
        String successFileName = filePath + ".success";
        File downloadingFile = new File(downloadingFileName);
        File successFile = new File(successFileName);
        if(downloadingFile.exists()){
            return "success: 文件正在上传，无需重复操作！";
        }

        DownloadAndPredictThread thread = new DownloadAndPredictThread(url,filePath,predictFrameRate,downloadingFileName,successFileName,aiType,predictUrlTypeMap);

 //      如果已经上传完成的直接执行解析操作
        if(successFile.exists()){
            thread.setDownloadCompleted(true);
            thread.start();
            return "success: 文件已经上传完成,请等待解析完成！";
        }else{
            thread.setDownloadCompleted(false);
            thread.start();
            return "success: 文件上传任务开启成功,请等待解析完成！";
        }
    }
}
