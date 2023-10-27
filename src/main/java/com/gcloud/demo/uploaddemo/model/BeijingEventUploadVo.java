package com.gcloud.demo.uploaddemo.model;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class BeijingEventUploadVo {
    private String siteID="1";
    private Integer type=2;
    private String alarmDate;
    private String videoName;
    private String cameraName;
    private Integer eventType;
    private Long alarmTime;
    private String alarmPicture;
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

    public String getCameraName() {
        return cameraName;
    }

    public void setCameraName(String cameraName) {
        this.cameraName = cameraName;
    }

    public Integer getEventType() {
        return eventType;
    }

    public void setEventType(Integer eventType) {
        this.eventType = eventType;
    }

    public Long getAlarmTime() {
        return alarmTime;
    }

    public void setAlarmTime(Long alarmTime) {
        this.alarmTime = alarmTime;
    }

    public String getAlarmPicture() {
        return alarmPicture;
    }

    public void setAlarmPicture(String alarmPicture) {
        this.alarmPicture = alarmPicture;
    }

    public List<Map<String, Object>> getInfo() {
        return info;
    }

    public void setInfo(List<Map<String, Object>> info) {
        this.info = info;
    }

    public BeijingEventUploadVo() {
        super();
        this.siteID= "1";
        this.type = 2;
        this.alarmTime = System.currentTimeMillis();
        // 获取当前时间的日期
        this.alarmDate = new SimpleDateFormat("yyyy-MM-dd").format(alarmTime);
    }

    public BeijingEventUploadVo(String siteID) {
        super();
        this.siteID= siteID;
        this.type = 2;
        this.alarmTime = System.currentTimeMillis();
        // 获取当前时间的日期
        this.alarmDate = new SimpleDateFormat("yyyy-MM-dd").format(alarmTime);
    }
}
