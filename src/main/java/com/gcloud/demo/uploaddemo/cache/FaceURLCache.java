package com.gcloud.demo.uploaddemo.cache;

import com.gcloud.demo.uploaddemo.mybatisplus.entity.FaceInfo;
import com.gcloud.demo.uploaddemo.mybatisplus.mapper.FaceInfoMapper;
import com.gcloud.demo.uploaddemo.util.SpringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
@Slf4j
@Component
public class FaceURLCache {
    // 人脸数据库MAP key值为 idCard
    private static Map<String, String> ID_CARD_URL_MAP = new HashMap<>();

    // 人脸数据库MAP key值为 idCard
    private static Map<String, String> UUID_FACE_URL_MAP = new HashMap<>();

    private static FaceInfoMapper faceMapper;

    static {
        log.info("加载人脸库图片缓存信息 start...");
        init();
        log.info("加载人脸库图片缓存信息 end...");
    }
    public static void init(){
        ID_CARD_URL_MAP = new HashMap<>();
        UUID_FACE_URL_MAP  = new HashMap<>();
        reloadFaceDb();
    }

    /**
     * 通过idCard方式获取缓存中人脸图片地址
     * @param idCard 身份证号码
     * @return
     */
    public static String getFaceUrlByIDCard(String idCard){
        if(!ID_CARD_URL_MAP.containsKey(idCard)){
            reloadOneFaceDbByIDCard(idCard);
        }
        return ID_CARD_URL_MAP.get(idCard);
    }
    /**
     * 通过UUID方式获取缓存中人脸图片地址
     * @param uuid  UUID
     * @return
     */
    public static String getFaceUrlByUUID(String uuid){
        if(!UUID_FACE_URL_MAP.containsKey(uuid)){
            reloadOneFaceDbByUUID(uuid);
        }
        return UUID_FACE_URL_MAP.get(uuid);
    }
    private static FaceInfoMapper getFaceMapperBySpring(){
        faceMapper =SpringUtils.getBean(FaceInfoMapper.class);
        return faceMapper;
    }

    /**
     * 重新加载人脸库，全量进入缓存中
     */
    public static void reloadFaceDb(){
        // 从数据库中加载人脸信息
        faceMapper = getFaceMapperBySpring();

        List<FaceInfo> list = faceMapper.selectList(null);

        // 数据库查询无误后，再更新人脸库缓存
        ID_CARD_URL_MAP = new HashMap<>();
        UUID_FACE_URL_MAP  = new HashMap<>();

        for (FaceInfo one : list) {
            ID_CARD_URL_MAP.put(one.getIdCard(),one.getFaceFilePath());
            UUID_FACE_URL_MAP.put(one.getUuid(),one.getFaceFilePath());
        }

        log.info("人脸库缓存加载完毕，共计:" + list.size() + "条人脸记录");
    }

    /**
     * 通过idCard方式单个人脸信息重新加载到人脸库缓存中
     */
    private static void reloadOneFaceDbByIDCard(String idCard){
        faceMapper = getFaceMapperBySpring();
        Map<String, Object> whereMap =  new HashMap<String, Object>();
        whereMap.put("id_card",idCard);
        List<FaceInfo> list = faceMapper.selectByMap(whereMap);
        if(list != null && list.size() >0){
            FaceInfo one = list.get(0);
            ID_CARD_URL_MAP.put(one.getIdCard(),one.getFaceFilePath());
            UUID_FACE_URL_MAP.put(one.getUuid(),one.getFaceFilePath());
        }
    }

    /**
     * 通过uuid方式单个人脸信息重新加载到人脸库缓存中
     */
    private static void reloadOneFaceDbByUUID(String uuid){
        faceMapper = getFaceMapperBySpring();
        Map<String, Object> whereMap =  new HashMap<String, Object>();
        whereMap.put("uuid",uuid);
        List<FaceInfo> list = faceMapper.selectByMap(whereMap);
        if(list != null && list.size() >0){
            FaceInfo one = list.get(0);
            ID_CARD_URL_MAP.put(one.getIdCard(),one.getFaceFilePath());
            UUID_FACE_URL_MAP.put(one.getUuid(),one.getFaceFilePath());
        }
    }
}
