package com.gcloud.demo.uploaddemo.params;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;

import java.util.Date;

@Data
public class RequestAlarmFilePageParams extends BasePageParams{
    private Long id;
    // 项目ID
    @TableField(value="site_id")
    private String siteID;
    // 保存路径
    private Long path;
    // 保存文件目录
    @Value("${gcloud.save-dir}")
    private String SAVE_DIR;

    // 创建时间
    private Date createTime;
}
