package com.gcloud.demo.uploaddemo.mybatisplus.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("alarm_history")
public class AlarmHistory {
    @TableId(value="id",type= IdType.AUTO)
    // 自增ID
    private Long id;
    // 告警类型：1=智能安全帽、2=固定摄像头
    @TableField(value="site_id")
    private String siteID;
    // 项目ID
    private Long type;
    // 告警时间戳（毫秒）
    private Date alarmTime;
    // 告警日期
    private String alarmDate;
    // 智能安全帽-传告警视频名称 / 固定摄像头-传通道名称
    private String videoName;
    // 智能安全帽-传安全帽名称
    private String cameraName;
    // 事件类型0=安全帽监测、1=反光衣监测、15=安全帽+人脸识别、16=反光衣+人脸识别、14=人脸（抓拍/识别）
    private Long eventType;
    // 图片路径
    private String alarmPictureUrl;
    // 图片base64
    private String alarmPicture;
    // 告警图片文件UUID
    private String alarmFileUuid;
    // 人脸身份证ID
    private String faceIdCard;

    private String info;
}
