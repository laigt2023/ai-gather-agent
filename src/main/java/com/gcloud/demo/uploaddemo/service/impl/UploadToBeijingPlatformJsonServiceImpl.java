package com.gcloud.demo.uploaddemo.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.gcloud.demo.uploaddemo.cache.TaskInfosCache;
import com.gcloud.demo.uploaddemo.model.BeijingEventUploadVo;
import com.gcloud.demo.uploaddemo.model.EventInfo;
import com.gcloud.demo.uploaddemo.params.FaceFileParams;
import com.gcloud.demo.uploaddemo.params.UploaddemoParams;
import com.gcloud.demo.uploaddemo.service.IUploadToBeijingPlatformService;
import com.gcloud.demo.uploaddemo.util.HttpClientUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.commons.CommonsMultipartFile;

import javax.annotation.Resource;
import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import java.awt.image.BufferedImage;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
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
    @Value("${gcloud.beijing.ai-face-url}")
    private String aiFaceUrl;

    @Value("${gcloud.save-dir}")
    private String SAVE_DIR;

    private String projectName = "";

    @Value("${gcloud.upload-after-del}")
    private Boolean UPLOAD_AFTER_DEL;
    @Value("${gcloud.is-post-event}")
    private Boolean IS_POST_EVENT;

    @Value("${gcloud.beijing.face_max_offset}")
    private int FACE_MAX_OFFSET = 200;

    @Resource
    private HttpServletRequest CURRENT_REQUEST;
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
        if( picFile != null ){
            EventInfo eventInfo;
            String skillType = null;
            // 空的时候找本地文件
            if(jsonFile == null){
                String jsonFileName = picFile.getOriginalFilename().replace(".jpeg",".json");
                File localJsonFile= new File(getTodayFolderName() + File.separator + jsonFileName);

                FileItem fileItem = getMultipartFile(localJsonFile, jsonFileName);
                MultipartFile multipartFile = new CommonsMultipartFile(fileItem);
                eventInfo = JSON.parseObject(HttpClientUtil.readMultipartFile(multipartFile), EventInfo.class);

            }else{
                // 不为空的时候直接转换
                eventInfo = JSON.parseObject(HttpClientUtil.readMultipartFile(jsonFile), EventInfo.class);
            }

            try {
                   // 根据配置是否上报事件信息
                   if(IS_POST_EVENT!=null && IS_POST_EVENT.booleanValue() == true ){
                       // 根据配置是否上报事件信息

                       String taskIp = getIpAddr(CURRENT_REQUEST);
                       JSONObject data = TaskInfosCache.getTaskInfo(taskIp,eventInfo.getTask_id());

                       // 摄像头地址
                       String cameraUrl = data.getJSONObject("source").getString("origin");
                       // 数据源名称
                       String videoName = data.getJSONObject("source").getString("name");

                       // 根基摄像头地址获取下来上报数据
                       // 通道号：获取流地址里的最后一个/和 01 或 02 中间的数字：
                       // 例如：...Channels/3902，获取到的通道号是 39。...Channels/601，获取到的通道号是 6
                       String[] strArray = cameraUrl.split("/");
                       String cameraName = strArray[strArray.length-1];
                       cameraName = cameraName.substring(0, cameraName.length()-2);

                       System.out.println("固定摄像头地址: (" + videoName + " ）" + cameraUrl);
                       // 上报事件信息
                       BeijingEventUploadVo paramsVo = new BeijingEventUploadVo();

                       // application.yml中的配置：事件对应的视频推理类型  （app_id:videoType）事件类型：0-安全帽监测、1-反光衣监测、15-安全帽+人脸识别、16-反光衣+人脸识别
                       // 6-吸烟 7-人员聚集 17-电子围栏 9-车辆违停 11-睡岗 14-人脸（抓拍/识别）
                       skillType = postEventTypeMap.get(eventInfo.getApp_id());

                       // 上报事件图片转base64格式
                       String picFileName = picFile.getOriginalFilename();
                       String jpgFilePath = getTodayFolderName() + File.separator + picFileName.replace(".jpeg",".jpg");
                       // 图片转base64
                       String img_base64 = ImageToBase64(jpgFilePath);


                       // 限制只上报配置中的任务事件
                       if(postEventTypeMap.containsKey(eventInfo.getApp_id())){
                           JSONObject paramsJson;
                           // 上报事件格式封装
                           paramsVo.setEventType(new Integer(skillType));
                           paramsVo.setVideoName(videoName);
                           paramsVo.setCameraName(cameraName);
                           paramsJson = JSONObject.parseObject(JSONObject.toJSON(paramsVo).toString());

                           // 14为人脸考勤单独处理上报数据
                           // 异常事件infos 不为空，只有人脸信息封装时，因为匹配精准度原因会出现空数组
                           if(new Integer(skillType).intValue() == 14 ){
                               JSONObject faceParamsJson = new JSONObject();
                               faceParamsJson.put("base64_code",img_base64);
                               String responseStr = HttpClientUtil.sendPostJson(aiFaceUrl,faceParamsJson);
                               JSONObject responseFaceJson = JSONObject.parseObject(responseStr);
                               log.info("人脸识别请求信息：()"+ aiFaceUrl+"{}", responseStr);
                               JSONArray faceArray = responseFaceJson.getJSONObject("data").getJSONArray("face-info");
                               JSONArray face_monitor_infos = new JSONArray();
                               // 人脸识别不为空时，上报事件
                               if(!faceArray.isEmpty()){
                                   // 封装人脸信息
                                   face_monitor_infos = getFaceInfoFormat(faceArray);
                                   // 将人脸识别成果的图片存储起来
                                   copyFaceFile(getTodayFolderName(),picFileName,faceArray);
                               }

                               if (!face_monitor_infos.isEmpty()) {
                                   reportSendHttpAndLog(paramsJson,face_monitor_infos,img_base64);
                               } else {
                                   String logStr = paramsJson.toJSONString();
                                   log.info("暂无法匹配相关人脸信息，暂不上报事件：()" + eventInfo.getApp_id());
                                   log.info("未上报事件数据：" + eventUploadUrl + "{}", jpgFilePath, logStr);
                               }

                           // skillType = 15 || 16 时，没有识别到人脸的事件归类到 普通事件监控中，0-安全帽监测、1-反光衣监测、15-安全帽+人脸识别、16-反光衣+人脸识别
                           }else if (new Integer(skillType).intValue() == 15 || new Integer(skillType).intValue() == 16 ){

                               // 异常事件相关的人员进行人脸识别与身份匹配
                               JSONObject faceParamsJson = new JSONObject();
                               faceParamsJson.put("base64_code",img_base64);
                               String responseStr = HttpClientUtil.sendPostJson(aiFaceUrl,faceParamsJson);
                               JSONObject responseFaceJson = JSONObject.parseObject(responseStr);
                               log.info("人脸识别事件信息：()"+ aiFaceUrl+"{}", responseStr);
                               JSONArray faceArray = responseFaceJson.getJSONObject("data").getJSONArray("face-info");
                               System.out.println(responseStr);

                               // 异常事件并进行人脸匹配
                               JSONArray event_info_list = getInfoListByFaces(eventInfo,faceArray);
                               JSONArray has_face_infos = new JSONArray();
                               JSONArray not_face_infos = new JSONArray();
                               for (int i =0;i<event_info_list.size();i++) {
                                   JSONObject e = event_info_list.getJSONObject(i);

                                   if(e.getString("cardId")!=null && !e.getString("cardId").equals("")){
                                       has_face_infos.add(e);
                                   }else{
                                       not_face_infos.add(e);
                                   }
                               }

                               // 分批上报【人脸匹配】成功的事件
                               if ( !has_face_infos.isEmpty() ) {
                                   reportSendHttpAndLog(paramsJson,not_face_infos,img_base64);
                               }

                               // 分批上报【人脸匹配】不成功的事件，作为普通事件监控格式上报
                               if ( !not_face_infos.isEmpty() ) {
                                   // eventType： 0-安全帽监测、15-安全帽+人脸识别   设置为安全帽普通监控事件
                                   if (new Integer(skillType).intValue() == 15) {
                                       paramsJson.put("eventType", 0);
                                   }

                                   // eventType： 1-反光衣监测、16-反光衣+人脸识别 设置为反光衣普通监控事件
                                   if (new Integer(skillType).intValue() == 16) {
                                       paramsJson.put("eventType", 1);
                                   }
                                   reportSendHttpAndLog(paramsJson,not_face_infos,img_base64);
                               }

                           }else{// 异常事件监控事件上报

                               JSONArray common_event_infos = getInfoList(eventInfo);
                               reportSendHttpAndLog(paramsJson,common_event_infos,img_base64);
                           }




                       }else{
                            log.info("未配置上报事件信息：()"+ eventInfo.getApp_id());
                       }


                   }
                   // 保存文件
                   saveAllFile(params);
                   // 如果配置了上传后删除，则删除, 或者为 14 - 人脸识别 则默认删除多余告警图片
                   if((UPLOAD_AFTER_DEL && UPLOAD_AFTER_DEL.booleanValue()) || (skillType !=null && new Integer(skillType).intValue() == 14) ){
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
     *  发送http事件上报请求，并记录返回日志
     * @param paramsJson  params请求参数
     * @param infos       人脸信息数组/普通事件数组
     * @param img_base64  事件图片base64编码
     * @throws Exception
     */
    private void reportSendHttpAndLog(JSONObject paramsJson,JSONArray infos,String img_base64) throws Exception {
        paramsJson.put("info", infos);
        paramsJson.put("alarmPicture", "");
        String logStr = JSON.toJSON(paramsJson).toString();
        // 上报时带上事件图片
        paramsJson.put("alarmPicture", img_base64);
        // 日志记录不带img_base64图片信息，减清日志文件大小
        log.info("上报事件信息：()" + eventUploadUrl + "{}", logStr);
        String ret = HttpClientUtil.sendPostJson(eventUploadUrl, paramsJson);
        log.info("上报事件SUCCESS：()" + eventUploadUrl + "{}", ret);
    }

    @Override
    public JSONObject faceComparison(FaceFileParams params) {
        JSONObject faceParamsJson = new JSONObject();
        try {
            if(params == null || params.getFile() == null){
                throw new Exception("file is nul :: 图片参数不能为空");
            }

            byte[] bytes =  params.getFile().getBytes();

            String img_base64 = Base64.getEncoder().encodeToString(bytes);
            faceParamsJson.put("base64_code",img_base64);

            if(params.getSim() != null){
                faceParamsJson.put("sim",params.getSim());
            }

            String responseStr = HttpClientUtil.sendPostJson(aiFaceUrl,faceParamsJson);
            log.info("人脸识别事件信息：()"+ aiFaceUrl+"{}", responseStr);
            return JSONObject.parseObject(responseStr);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (Exception e) {
            throw new RuntimeException(e);
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

    /**
     * 获取异常事件信息并匹配人员人脸信息
     * @param eventInfo 事件信息
     * @param faceArray 人脸识别信息JSON数组
     * @return 根据坐标匹配事件人员
     */
    private JSONArray getInfoListByFaces(EventInfo eventInfo,JSONArray faceArray){
        JSONArray result = new JSONArray();

        JSONArray array = JSONObject.parseArray(eventInfo.getDetails());
        if(array == null || array.isEmpty()){
            log.error("上报事件信息失败，"+ eventInfo.getEvent_id() +"的details为空");
        }

        JSONArray targets = array.getJSONObject(0).getJSONArray("targets");
        if(array == null || array.isEmpty()){
            log.error("上报事件信息失败，"+ eventInfo.getEvent_id()  +"的targets为空");
        }

        if(faceArray ==null || faceArray.isEmpty()){
            log.error("上报事件信息失败::无人脸身份匹配信息，"+ eventInfo.getEvent_id());
        }
        // 遍历eventInfo 输出异常事件对象列表
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

            String cardId = "";
            // 寻找坐标最近的一个faceID
            item.put("cardId",cardId);
            result.add(item);
        }

        // 人脸信息匹配到最佳的事件上
        // 遍历人脸，将人脸信息尽量匹配到异常事件上
        for (int i =0;i<faceArray.size();i++) {
            JSONObject face = faceArray.getJSONObject(i);
            JSONArray box = face.getJSONArray("box");

            int faceMiddleX = new Integer((box.getInteger(0) + box.getInteger(2)) / 2);
            int faceMinY  = box.getInteger(1);

            int current_face_match_index = -1;
            int min_face_event_position = -1;
            // 遍历异常事件列表，匹配与当前人脸信息相对位置最近的对象
            for (int k =0;k<result.size();k++) {
                // 获取每一个异常事件
                JSONObject eventJsonInfo = result.getJSONObject(k);
                String[] event_info_array = eventJsonInfo.getString("coordinate").split(",");
                if(event_info_array !=null && event_info_array.length >= 4){
                    int eventMiddleX = new Integer((new Integer(event_info_array[0]) + new Integer(event_info_array[2])) / 2);
                    int eventMinY = new Integer(event_info_array[1]);

                    // 筛选出位置最接近的event信息，并记录下标
                    int absX = Math.abs((eventMiddleX - faceMiddleX));
                    int absY = Math.abs((eventMinY - faceMinY));
                    int absXY = absX + absY;
                    if(min_face_event_position < 0 || absXY < min_face_event_position){
                        min_face_event_position = absXY;
                        current_face_match_index = k;
                    }
                }else{
                    log.error("上报事件信息失败，coordinate异常事件坐标新为空："+eventJsonInfo.getString("coordinate") + ":"+ eventInfo.getEvent_id()  +"");
                }
            }
            System.out.println("current_face_match_index:"+current_face_match_index);
            System.out.println("min_face_event_position:"+min_face_event_position);
            // 存在问题： 如果人脸在附近（距离小于FACE_MAX_OFFSET）， 目标事件的人脸有识别  会出现身份赋值错误
            // min_face_event_position 最小距离  current_face_match_index 最佳匹配的人脸下标
            if(min_face_event_position < FACE_MAX_OFFSET && current_face_match_index > 0){
                String cardId = face.getString("name");
                if(face.getString("name").split("_").length > 1){
                    cardId = face.getString("name").split("_")[1];
                }

                result.getJSONObject(current_face_match_index).put("cardId",cardId);
                result.getJSONObject(current_face_match_index).put("faceBox",box.getInteger(0)+","+box.getInteger(1)+","+box.getInteger(2)+","+box.getInteger(3));
            }

        }
        // cardId为空的，证明人脸匹配失败
        return result;
    }

    /**
     * 获取异常事件信息
     * @param eventInfo 事件信息
     * @return 根据坐标匹配事件人员
     */
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

    // 将识别到face的图片保存到./face目录下
    private static void copyFaceFile(String dir,String fileName,JSONArray faceArray){
        String faceDir = dir + File.separator + "face" ;

        if(!new File(faceDir).exists()){
            new File(faceDir).mkdir();
        }

        String jpgFilePath = dir + File.separator + fileName.replace(".jpeg",".jpg");
        // 复制到face目录下
        String faceFilePath = faceDir + File.separator + fileName.replace(".jpeg",".jpg");
        // 保存人脸识别数据
        String josnFilePath = faceDir + File.separator + fileName.replace(".jpeg",".json");
        try {
            copyFileUsingStream(new File(jpgFilePath),new File(faceFilePath));
        } catch (IOException e) {
            log.info("人脸图片保存失败（" + jpgFilePath + ")");
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(josnFilePath))) {
            writer.write(faceArray.toJSONString());
        } catch (IOException e) {
            log.info("人脸信息保存失败（" + josnFilePath + ")");
        }
    }
    // 文件复制
    private static void copyFileUsingStream(File source, File dest) throws IOException {
        InputStream is = null;
        OutputStream os = null;
        try {
            is = new FileInputStream(source);
            os = new FileOutputStream(dest);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = is.read(buffer)) > 0) {
                os.write(buffer, 0, length);
            }
        } finally {
            is.close();
            os.close();
        }
    }

    /**
     * 人脸信息格式
     * @param faceArray 人脸识别信息JSON数组
     * @return 封装为上报接口的数据结构
     */
    private JSONArray getFaceInfoFormat(JSONArray faceArray){
        JSONArray result = new JSONArray();
        String label = "face";
        String color = "#FF0000";

        for (int i =0;i<faceArray.size();i++){
            JSONObject t = faceArray.getJSONObject(i);
            String prob = t.getString("sim");
            String cardId = "";
            if(t.getString("name").split("_").length > 1){
                cardId = t.getString("name").split("_")[1];
            }

            JSONArray box = t.getJSONArray("box");
            int minX = (int) Math.floor(box.getFloat(0));
            int minY = (int) Math.floor(box.getFloat(1));
            int maxX = (int) Math.floor(box.getFloat(2));
            int maxY = (int) Math.floor(box.getFloat(3));

            JSONObject item = new JSONObject();
            item.put("coordinate",minX+","+minY+","+maxX+","+maxY);
            item.put("label",label);
            item.put("color",color);
            item.put("confidence",prob);
            item.put("cardId",cardId);

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

    /**
     * 获取请求方的真实IP
     * @param request
     * @return
     */
    public String getIpAddr(HttpServletRequest request) {
        String ip = request.getHeader("x-forwarded-for");
        if(ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if(ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if(ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
//    public static MultipartFile convert(File file) throws IOException {
//        DiskFileItem fileItem = new DiskFileItem("file",
//                Files.probeContentType(file.toPath()), false,
//                file.getName(), (int) file.length(), file.getParentFile());
//        IoUtil.copy(Files.newInputStream(file.toPath()), fileItem.getOutputStream());
//        return new CommonsMultipartFile(fileItem);
//    }
}
