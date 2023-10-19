package com.gcloud.demo.uploaddemo.params;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RequestTestReportParams {
    private String siteID;
    private String alarmDate;

    private Integer type;
    private String videoName;
    private String eventType;
    private String cameraName;

    private String alarmPicture;
    private String alarmTime;

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

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
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

    public String getAlarmTime() {
        return alarmTime;
    }

    public void setAlarmTime(String alarmTime) {
        this.alarmTime = alarmTime;
    }

    public void setInfo(List<Map<String, Object>> info) {
        this.info = info;
    }
}

