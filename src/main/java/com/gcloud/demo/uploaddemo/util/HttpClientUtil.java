package com.gcloud.demo.uploaddemo.util;

import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.springframework.http.RequestEntity;
import org.springframework.web.multipart.MultipartFile;
import sun.misc.BASE64Encoder;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
@Slf4j
public class HttpClientUtil {
    // 常规调用
    public static String sendPostForm(String url, Map<String, String> params) throws Exception {
        CloseableHttpClient client = HttpClientBuilder.create().build();
        HttpPost request = new HttpPost(url);

        // set params
        if (params != null) {
            List<NameValuePair> nameValuePairList = new ArrayList<NameValuePair>();
            for (Map.Entry<String, String> entry : params.entrySet()) {
                nameValuePairList.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
            }
            UrlEncodedFormEntity bodyEntity = new UrlEncodedFormEntity(nameValuePairList, "UTF-8");
            request.setEntity(bodyEntity);
        }

        // send request
        CloseableHttpResponse response = client.execute(request);
        // read rsp code
        log.info("rsp code:" + response.getStatusLine().getStatusCode());
        // return content
        String ret = readResponseContent(response.getEntity().getContent());
        response.close();
        return ret;
    }

    public static String sendPostJson(String url, JSONObject jsonObject) throws Exception {
        CloseableHttpClient client = HttpClientBuilder.create().build();
        HttpPost request = new HttpPost(url);
        StringEntity se = new StringEntity(jsonObject.toString(), StandardCharsets.UTF_8);
        se.setContentType("application/json");
        se.setContentEncoding("UTF-8");
        request.setEntity(se);
        CloseableHttpResponse response = client.execute(request);
        // read rsp code
        log.info("rsp code:" + response.getStatusLine().getStatusCode());
        // return content
        String ret = readResponseContent(response.getEntity().getContent());
        log.info("rsp content:" + ret);
        response.close();
        return ret;
    }

    public static String sendPostJson(String url, MultipartFile file, Map<String, String> params,String skillType) throws Exception {
        CloseableHttpClient client = HttpClientBuilder.create().build();
        HttpPost request = new HttpPost(url);

        BASE64Encoder encoder = new BASE64Encoder();

        JSONObject jsonObject = new JSONObject();
//        中建通旧项目地址
//        jsonObject.put("projectId","1382169102120566786");
        jsonObject.put("projectId","1645960692836073472");
        jsonObject.put("skillType",skillType);
        jsonObject.put("renderImageBase64", ImageUtil.getImageStr(file.getInputStream()));
        for (Map.Entry<String, String> entry : params.entrySet()) {
            jsonObject.put(entry.getKey(),entry.getValue());
        }
        log.info("jsonObject:" + jsonObject.toString());

        StringEntity se = new StringEntity(jsonObject.toString());
        se.setContentType("application/json");
        se.setContentEncoding("UTF-8");
        //默认的重试策略
//        request.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler());
//        request.getParams().setParameter(HttpMethodParams.SO_TIMEOUT, 5000);//设置超时时间
        request.setEntity(se);
        // send request
        CloseableHttpResponse response = client.execute(request);
        // read rsp code
        log.info("rsp code:" + response.getStatusLine().getStatusCode());
        // return content
        String ret = readResponseContent(response.getEntity().getContent());
        log.info("rsp content:" + ret);
        response.close();
        return ret;
    }

    private static String readResponseContent(InputStream inputStream) throws Exception {
        if (inputStream == null) {
            return "";
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buf = new byte[512];
        int len;
        while (inputStream.available() > 0) {
            len = inputStream.read(buf);
            out.write(buf, 0, len);
        }

        return out.toString();
    }

    /**
     * MultipartFile文件上传
     *
     * @param file
     * @param url
     * @return
     */
    public static Map doPostFile(MultipartFile file, String url, Map<String, String> params)  {
        //创建http客户端
        CloseableHttpClient httpClient= HttpClientBuilder.create().build();
        //接收数据并返回
        Map<String,Object> userMap=new HashMap<>();
        String resultJson=null;
        //发送post请求传入地址
        HttpPost post = new HttpPost(url);
        //post.addHeader("token", "");//TokenContext.getToken()
        try {
            //上传文件 别的格式参考 httpEntity
            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            //参数分别为
            // files: 入参的键,
            // file.getInputStream(): 字节输入流
            // ContentType.MULTIPART_FORM_DATA: 设置内容类型
            //file.getOriginalFilename(): 文件名称
            builder.setCharset(Charset.forName("UTF-8")).addBinaryBody
                    ("file", file.getInputStream(), ContentType.MULTIPART_FORM_DATA, file.getOriginalFilename());
            //遍历params
            for (Map.Entry<String, String> entry : params.entrySet()) {
                builder.addTextBody(entry.getKey(), entry.getValue());
            }
            HttpEntity entity=builder.build();
            post.setEntity(entity);
            HttpResponse res = httpClient.execute(post);
            if (res.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                // 返回json格式
                resultJson = EntityUtils.toString(res.getEntity());
                //JSONandMap 下面封装的JSON 转 map对象
                userMap = JSONandMap(resultJson);
                log.info(userMap.toString());
            }
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return  userMap;
    }

    //json转map
    public static Map JSONandMap(String param){
        Map<String,Object> userMap=new HashMap<>();
        JSONObject response = JSONObject.parseObject(param);
        for (Map.Entry<String, Object> entry : response.entrySet()) {
            userMap.put(entry.getKey(), entry.getValue());
        }
        return userMap;
    }

    /*public static String readJsonFile(String path){
        File file = new File(path);
        String jsonStr = "";
        FileInputStream fis = null;
        InputStreamReader isr = null;
        BufferedReader br = null;
        try {
            fis = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        try {
            isr = new InputStreamReader(fis, "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        br = new BufferedReader(isr);
        String line = null;
        try {
            while ((line = br.readLine()) != null) {
                jsonStr += line;
            }
        } catch (IOException e) {
            log.error("readJsonFile error", e);
        }
        return jsonStr;
    }*/

    public static String readMultipartFile(MultipartFile file){
        String fileStr = "";
        FileInputStream fis = null;
        InputStreamReader isr = null;
        BufferedReader br = null;

        try {
            isr = new InputStreamReader(file.getInputStream(), "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        br = new BufferedReader(isr);
        String line = null;
        try {
            while ((line = br.readLine()) != null) {
                fileStr += line;
            }
        } catch (IOException e) {
            log.error("readJsonFile error", e);
        }
        return fileStr;
    }
}
