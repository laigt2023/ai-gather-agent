package com.gcloud.demo.uploaddemo.service.impl;

import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.gcloud.demo.uploaddemo.model.BeijingEventUploadVo;
import com.gcloud.demo.uploaddemo.model.EventInfo;
import com.gcloud.demo.uploaddemo.params.UploaddemoParams;
import com.gcloud.demo.uploaddemo.service.IUploadToBeijingPlatformService;
import com.gcloud.demo.uploaddemo.service.IUploadToThirdPartyPlatformService;
import com.gcloud.demo.uploaddemo.util.HttpClientUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.commons.CommonsMultipartFile;
import sun.misc.BASE64Encoder;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

//   北京工地上报方案实现
@Primary
@Slf4j
@Service
public class UploadToBeijingPlatformJsonServiceImpl implements IUploadToBeijingPlatformService{
    @Value("#{${gcloud.beijing.event-Type}}")
    private Map<String,String> postEventTypeMap;
    @Value("${gcloud.beijing.event-upload-url}")
    private String eventUploadUrl;
    @Value("${gcloud.gddi.api-url}")
    private String gddiApiUrl;
    @Value("${gcloud.gddi.api-token}")
    private String gddiApiToken;
    @Value("${gcloud.gddi.username}")
    private String gddiUsername;
    @Value("${gcloud.gddi.password}")
    private String gddiPassword;

    @Value("${gcloud.save-dir}")
    private String SAVE_DIR;

    private String projectName = "";

    @Value("${gcloud.upload-after-del}")
    private Boolean UPLOAD_AFTER_DEL;
    @Value("${gcloud.is-post-event}")
    private Boolean IS_POST_EVENT;

    @Override
    public void upload(UploaddemoParams params) {
        MultipartFile picFile = null;
        MultipartFile jsonFile = null;

        if(!StringUtils.isEmpty(params.getAppKeyID())){
            projectName = params.getAppKeyID();
        }

        for (int i = 0; i < params.getFile().length; i++) {
            log.info("接收到文件名：{}",params.getFile()[i].getOriginalFilename());
            String fileName = params.getFile()[i].getOriginalFilename();

            if(fileName != null && fileName.toLowerCase().endsWith(".json")){
                jsonFile = params.getFile()[i];
                // 保存json文件
                saveAllFile(params);
            }else if(fileName != null && fileName.toLowerCase().endsWith(".jpg")){
//                picFile = params.getFile()[i];
                // 保存jpg文件
                saveAllFile(params);
            }else if(fileName != null && fileName.toLowerCase().endsWith(".jpeg")){
                picFile = params.getFile()[i];
                // 延后保存
            }
        }
        //  上传顺序是jpg,json,jpeg
        if( picFile !=null ){
            Map<String,String> paramMap = new HashMap<String,String>();
            EventInfo eventInfo = null;
            // 空的时候找本地文件
            if(jsonFile == null){
                String jsonFileName = picFile.getOriginalFilename().replace(".jpeg",".json");
                File localJsonFile= new File(getTodayFolderName() + File.separator + jsonFileName);

                FileItem fileItem = getMultipartFile(localJsonFile, jsonFileName);
                MultipartFile multipartFile = new CommonsMultipartFile(fileItem);
                eventInfo = JSON.parseObject(HttpClientUtil.readMultipartFile(multipartFile), EventInfo.class);

//                File localFile= new File(getTodayFolderName() + File.separator + picFileName);
//                FileItem fileItem = getMultipartFile(localFile, picFileName);
//                MultipartFile multipartFile = new CommonsMultipartFile(fileItem);
//                if(localFile.exists()){
//                    picFile = multipartFile;
//                }
//                picFile = multipartFile.transferTo(f);
                /* file 转 multipartFile */
            }else{
                // 不为空的时候直接转换
                eventInfo = JSON.parseObject(HttpClientUtil.readMultipartFile(jsonFile), EventInfo.class);
            }

            try {
                   // 根据配置是否上报事件信息
                   if(IS_POST_EVENT!=null && IS_POST_EVENT.booleanValue() == true ){
                       // 根据配置是否上报事件信息

                       JSONArray infos = getInfoList(eventInfo);
                       CloseableHttpClient client = HttpClientBuilder.create().build();
                       HttpGet getRequest = new HttpGet(gddiApiUrl+"tasks/"+eventInfo.getTask_id());

                       BASE64Encoder encoder = new BASE64Encoder();
                       JSONObject jsonObject = new JSONObject();

                       // 设置token
                       getRequest.setHeader("Authentication", gddiApiToken);
                       // send request
                       CloseableHttpResponse response = client.execute(getRequest);

                       HttpEntity entity = response.getEntity();
                       String jsonString = EntityUtils.toString(entity);
                       JSONObject json = JSONObject.parseObject(jsonString);
                       if(json.get("message")!=null && json.get("message").toString().length()>0){
                           System.out.println("获取任务信息失败:("+ getRequest.getURI() +")");
                           log.info("获取任务信息失败:("+ getRequest.getURI() +") ");
                           return;
                       }
                       System.out.println(gddiApiUrl+"/tasks/"+eventInfo.getTask_id());
                       JSONObject data = json.getJSONObject("data");
                       String cameraUrl = data.getJSONObject("source").getString("origin");
                       String videoName = data.getJSONObject("source").getString("name");

                       //通道号：获取流地址里的最后一个/和 01 或 02 中间的数字：例如：...Channels/3902，获取到的通道号是 39。...Channels/601，获取到的通道号是 6
                       String[] strArray = cameraUrl.split("/");
                       String cameraName = strArray[strArray.length-1];
                       cameraName = cameraName.substring(0, cameraName.length()-2);

                       System.out.println("固定摄像头地址: (" + videoName + " ）" + cameraUrl);
                       // 上报事件信息
                       BeijingEventUploadVo paramsVo = new BeijingEventUploadVo();



                       String skillType = postEventTypeMap.get(eventInfo.getApp_id());

                       if(postEventTypeMap.containsKey(eventInfo.getApp_id())){
                           String picFileName = picFile.getOriginalFilename();
                           String jpgFilePath = getTodayFolderName() + File.separator + picFileName.replace(".jpeg",".jpg");

                           paramsVo.setEventType(new Integer(skillType));
                           paramsVo.setVideoName(videoName);
                           paramsVo.setCameraName(cameraName);
                           paramsVo.setAlarmPicture(ImageToBase64(jpgFilePath));


                           JSONObject paramsJson = JSONObject.parseObject(JSONObject.toJSON(paramsVo).toString());
                           paramsJson.put("info",infos);

                           JSONObject logJson = JSONObject.parseObject(JSONObject.toJSON(paramsVo).toString());
                           logJson.put("info",infos);
                           logJson.remove("alarmPicture");

                           log.info("上报事件信息：()"+ eventUploadUrl+"{}", JSON.toJSON(logJson).toString());
                           HttpClientUtil.sendPostJson(eventUploadUrl,paramsJson);
                       }else{
                            log.info("未配置上报事件信息：()"+ eventInfo.getApp_id());
                       }


                   }
                   // 保存文件
                   saveAllFile(params);
                   // 如果配置了上传后删除，则删除
                   if(UPLOAD_AFTER_DEL && UPLOAD_AFTER_DEL.booleanValue()){
                       // 删除文件 .json .jpg .jpeg
                       String picFileName = picFile.getOriginalFilename();
                       File jpegF = new File(getTodayFolderName() + File.separator + picFile.getOriginalFilename());
                       File jpgF = new File(getTodayFolderName() + File.separator + picFileName.replace(".jpeg",".jpg"));
                       File jsonF = new File(getTodayFolderName() + File.separator + picFileName.replace(".jpeg",".json"));
                       if(jsonF.exists()){
                           jsonF.delete();
                       }
                       if(jpgF.exists()){
                           jpgF.delete();
                       }
                       if(jpegF.exists()){
                           jpegF.delete();
                       }
                   }

            } catch (Exception e) {
                log.error("上报事件信息失败，" + picFile.getOriginalFilename() + e.getMessage());
            }
        }
    }


    /**
     * 保存params中的所有文件
     * @param params
     */
    private void saveAllFile(UploaddemoParams params){
        String saveDir = getTodayFolderName();
        /* 判断目录是否存在，不存在则创建 */
        File directory = new File(saveDir);
        if (!directory.exists()) {
            directory.mkdirs();
        }

        // 保存文件到本地  - 循环打印file数组
        for (int i = 0; i < params.getFile().length; i++) {
            log.info(params.getFile()[i].getOriginalFilename());
            //把文件保存在本地
            try {
                params.getFile()[i].transferTo(new File(saveDir + File.separator + params.getFile()[i].getOriginalFilename()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    private String ImageToBase64 (String filePath) {
        String base64Image = "";
        try {
            BufferedImage image = ImageIO.read(new File(filePath));
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, "jpg", baos);
            byte[] imageBytes = baos.toByteArray();

            base64Image = Base64.getEncoder().encodeToString(imageBytes);

        } catch (IOException e) {
            e.printStackTrace();
        }
        return  base64Image;
    }


    private JSONArray getInfoList(EventInfo eventInfo){
        JSONArray result = new JSONArray();

        JSONArray array = JSONObject.parseArray(eventInfo.getDetails());
        if(array == null || array.isEmpty()){
            log.error("上报事件信息失败，"+ eventInfo.getEvent_id() +"的details为空");
        }

        JSONArray targets = array.getJSONObject(0).getJSONArray("targets");
        if(array == null || array.isEmpty()){
            log.error("上报事件信息失败，"+ eventInfo.getEvent_id()  +"的targets为空");
        }

        for (int i =0;i<targets.size();i++){
            JSONObject t = targets.getJSONObject(i);
            String prob = t.getString("prob");
            String label = t.getString("label");
            JSONArray color = t.getJSONArray("color");
            JSONObject box = t.getJSONObject("box");
            int minX = (int) Math.floor(box.getDouble("left_top_x"));
            int minY = (int) Math.floor(box.getDouble("left_top_y"));
            int maxX = (int) Math.floor(box.getDouble("right_bottom_x"));
            int maxY = (int) Math.floor(box.getDouble("right_bottom_y"));

            JSONObject item = new JSONObject();
            item.put("coordinate",minX+","+minY+","+maxX+","+maxY);
            item.put("label",label);
            item.put("color",rgbToHex(color.getInteger(0),color.getInteger(1),color.getInteger(2)));
            item.put("confidence",prob);
            item.put("cardId","");

            result.add(item);
        }

        return result;
    }

    public static String rgbToHex(int r, int g, int b) {
        return String.format("#%02x%02x%02x", r, g, b);
    }

    /* 获取今日日期的文件夹名称 */
    public String getTodayFolderName() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

        // 如果配置了项目名称，则按照项目名称创建文件夹
        if(!StringUtils.isEmpty(projectName)){
            return SAVE_DIR + projectName + File.separator + sdf.format(new Date());
        }

        return SAVE_DIR + sdf.format(new Date());
    }
    private FileItem getMultipartFile(File file, String fieldName) {
        FileItemFactory factory = new DiskFileItemFactory(16, null);
        FileItem item = factory.createItem(fieldName, "text/plain", true, file.getName());
        int bytesRead = 0;
        int len = 8192;
        byte[] buffer = new byte[len];
        try {
            FileInputStream fis = new FileInputStream(file);
            OutputStream os = item.getOutputStream();
            while ((bytesRead = fis.read(buffer, 0, len)) != -1) {
                os.write(buffer, 0, bytesRead);
            }
            os.close();
            fis.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return item;
    }

//    public static MultipartFile convert(File file) throws IOException {
//        DiskFileItem fileItem = new DiskFileItem("file",
//                Files.probeContentType(file.toPath()), false,
//                file.getName(), (int) file.length(), file.getParentFile());
//        IoUtil.copy(Files.newInputStream(file.toPath()), fileItem.getOutputStream());
//        return new CommonsMultipartFile(fileItem);
//    }
}
