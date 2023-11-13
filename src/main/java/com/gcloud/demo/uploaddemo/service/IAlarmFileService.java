package com.gcloud.demo.uploaddemo.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.gcloud.demo.uploaddemo.mybatisplus.entity.AlarmFile;
import com.gcloud.demo.uploaddemo.params.RequestAlarmFilePageParams;

import java.util.List;

public interface IAlarmFileService {
    /**
     * 获取列表
     * @param params
     * @return
     */
    List<AlarmFile> list(RequestAlarmFilePageParams params);

    /**
     * 获取分页
     * @param params
     * @return
     */
    IPage<AlarmFile> page(RequestAlarmFilePageParams params);

    /**
     * 通过UUID获取告警事件
     * @param uuid
     * @return
     */
    AlarmFile getOneByUUID(String uuid);

    /**
     * 通过UUID获取告警事件图片存储路径
     * @param uuid
     * @return
     */
    String getImagePathByUUID(String uuid);
}
