package com.gcloud.demo.uploaddemo.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.gcloud.demo.uploaddemo.cache.TaskInfosCache;
import com.gcloud.demo.uploaddemo.model.BeijingEventUploadVo;
import com.gcloud.demo.uploaddemo.model.EventInfoVo;
import com.gcloud.demo.uploaddemo.model.FaceItemVo;
import com.gcloud.demo.uploaddemo.model.ReportInfoItemVo;
import com.gcloud.demo.uploaddemo.params.RequestFaceFileParams;
import com.gcloud.demo.uploaddemo.params.RequestFaceImageComparisonParams;
import com.gcloud.demo.uploaddemo.params.RequestUploaddemoParams;
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
import java.util.*;

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

    @Value("#{${gcloud.video-predict-type}}")
    private Map<String,String> predictUrlTypeMap;

    private String BASE64_IMAGE_PRE="data:image/jpeg;base64,";
    @Resource
    private HttpServletRequest CURRENT_REQUEST;
    @Override
    public void upload(RequestUploaddemoParams params) {
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
            EventInfoVo eventInfoVo;
            String skillType = null;
            // 空的时候找本地文件
            if(jsonFile == null){
                String jsonFileName = picFile.getOriginalFilename().replace(".jpeg",".json");
                File localJsonFile= new File(getTodayFolderName() + File.separator + jsonFileName);

                FileItem fileItem = getMultipartFile(localJsonFile, jsonFileName);
                MultipartFile multipartFile = new CommonsMultipartFile(fileItem);
                eventInfoVo = JSON.parseObject(HttpClientUtil.readMultipartFile(multipartFile), EventInfoVo.class);

            }else{
                // 不为空的时候直接转换
                eventInfoVo = JSON.parseObject(HttpClientUtil.readMultipartFile(jsonFile), EventInfoVo.class);
            }

            try {
                   // 根据配置是否上报事件信息
                   if(IS_POST_EVENT!=null && IS_POST_EVENT.booleanValue() == true ){
                       // 根据配置是否上报事件信息

                       String taskIp = getIpAddr(CURRENT_REQUEST);
                       JSONObject data = TaskInfosCache.getTaskInfo(taskIp, eventInfoVo.getTask_id());

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
                       skillType = postEventTypeMap.get(eventInfoVo.getApp_id());

                       // 上报事件图片转base64格式
                       String picFileName = picFile.getOriginalFilename();
                       String jpgFilePath = getTodayFolderName() + File.separator + picFileName.replace(".jpeg",".jpg");
                       // 图片转base64
                       String img_base64 = ImageToBase64(jpgFilePath);


                       // 限制只上报配置中的任务事件
                       if(postEventTypeMap.containsKey(eventInfoVo.getApp_id())){
                           // 上报事件格式封装
                           paramsVo.setEventType(new Integer(skillType));
                           paramsVo.setVideoName(videoName);
                           paramsVo.setCameraName(cameraName);
                           JSONObject reportJson = JSONObject.parseObject(JSONObject.toJSON(paramsVo).toString());

                           // 14为人脸考勤单独处理上报数据
                           // 异常事件infos 不为空，只有人脸信息封装时，因为匹配精准度原因会出现空数组
                           if(new Integer(skillType).intValue() == 14 ){
                               JSONObject faceParamsJson = new JSONObject();
                               faceParamsJson.put("base64_code",img_base64);
                               String responseStr = HttpClientUtil.sendPostJson(aiFaceUrl,faceParamsJson);
                               JSONObject responseFaceJson = JSONObject.parseObject(responseStr);
                               log.info("人脸识别请求信息：()"+ aiFaceUrl+"{}", responseStr);
                               JSONArray faceArray = responseFaceJson.getJSONObject("data").getJSONArray("face-info");
                               List<ReportInfoItemVo> face_monitor_infos = new ArrayList<ReportInfoItemVo>();
                               // 人脸识别不为空时，上报事件
                               if(!faceArray.isEmpty()){
                                   // 封装人脸信息
                                   face_monitor_infos = getFaceInfoFormat(faceArray);
                                   // 将人脸识别成果的图片存储起来
                                   copyFaceFile(getTodayFolderName(),picFileName,faceArray);
                               }

                               if (!face_monitor_infos.isEmpty()) {
                                   reportSendHttpAndLog(reportJson,face_monitor_infos,img_base64);
                               } else {
                                   String logStr = reportJson.toJSONString();
                                   log.info("暂无法匹配相关人脸信息，暂不上报事件：()" + eventInfoVo.getApp_id());
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
                               JSONArray faceJsonArray = responseFaceJson.getJSONObject("data").getJSONArray("face-info");
                               List<FaceItemVo> faceArray = jsonToFaceList(faceJsonArray);
                               System.out.println(responseStr);

                               // 异常事件并进行人脸匹配
                               List<ReportInfoItemVo>  event_info_list = getInfoListMatchFaces(eventInfoVo,faceArray);
                               List<ReportInfoItemVo> has_face_infos = new ArrayList<ReportInfoItemVo>();
                               List<ReportInfoItemVo> not_face_infos = new ArrayList<ReportInfoItemVo>();
                               for (ReportInfoItemVo info : event_info_list) {
                                   if(info.getCardId()  != null && info.getCardId().length() >0){
                                       has_face_infos.add(info);
                                   }else{
                                       not_face_infos.add(info);
                                   }
                               }

                               // 分批上报【人脸匹配】成功的事件
                               if ( !has_face_infos.isEmpty() ) {
                                   reportSendHttpAndLog(reportJson,has_face_infos,img_base64);
                               }

                               // 分批上报【人脸匹配】不成功的事件，作为普通事件监控格式上报
                               if ( !not_face_infos.isEmpty() ) {
                                   // eventType： 0-安全帽监测、15-安全帽+人脸识别   设置为安全帽普通监控事件
                                   if (new Integer(skillType).intValue() == 15) {
                                       reportJson.put("eventType", 0);
                                   }

                                   // eventType： 1-反光衣监测、16-反光衣+人脸识别 设置为反光衣普通监控事件
                                   if (new Integer(skillType).intValue() == 16) {
                                       reportJson.put("eventType", 1);
                                   }
                                   reportSendHttpAndLog(reportJson,not_face_infos,img_base64);
                               }

                           }else{// 普通异常事件监控上报

                               List<ReportInfoItemVo> common_event_infos = getCommonInfoList(eventInfoVo);
                               reportSendHttpAndLog(reportJson,common_event_infos,img_base64);
                           }
                       }else{
                            log.info("未配置上报事件信息：()"+ eventInfoVo.getApp_id());
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
     * @param reportJson  params请求参数
     * @param list       人脸信息数组/普通事件数组 List<ReportInfoItem>
     * @param img_base64  事件图片base64编码
     * @throws Exception
     */
    private void reportSendHttpAndLog(JSONObject reportJson, List<ReportInfoItemVo> list, String img_base64) throws Exception {
        if(list != null && !list.isEmpty()) {
            JSONArray infos = JSONArray.parseArray(JSON.toJSONString(list));
            reportSendHttpAndLog(reportJson, infos, img_base64);
        }
    }

    /**
     *  发送http事件上报请求，并记录返回日志
     * @param reportJson  params请求参数
     * @param infos       人脸信息数组/普通事件数组
     * @param img_base64  事件图片base64编码
     * @throws Exception
     */
    private void reportSendHttpAndLog(JSONObject reportJson,JSONArray infos,String img_base64) throws Exception {
        if(infos != null && !infos.isEmpty()){
            reportJson.put("info", infos);
            reportJson.put("alarmPicture", "");
            String logStr = JSON.toJSON(reportJson).toString();
            // 上报时带上事件图片
            reportJson.put("alarmPicture", img_base64);
            // 日志记录不带img_base64图片信息，减清日志文件大小
            log.info("上报事件信息：()" + eventUploadUrl + "{}", logStr);
            String ret = HttpClientUtil.sendPostJson(eventUploadUrl, reportJson);
            log.info("上报事件SUCCESS：()" + eventUploadUrl + "{}", ret);
        }
    }

    @Override
    public JSONObject faceComparison(RequestFaceFileParams params) {
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
     * 人脸图片匹配，先进行异常事件识别根据params.eventType,再根据返回信息进行人脸识别
     * @param params
     * @return
     */
    @Override
    public JSONObject faceImageComparisonTask(RequestFaceImageComparisonParams params) {
        JSONObject result = new JSONObject();
        String imagePredictUrl = null;
        // params.eventType 事件类型：0-安全帽监测、1-反光衣监测、15-安全帽+人脸识别、16-反光衣+人脸识别
        // 6-吸烟 7-人员聚集 17-电子围栏 9-车辆违停 11-睡岗 14-人脸（抓拍/识别）
        if(params.getEventType() == null || params.getEventType().equals("")){
            String message ="请前方IP( " + getIpAddr(CURRENT_REQUEST) + " ) : eventType 事件类型参赛不能为空";
            result.put("message",message);
            log.error(message);
            return result;
        }

        if(params.getEventType().intValue() == 14){
            imagePredictUrl = predictUrlTypeMap.get("face");
        }

        if(params.getEventType().intValue() == 15){
            imagePredictUrl = predictUrlTypeMap.get("helmet");
        }

        if(params.getEventType().intValue() == 16){
            imagePredictUrl = predictUrlTypeMap.get("vest");
        }

        // 上报事件参数
        JSONObject reportJson = new JSONObject();
        reportJson.put("siteId",params.getSiteID());
        reportJson.put("type",params.getType());
        reportJson.put("videoName",params.getVideoName());
        reportJson.put("eventType",params.getEventType());
        reportJson.put("cameraName",params.getCameraName());
        reportJson.put("alarmDate",params.getAlarmDate());

        // 获取当前时间的时间戳与日期
        Long alarmTime = System.currentTimeMillis();
        if(params.getAlarmDate()==null || params.getAlarmDate().length() ==0){
            String alarmDate = new SimpleDateFormat("yyyy-MM-dd").format(alarmTime);
            reportJson.put("alarmDate", alarmDate);
        }

        reportJson.put("alarmTime", alarmTime);

        if(imagePredictUrl != null){
            JSONObject eventParamsJson = new JSONObject();
            // 补充图片base64数据格式前缀
            eventParamsJson.put("image",BASE64_IMAGE_PRE + params.getAlarmPicture());
            try {
                String responseStr = HttpClientUtil.sendPostJson(imagePredictUrl,eventParamsJson);
                JSONObject responseEventJson = JSONObject.parseObject(responseStr);

                if(responseEventJson.getString("message") != null && responseEventJson.getString("message").length() > 0){
                    log.error("当前图片识别服务请求异常，请求服务url（"+ imagePredictUrl  +"）：" + responseEventJson.getString("message"));
                    return null;
                }
                JSONObject imageEventInfo = responseEventJson.getJSONObject("data");
                JSONArray eventArray = responseEventJson.getJSONObject("data").getJSONArray("targets");

                if(eventArray == null || eventArray.isEmpty()){
                    log.info("当前图片为识别未异常事件，请求图片识别服务url（"+ imagePredictUrl  +"）： targets为空");
                    return null;
                }

                // 人脸考勤
                if(params.getEventType().intValue() == 14){
                    // 调用人脸识别服务，获取图片中的所有人脸信息
                    JSONObject faceParamsJson = new JSONObject();
                    faceParamsJson.put("base64_code",params.getAlarmPicture());
                    String responseFaceStr = HttpClientUtil.sendPostJson(aiFaceUrl,faceParamsJson);
                    JSONObject responseFaceJson = JSONObject.parseObject(responseFaceStr);
                    log.info("人脸识别请求信息：()"+ aiFaceUrl+"{}", responseStr);
                    JSONArray faceArray = responseFaceJson.getJSONObject("data").getJSONArray("face-info");
                    List<ReportInfoItemVo> face_monitor_infos = new ArrayList<ReportInfoItemVo>();
                    // 人脸识别不为空时，上报事件
                    if(!faceArray.isEmpty()){
                        // 封装人脸信息
                        face_monitor_infos = getFaceInfoFormat(faceArray);
                        // 人脸考勤数据上报
                        reportSendHttpAndLog(reportJson,face_monitor_infos,params.getAlarmPicture());
                    }

                }else if (params.getEventType().intValue() == 15 || params.getEventType().intValue() == 16 ){
                    String img_base64 = params.getAlarmPicture();

                    // 异常事件相关的人员进行人脸识别与身份匹配
                    JSONObject faceParamsJson = new JSONObject();
                    faceParamsJson.put("base64_code",img_base64);
                    String responseFaceStr = HttpClientUtil.sendPostJson(aiFaceUrl,faceParamsJson);
                    JSONObject responseFaceJson = JSONObject.parseObject(responseFaceStr);
                    log.info("人脸识别事件信息：()"+ aiFaceUrl+"{}", responseFaceJson);
                    JSONArray faceJsonArray = responseFaceJson.getJSONObject("data").getJSONArray("face-info");
                    List<FaceItemVo> faceArray = jsonToFaceList(faceJsonArray);

                    // 异常事件并进行人脸匹配
                    List<ReportInfoItemVo> event_info_list = imageEventInfoMatchFaces(imageEventInfo,faceArray);

                    // 进行事件分类：按照有无识别到人脸信息
                    List<ReportInfoItemVo> has_face_infos = new ArrayList<ReportInfoItemVo>();
                    List<ReportInfoItemVo> not_face_infos = new ArrayList<ReportInfoItemVo>();
                    for (ReportInfoItemVo info : event_info_list) {
                        if(info.getCardId()  != null && info.getCardId().length() >0){
                            has_face_infos.add(info);
                        }else{
                            not_face_infos.add(info);
                        }
                    }

                    // 分批上报【人脸匹配】成功的事件
                    if ( !has_face_infos.isEmpty() ) {
                        reportSendHttpAndLog(reportJson,has_face_infos,img_base64);
                    }

                    // 分批上报【人脸匹配】不成功的事件，作为普通事件监控格式上报
                    if ( !not_face_infos.isEmpty() ) {
                        // eventType： 0-安全帽监测、15-安全帽+人脸识别   设置为安全帽普通监控事件
                        if (params.getEventType().intValue() == 15) {
                            reportJson.put("eventType", 0);
                        }

                        // eventType： 1-反光衣监测、16-反光衣+人脸识别 设置为反光衣普通监控事件
                        if (params.getEventType().intValue() == 16) {
                            reportJson.put("eventType", 1);
                        }
                        reportSendHttpAndLog(reportJson,not_face_infos,img_base64);
                    }

                }
            } catch (Exception e) {
                JSONObject resultError = new JSONObject();
                resultError.put("status",false);
                resultError.put("message",e.getMessage());
                resultError.put("result",null);
                resultError.put("timestamp",System.currentTimeMillis());
                return resultError;
            }

        }
        JSONObject resultMessage = new JSONObject();
        resultMessage.put("status",true);
        resultMessage.put("message","图片识别任务已开启,请耐心等待数据上报");
        resultMessage.put("result",null);
        resultMessage.put("timestamp",System.currentTimeMillis());
        return resultMessage;
    }

    /**
     * 将jsonArray 转化为 人脸信息列表
     * @param faceArray
     * @return
     */
    private List<FaceItemVo> jsonToFaceList(JSONArray faceArray){
        List<FaceItemVo> result = new ArrayList<FaceItemVo>();

        for (int i =0;i<faceArray.size();i++) {
            JSONObject faceJson = faceArray.getJSONObject(i);

            FaceItemVo item = new FaceItemVo();
            item.setName(faceJson.getString("name"));
            item.setSim(faceJson.getString("sim"));
            JSONArray box = faceJson.getJSONArray("box");
            item.setBox(box.getString(0) + "," + box.getString(1) + "," + box.getString(2) + "," + box.getString(3));
            result.add(item);
        }
        return result;
    }
    /**
     * 图片事件异常事件匹配人脸信息
     * @param imageEventInfo 图片事件信息
     * @param faceArray 人脸识别信息JSON数组   {"name": "李建芳_132440196902121014","sim": 0.3934230878458037,"box": [781,417,848,506]  }
     * @return
     */
    private List<ReportInfoItemVo> imageEventInfoMatchFaces(JSONObject imageEventInfo, List<FaceItemVo>  faceArray) {
        List<ReportInfoItemVo> result = new ArrayList<ReportInfoItemVo>();
        JSONArray targets  = imageEventInfo.getJSONArray("targets");
        if (targets == null || targets.isEmpty()) {
            log.error("上报事件失败（图片服务）:返回信息解析失败 - targets为空");
        }

        if (faceArray == null || faceArray.isEmpty()) {
            log.error("上报事件失败（图片服务）:图片中无人脸身份匹配信息");
        }
        // 遍历eventInfo 输出异常事件对象列表
        for (int i = 0; i < targets.size(); i++) {
            JSONObject t_bbox = targets.getJSONObject(i).getJSONObject("bbox");
            ReportInfoItemVo item = formatReportItem(t_bbox);
            result.add(item);
        }

        // 将人脸信息匹配到最佳的事件上
        result = matchFaceToEventInfo(faceArray,result);

        return result;
    }

    /**
     * ReportItem 类型格式
     * @param targetJsonObject  target对象JSON数据
     * @return
     */
    private ReportInfoItemVo formatReportItem(JSONObject targetJsonObject){
        ReportInfoItemVo item = new ReportInfoItemVo();

        JSONObject box = targetJsonObject.getJSONObject("box");
        String prob = targetJsonObject.getString("prob");
        String label = targetJsonObject.getString("label");
        JSONArray color = targetJsonObject.getJSONArray("color");
        int minX = (int) Math.floor(box.getDouble("left_top_x"));
        int minY = (int) Math.floor(box.getDouble("left_top_y"));
        int maxX = (int) Math.floor(box.getDouble("right_bottom_x"));
        int maxY = (int) Math.floor(box.getDouble("right_bottom_y"));

//        JSONObject item = new JSONObject();
        item.setCoordinate(minX + "," + minY + "," + maxX + "," + maxY);
//        item.put("coordinate", minX + "," + minY + "," + maxX + "," + maxY);
        item.setLabel(label);
//        item.put("label", label);
        item.setColor(rgbToHex(color.getInteger(0), color.getInteger(1), color.getInteger(2)));
//        item.put("color", rgbToHex(color.getInteger(0), color.getInteger(1), color.getInteger(2)));
        item.setConfidence(prob);
//        item.put("confidence", prob);

        String cardId = "";
        // 寻找坐标最近的一个faceID
        item.setCardId(cardId);
//        item.put("cardId", cardId);
        return item;
    }

    /**
     * 保存params中的所有文件
     * @param params
     */
    private void saveAllFile(RequestUploaddemoParams params){
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

    /**
     * 文件转base64数据格式
     * @param filePath 目标文件路径
     * @return
     */
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
     * @param eventInfoVo 事件信息
     * @param faceArray 人脸识别信息JSON数组   {"name": "李建芳_132440196902121014","sim": 0.3934230878458037,"box": [781,417,848,506]  }
     * @return 根据坐标匹配事件人员
     */
    private List<ReportInfoItemVo> getInfoListMatchFaces(EventInfoVo eventInfoVo, List<FaceItemVo>  faceArray){
        List<ReportInfoItemVo> result = new ArrayList<ReportInfoItemVo>();

        JSONArray array = JSONObject.parseArray(eventInfoVo.getDetails());
        if(array == null || array.isEmpty()){
            log.error("上报事件信息失败，"+ eventInfoVo.getEvent_id() +"的details为空");
        }

        JSONArray targets = array.getJSONObject(0).getJSONArray("targets");
        if(array == null || array.isEmpty()){
            log.error("上报事件信息失败，"+ eventInfoVo.getEvent_id()  +"的targets为空");
        }

        if(faceArray ==null || faceArray.isEmpty()){
            log.error("上报事件信息失败::无人脸身份匹配信息，"+ eventInfoVo.getEvent_id());
        }
        // 遍历eventInfo 输出异常事件对象列表
        for (int i =0;i<targets.size();i++){
            JSONObject t = targets.getJSONObject(i);
            ReportInfoItemVo item = formatReportItem(t);
            result.add(item);
        }

        // 将人脸信息匹配到最佳的事件上
        result = matchFaceToEventInfo(faceArray,result);
        // cardId为空的，证明人脸匹配失败
        return result;
    }

    /**
     * 将人脸信息匹配到异常事件上  face.name -> info.cardId
     * @param faceArray
     * @param eventArray
     */
    private List<ReportInfoItemVo> matchFaceToEventInfo(List<FaceItemVo> faceArray, List<ReportInfoItemVo> eventArray){

        // 人脸信息匹配到最佳的事件上
        // 遍历人脸，将人脸信息尽量匹配到异常事件上
        for (FaceItemVo face  : faceArray) {
            String[] box = face.getBox().split(",");
            int faceMinX = new Integer(box[0]);
            int faceMinY = new Integer(box[1]);
            int faceMaxX = new Integer(box[2]);
            int faceMaxY = new Integer(box[3]);

            int faceMiddleX = new Integer((faceMinX + faceMaxX) / 2);


            int current_face_match_index = -1;
            int min_face_event_position = -1;
            // 遍历异常事件列表，匹配与当前人脸信息相对位置最近的对象
            for (int k =0;k<eventArray.size();k++) {
                // 获取每一个异常事件
                ReportInfoItemVo eventJsonInfo = eventArray.get(k);
                // 获取坐标
                String[] event_info_array = eventJsonInfo.getCoordinate().split(",");
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
                    log.error("上报事件信息失败，coordinate异常事件坐标新为空："+eventJsonInfo.getConfidence());
                }
            }

            // 存在问题： 如果人脸在附近（距离小于FACE_MAX_OFFSET）， 目标事件的人脸有识别  会出现身份赋值错误
            // min_face_event_position 最小距离  current_face_match_index 最佳匹配的人脸下标
            if(min_face_event_position < FACE_MAX_OFFSET && current_face_match_index >= 0){
                String cardId = face.getName();
                if(face.getName().split("_").length > 1){
                    cardId = face.getName().split("_")[1];
                }

                eventArray.get(current_face_match_index).setCardId(cardId);
                eventArray.get(current_face_match_index).setFaceBox(face.getBox());
            }

        }
        return eventArray;
    }

    /**
     * 获取异常事件信息
     * @param eventInfoVo 事件信息
     * @return 根据坐标匹配事件人员
     */
    private List<ReportInfoItemVo>  getCommonInfoList(EventInfoVo eventInfoVo){
        List<ReportInfoItemVo>  result = new ArrayList<ReportInfoItemVo>();

        JSONArray array = JSONObject.parseArray(eventInfoVo.getDetails());
        if(array == null || array.isEmpty()){
            log.error("上报事件信息失败，"+ eventInfoVo.getEvent_id() +"的details为空");
        }

        JSONArray targets = array.getJSONObject(0).getJSONArray("targets");
        if(array == null || array.isEmpty()){
            log.error("上报事件信息失败，"+ eventInfoVo.getEvent_id()  +"的targets为空");
        }

        for (int i =0;i<targets.size();i++){
            JSONObject t = targets.getJSONObject(i);
            ReportInfoItemVo item = formatReportItem(t);
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
     * @param faceArray 人脸识别信息JSON数组 人脸识别信息JSON数组   {"name": "李建芳_132440196902121014","sim": 0.3934230878458037,"box": [781,417,848,506]  }
     * @return 封装为上报接口的数据结构
     */
    private List<ReportInfoItemVo> getFaceInfoFormat(JSONArray faceArray){
        List<ReportInfoItemVo> result = new ArrayList<ReportInfoItemVo>();
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

            ReportInfoItemVo item = new ReportInfoItemVo();
            item.setFaceBox(minX+","+minY+","+maxX+","+maxY);
            item.setCoordinate(minX+","+minY+","+maxX+","+maxY);
//            item.put("coordinate",minX+","+minY+","+maxX+","+maxY);
            item.setLabel(label);
//            item.put("label",label);
            item.setColor(color);
//            item.put("color",color);
            item.setConfidence(prob);
//            item.put("confidence",prob);
            item.setCardId(cardId);
//            item.put("cardId",cardId);

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
