package com.gcloud.demo.uploaddemo.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gcloud.demo.uploaddemo.mybatisplus.entity.AlarmHistory;
import com.gcloud.demo.uploaddemo.mybatisplus.mapper.AlarmHistoryMapper;
import com.gcloud.demo.uploaddemo.params.RequestAlarmHistoryPageParams;
import com.gcloud.demo.uploaddemo.params.RequestAlarmHistoryReportParams;
import com.gcloud.demo.uploaddemo.service.IAlarmHistory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class AlarmHistoryImpl implements IAlarmHistory {

    @Autowired
    private AlarmHistoryMapper alarmHistoryMapper;


    @Override
    public List<AlarmHistory> list(RequestAlarmHistoryPageParams params) {
        List<AlarmHistory> alarmHistoryList = alarmHistoryMapper.selectList(null);
        for(AlarmHistory alarm:alarmHistoryList) {
            System.out.println(alarm);
        }

        return alarmHistoryList;
    }

    @Override
    public IPage<AlarmHistory> page(RequestAlarmHistoryPageParams params) {
        Page<AlarmHistory> page = Page.of(params.getPageNo(),params.getPageSize());

        //queryWrapper组装查询where条件
        LambdaQueryWrapper<AlarmHistory> queryWrapper = new LambdaQueryWrapper<>();

        IPage<AlarmHistory> result = alarmHistoryMapper.selectPage(page,queryWrapper);

        return result;
    }

    @Override
    public int report(RequestAlarmHistoryReportParams params) {
        AlarmHistory entity = new AlarmHistory();

        entity.setSiteID(params.getSiteID());
        entity.setType(params.getType());
        entity.setEventType(params.getEventType());
        entity.setAlarmDate(params.getAlarmDate());
        if(params.getAlarmTime() != null){
            entity.setAlarmTime(new Date(params.getAlarmTime()));
        }else{
            entity.setAlarmTime(new Date());
        }
        entity.setVideoName(params.getVideoName());
        entity.setCameraName(params.getCameraName());

        if(params.getInfo()!=null && !params.getInfo().isEmpty()){
            String josnText = JSONArray.toJSONString(params.getInfo());
            entity.setInfo(josnText);
        }

        // 暂时不保存告警图片的 base64格式数据

        alarmHistoryMapper.insert(entity);
        return 1;
    }
}
