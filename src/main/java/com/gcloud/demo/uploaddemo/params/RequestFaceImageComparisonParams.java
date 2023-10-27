package com.gcloud.demo.uploaddemo.params;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RequestFaceImageComparisonParams {
    //工地ID
    private String siteID;
    // 告警日期：例：2023-08-10
    private String alarmDate;
    //
    private Integer type;
    // 告警视频名称
    private String videoName;
    //事件类型：0-安全帽监测、1-反光衣监测、15-安全帽+人脸识别、16-反光衣+人脸识别
    // 6-吸烟 7-人员聚集 17-电子围栏 9-车辆违停 11-睡岗 14-人脸（抓拍/识别）
    private Integer eventType;
    // 安全帽名称
    private String cameraName;
    // 告警图片(base64，不带前缀data:image/jpeg;base64,)
    private String alarmPicture;
    // 告警时间 时间戳，到（毫秒）
    private Long alarmTime;

    private List<Map<String,Object>> info = new ArrayList<>();

    public String getSiteID() {
        return siteID;
    }

    public void setSiteID(String siteID) {
        this.siteID = siteID;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public String getAlarmDate() {
        return alarmDate;
    }

    public void setAlarmDate(String alarmDate) {
        this.alarmDate = alarmDate;
    }

    public String getVideoName() {
        return videoName;
    }

    public void setVideoName(String videoName) {
        this.videoName = videoName;
    }

    public Integer getEventType() {
        return eventType;
    }

    public void setEventType(Integer eventType) {
        this.eventType = eventType;
    }

    public String getCameraName() {
        return cameraName;
    }

    public void setCameraName(String cameraName) {
        this.cameraName = cameraName;
    }

    public List<Map<String, Object>> getInfo() {
        return info;
    }

    public String getAlarmPicture() {
        return alarmPicture;
    }

    public void setAlarmPicture(String alarmPicture) {
        this.alarmPicture = alarmPicture;
    }

    public Long getAlarmTime() {
        return alarmTime;
    }

    public void setAlarmTime(Long alarmTime) {
        this.alarmTime = alarmTime;
    }

    public void setInfo(List<Map<String, Object>> info) {
        this.info = info;
    }
}

