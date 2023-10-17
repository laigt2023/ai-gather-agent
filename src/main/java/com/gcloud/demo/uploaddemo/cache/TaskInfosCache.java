package com.gcloud.demo.uploaddemo.cache;

import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Value;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
@Slf4j
public class TaskInfosCache {
    // url - 任务地址对应的 任务信息       key：task任务地址   value：info
    private static Map<String, JSONObject> TASK_URL_AND_INFO_MAP = new HashMap<>();
    public static void init(){
        TASK_URL_AND_INFO_MAP = new HashMap<>();
    }

    @Value("${gcloud.gddi.username}")
    private String gddiUsername;
    @Value("${gcloud.gddi.password}")
    private String gddiPassword;

    public JSONObject getTaskInfo(String taskUrl){
        JSONObject result = new JSONObject();

        if(TASK_URL_AND_INFO_MAP.containsKey(taskUrl)){
            return TASK_URL_AND_INFO_MAP.get(taskUrl);
        }






        return result;
    }


    /**
     * 发起http请求去获取taskUrl的任务信息
     * @param taskUrl 任务地址
     */
    private static void httpToTaskUrl(String taskUrl) throws IOException {
        // 请求推理平台获取任务信息
        CloseableHttpClient client = HttpClientBuilder.create().build();
        HttpGet getRequest = new HttpGet(taskUrl);

        // 设置token
        getRequest.setHeader("Authentication", getTokenByTaskUrl(taskUrl));
        // send request
        CloseableHttpResponse response = client.execute(getRequest);

        HttpEntity entity = response.getEntity();
        String jsonString = EntityUtils.toString(entity);
        JSONObject json = JSONObject.parseObject(jsonString);
        if( json.get("message") != null && json.get("message").toString().length() > 0 ){
            log.info("获取任务信息失败:("+ getRequest.getURI() +") " + json.get("message").toString());
            return;
        }
    }

    private static String getTokenByTaskUrl(String url){
        String gddiApiToken = "";
        return gddiApiToken;
    }
}
