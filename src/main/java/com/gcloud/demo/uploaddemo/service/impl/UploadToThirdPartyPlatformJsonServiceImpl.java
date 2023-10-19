package com.gcloud.demo.uploaddemo.service.impl;

import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson.JSON;
import com.gcloud.demo.uploaddemo.model.EventInfoVo;
import com.gcloud.demo.uploaddemo.params.RequestUploaddemoParams;
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
import com.gcloud.demo.uploaddemo.service.IUploadToThirdPartyPlatformService;
import org.springframework.web.multipart.commons.CommonsMultipartFile;


import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

//   方案一实现
@Primary
@Slf4j
@Service
public class UploadToThirdPartyPlatformJsonServiceImpl implements IUploadToThirdPartyPlatformService{
    @Value("${gcloud.upload.jky.picUploadUrl}")
    private String uploadUrl;
    @Value("#{${gcloud.event-name}}")
    private Map<String,String> eventNameMap;

    @Value("#{${gcloud.event-skill-Type}}")
    private Map<String,String> postEventSkillTypeMap;
    @Value("${gcloud.save-dir}")
    private String SAVE_DIR;

    private String projectName = "";

    @Value("${gcloud.upload-after-del}")
    private Boolean UPLOAD_AFTER_DEL;
    @Value("${gcloud.is-post-event}")
    private Boolean IS_POST_EVENT;

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
        if( picFile !=null ){
            Map<String,String> paramMap = new HashMap<String,String>();
            EventInfoVo eventInfoVo = null;
            // 空的时候找本地文件
            if(jsonFile == null){
                String jsonFileName = picFile.getOriginalFilename().replace(".jpeg",".json");
                File localJsonFile= new File(getTodayFolderName() + File.separator + jsonFileName);

                FileItem fileItem = getMultipartFile(localJsonFile, jsonFileName);
                MultipartFile multipartFile = new CommonsMultipartFile(fileItem);
                eventInfoVo = JSON.parseObject(HttpClientUtil.readMultipartFile(multipartFile), EventInfoVo.class);

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
                eventInfoVo = JSON.parseObject(HttpClientUtil.readMultipartFile(jsonFile), EventInfoVo.class);
            }

            paramMap.put("description",eventNameMap.containsKey(eventInfoVo.getApp_id())?eventNameMap.get(eventInfoVo.getApp_id()): eventInfoVo.getApp_name() + "-设备名称：" + eventInfoVo.getSrc_name() + "，时间：" + DateUtil.format(DateUtil.date(eventInfoVo.getCreated()*1000L), "YYYY-MM-dd HH:mm:ss"));
            try {

                   // 根据配置是否上报事件信息
                   if(IS_POST_EVENT!=null && IS_POST_EVENT.booleanValue() == true ){
                       if(postEventSkillTypeMap !=null && postEventSkillTypeMap.containsKey(eventInfoVo.getApp_id())) {
                           String skillType = postEventSkillTypeMap.get(eventInfoVo.getApp_id());
                           log.info("上报事件信息：{}", JSON.toJSON(paramMap).toString());
                           HttpClientUtil.sendPostJson(uploadUrl, picFile, paramMap,skillType);
                       }
                   }

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
