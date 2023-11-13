package com.gcloud.demo.uploaddemo.mybatisplus.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("alarm_files")
public class AlarmFile {
    @TableId(value="id",type= IdType.AUTO)
    // 自增ID
    private Long id;
    // uuid
    private String uuid;
    // 项目ID
    @TableField(value="site_id")
    private String siteID;
    // 保存路径
    private String path;
    // 保存文件目录
    private String saveDir;
    // 创建时间
    private Date createTime;



}
