package com.gcloud.demo.uploaddemo.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.gcloud.demo.uploaddemo.mybatisplus.entity.AlarmHistory;
import com.gcloud.demo.uploaddemo.params.RequestAlarmHistoryPageParams;
import com.gcloud.demo.uploaddemo.params.RequestAlarmHistoryReportParams;

import java.util.List;

public interface IAlarmHistory {
    List<AlarmHistory> list(RequestAlarmHistoryPageParams params);

    IPage<AlarmHistory> page(RequestAlarmHistoryPageParams params);

    int report (RequestAlarmHistoryReportParams params);
}
