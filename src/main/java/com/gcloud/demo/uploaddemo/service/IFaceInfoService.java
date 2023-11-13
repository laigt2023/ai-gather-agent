package com.gcloud.demo.uploaddemo.service;

import com.gcloud.demo.uploaddemo.mybatisplus.entity.FaceInfo;

import java.util.List;

public interface IFaceInfoService {
    /**
     * 人脸库加载路径
     * @param loadPath
     */
    void refreshFaceDb(String SiteId,String loadPath);

    FaceInfo getFaceInfoByIdCard(String idCard);

    /**
     * 人脸信息列表
     * @return
     */
    List<FaceInfo> list();

    String getFaceImage(String uuid,String idCard);
}
