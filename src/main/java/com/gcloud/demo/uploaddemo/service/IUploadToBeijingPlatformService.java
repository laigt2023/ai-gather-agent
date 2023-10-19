package com.gcloud.demo.uploaddemo.service;

import com.alibaba.fastjson.JSONObject;
import com.gcloud.demo.uploaddemo.params.RequestFaceFileParams;
import com.gcloud.demo.uploaddemo.params.RequestFaceImageComparisonParams;
import com.gcloud.demo.uploaddemo.params.RequestUploaddemoParams;

public interface IUploadToBeijingPlatformService {
    void upload(RequestUploaddemoParams params);
    JSONObject faceComparison(RequestFaceFileParams params);

    /**
     * 人脸图片匹配，先进行异常事件识别根据params.eventType,再根据返回信息进行人脸识别
     * 需要开启多进程模式进行任务处理
     * @param params
     * @return
     */
    JSONObject faceImageComparisonTask(RequestFaceImageComparisonParams params);
}
