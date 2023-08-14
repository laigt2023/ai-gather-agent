package com.gcloud.demo.uploaddemo.service;

import com.gcloud.demo.uploaddemo.model.PredictVideoParams;

public interface IPredictVideoService {
    String downloadAndPredict(PredictVideoParams params);
}
