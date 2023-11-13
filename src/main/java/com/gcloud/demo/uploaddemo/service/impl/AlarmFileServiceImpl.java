package com.gcloud.demo.uploaddemo.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gcloud.demo.uploaddemo.exception.GcException;
import com.gcloud.demo.uploaddemo.mybatisplus.entity.AlarmFile;
import com.gcloud.demo.uploaddemo.mybatisplus.mapper.AlarmFileMapper;
import com.gcloud.demo.uploaddemo.params.RequestAlarmFilePageParams;
import com.gcloud.demo.uploaddemo.service.IAlarmFileService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class AlarmFileServiceImpl implements IAlarmFileService {

    @Autowired
    private AlarmFileMapper alarmFileMapper;

    @Override
    public List<AlarmFile> list(RequestAlarmFilePageParams params) {
        List<AlarmFile> AlarmFileList = alarmFileMapper.selectList(null);
        return AlarmFileList;
    }

    @Override
    public IPage<AlarmFile> page(RequestAlarmFilePageParams params) {
        Page<AlarmFile> page = Page.of(params.getPageNo(),params.getPageSize());

        //queryWrapper组装查询where条件
        LambdaQueryWrapper<AlarmFile> queryWrapper = new LambdaQueryWrapper<>();

        IPage<AlarmFile> result = alarmFileMapper.selectPage(page,queryWrapper);

        return result;
    }

    @Override
    public AlarmFile getOneByUUID(String uuid) {
        if(StringUtils.isBlank(uuid)){
            new GcException("uuid is null :: UUID 不能为空");
        }
        QueryWrapper<AlarmFile> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("uuid",uuid);
        return alarmFileMapper.selectOne(queryWrapper);
    }

    @Override
    public String getImagePathByUUID(String uuid) {
        if(StringUtils.isBlank(uuid)){
            new GcException("get_alarm_image_0001 :: UUID 不能为空");
        }

        AlarmFile alarm = getOneByUUID(uuid);

        if(alarm == null){
            new GcException("get_alarm_image_0002 :: 不存在指定异常事件图片记录");
        }

        return alarm.getPath();
    }
}
