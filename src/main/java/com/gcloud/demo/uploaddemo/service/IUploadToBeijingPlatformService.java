package com.gcloud.demo.uploaddemo.service;

import com.alibaba.fastjson.JSONObject;
import com.gcloud.demo.uploaddemo.params.FaceFileParams;
import com.gcloud.demo.uploaddemo.params.UploaddemoParams;
import org.springframework.web.multipart.MultipartFile;

public interface IUploadToBeijingPlatformService {
    void upload(UploaddemoParams params);
    JSONObject faceComparison(FaceFileParams params);
}
