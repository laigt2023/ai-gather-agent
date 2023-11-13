package com.gcloud.demo.uploaddemo.mybatisplus.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;


@Data
@TableName("face_info")
public class FaceInfo {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    // 唯一uuid
    private String uuid;
    // 项目ID
    private String siteId;
    // 唯一UUID
    private String idCard;
    // 真实姓名
    private String realName;
    // 性别 0-女 1-男
    private Integer sex = 1;
    // 单位/部门
    private String deptName;
    // 工种/职位
    private String workTypeName;
    // 项目名称
    private String siteName;
    // 联系方式
    private String phone;
    // 人脸文件保留路径
    private String faceFilePath;
    // 更新时间
    private Date updateDate;

}