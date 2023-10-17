package com.gcloud.demo.uploaddemo.cache;

import com.alibaba.fastjson.JSONObject;
import com.gcloud.demo.uploaddemo.util.HttpClientUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;


@Slf4j
@Component
public class TaskInfosCache implements InitializingBean {
    // url - 任务地址对应的 任务信息       key：task任务地址   value：info
    private static Map<String, JSONObject> TASK_URL_AND_INFO_MAP = new HashMap<>();
    public static void init(){
        TASK_URL_AND_INFO_MAP = new HashMap<>();
    }

    @Value("${gcloud.gddi.username}")
    private String username;

    @Value("${gcloud.gddi.password}")
    private String password;

    @Value("${gcloud.gddi.username}")
    private static String gddiUsername;

    @Value("${gcloud.gddi.password}")
    private static String gddiPassword;

    private static String API_PORT = "9090";
    private static String API_PRE = "/api/inflet/v1";

    private static String PROTOCAL = "http://";

    /**
     *
     * @param taskIp 发送方IP（任务产生方机器IP）
     * @param taskId 任务ID
     * @return
     * @throws Exception
     */
    public static JSONObject getTaskInfo(String taskIp,String taskId) throws Exception {
        JSONObject result = new JSONObject();
        String taskUrl = PROTOCAL + taskIp +":" + API_PORT + API_PRE + "/tasks/"+taskId;
        if(TASK_URL_AND_INFO_MAP.containsKey(taskUrl)){
            return TASK_URL_AND_INFO_MAP.get(taskUrl);
        }
        JSONObject taskInfo = getTaskInfoByHttp(taskIp,taskId);

        TASK_URL_AND_INFO_MAP.put(taskUrl,taskInfo);

        return taskInfo;
    }

    /**
     * 发起http请求去获取taskUrl的任务信息
     * @param taskIp 任务所在机器的IP地址
     * @param taskId 任务ID
     * @return
     */
    private static JSONObject getTaskInfoByHttp(String taskIp,String taskId) throws Exception {
        // 请求推理平台获取任务信息
        CloseableHttpClient client = HttpClientBuilder.create().build();
        String taskUrl = PROTOCAL + taskIp + ":" + API_PORT + API_PRE + "/tasks/"+taskId;
        HttpGet getRequest = new HttpGet(taskUrl);

        // 设置token
        String token = getTokenByTaskUrl(taskIp);
        getRequest.setHeader("Authentication", token);
        // send request
        CloseableHttpResponse response = client.execute(getRequest);

        HttpEntity entity = response.getEntity();
        String jsonString = EntityUtils.toString(entity);
        JSONObject json = JSONObject.parseObject(jsonString);
        if( json.get("message") != null && json.get("message").toString().length() > 0 ){
            log.info("获取任务信息失败:("+ getRequest.getURI() +") " + json.get("message").toString());
            return null;
        }
        // 获取到当前上报事件的任务配置数据
        JSONObject data = json.getJSONObject("data");

        return data;

    }

    /**
     * 获取token
     * @param taskIp  任务所在机器的IP地址
     * @return
     * @throws Exception
     */
    private static String getTokenByTaskUrl(String taskIp) throws Exception {
        String loginUrl = PROTOCAL + taskIp + ":" + API_PORT + API_PRE + "/user/login";
        JSONObject postJson =new JSONObject();
        postJson.put("username",gddiUsername);
        postJson.put("password",gddiPassword);

        String jsonString = HttpClientUtil.sendPostJson(loginUrl,postJson);
        JSONObject json = JSONObject.parseObject(jsonString);
        if( json.get("message") != null && json.get("message").toString().length() > 0 ){
            String message = "获取平台Token信息失败:("+ loginUrl +") " + json.get("message").toString();
            log.info(message);
            throw new Exception(message);
        }


        if( json.getJSONObject("data") != null && json.getJSONObject("data").getString("token")!=null ){
            log.info("获取平台Token信息成功:("+ loginUrl +") :" + json.getJSONObject("data").getString("token"));
            return json.getJSONObject("data").getString("token");
        }

        return "";
    }


    public static String getGddiUsername() {
        return gddiUsername;
    }
    public static void setGddiUsername(String gddiUsername) {
        TaskInfosCache.gddiUsername = gddiUsername;
    }

    public static String getGddiPassword() {
        return gddiPassword;
    }
    public static void setGddiPassword(String gddiPassword) {
        TaskInfosCache.gddiPassword = gddiPassword;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        gddiUsername = username;
        gddiPassword = password;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}



