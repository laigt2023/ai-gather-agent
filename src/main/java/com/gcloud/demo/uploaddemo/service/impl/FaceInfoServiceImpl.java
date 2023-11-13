package com.gcloud.demo.uploaddemo.service.impl;

import com.gcloud.demo.uploaddemo.cache.FaceURLCache;
import com.gcloud.demo.uploaddemo.mybatisplus.entity.FaceInfo;
import com.gcloud.demo.uploaddemo.mybatisplus.mapper.AlarmHistoryMapper;
import com.gcloud.demo.uploaddemo.mybatisplus.mapper.FaceInfoMapper;
import com.gcloud.demo.uploaddemo.service.IFaceInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.*;

@Service
public class FaceInfoServiceImpl implements IFaceInfoService {
    @Value("${gcloud.beijing.face_file_save_dir}")
    private String FACE_FILE_SAVE_DIR;
    @Value("${gcloud.beijing.site-id}")
    private String CURRENT_SITE_ID;

    @Autowired
    private FaceInfoMapper faceInfoMapper;

    @Override
    public void refreshFaceDb(String siteId, String loadPath) {
        if(siteId==null){
            siteId = CURRENT_SITE_ID;
        }

        if(loadPath!=null && loadPath.length() > 0){
            recursionReloadFileDir(siteId,loadPath);
        }else{
            recursionReloadFileDir(siteId,FACE_FILE_SAVE_DIR);
        }
    }

    @Override
    public FaceInfo getFaceInfoByIdCard(String idCard) {
        Map<String, Object> whereMap =  new HashMap<>();
        whereMap.put("id_card",idCard);
        whereMap.put("site_id",CURRENT_SITE_ID);
        List<FaceInfo> list = faceInfoMapper.selectByMap(whereMap);
        if(list !=null && list.size() > 0){
             return list.get(0);
        }

        return null;
    }

    @Override
    public List<FaceInfo> list() {
        return faceInfoMapper.selectList(null);
    }

    @Override
    public String getFaceImage(String uuid, String idCard) {
        if(uuid !=null && uuid.length() > 0){
            return FaceURLCache.getFaceUrlByUUID(uuid);
        }

        if(idCard !=null && idCard.length() > 0){
            return FaceURLCache.getFaceUrlByIDCard(idCard);
        }

        return null;
    }

    /**
     * 递归刷新目录下所有文件
     * @param siteId 项目ID
     * @param dir_path 目录
     */
    void recursionReloadFileDir(String siteId,String dir_path){
        File dir = new File(dir_path);
        if (dir.isDirectory()) {
            File[] files = dir.listFiles();
            if (files != null) {
                for (File file : files) {
                    // 递归所有目录
                    if (file.isDirectory()) {
                        recursionReloadFileDir(siteId,file.getPath());
                    } else {
                        String lowerFileName = file.getName().toLowerCase();
                        if(lowerFileName.endsWith(".jpg") || lowerFileName.endsWith(".jpeg") || lowerFileName.endsWith(".png")){
                            updateFaceInfoDbByFile(siteId,file);
                        }
                    }
                }
            }
        }
    }

    /**
     * 根据人脸图片更新人脸库
     * @param siteId 项目ID
     * @param faceFile 人脸文件
     */
    void updateFaceInfoDbByFile(String siteId,File faceFile){
        String fileName = faceFile.getName();
        if(fileName.lastIndexOf(".") > -1){
            fileName = fileName.substring(0,fileName.lastIndexOf("."));
        }
        String[] array = fileName.split("_");
        if(array.length >= 2){
            String realName = array[0];
            String idCard = array[1];

            FaceInfo entity = new FaceInfo();

            // 查询id_card是否已存在
            Map<String, Object> whereMap =  new HashMap<>();
            whereMap.put("id_card",idCard);
            whereMap.put("site_id",siteId);
            List<FaceInfo> list = faceInfoMapper.selectByMap(whereMap);
            if(list !=null && list.size() > 0){
                entity = list.get(0);
            }

            entity.setRealName(realName);
            entity.setIdCard(idCard);
            entity.setSiteId(siteId);
            entity.setUpdateDate(new Date());
            entity.setFaceFilePath(faceFile.getPath());

            //  编辑的时候，如果uuid为空时，存储UUID
            if(entity.getUuid() == null){
                entity.setUuid(UUID.randomUUID().toString());
            }

            if(entity.getId() !=null ){
                faceInfoMapper.updateById(entity);
            }else{
                faceInfoMapper.insert(entity);
            }
        }
    }
}
