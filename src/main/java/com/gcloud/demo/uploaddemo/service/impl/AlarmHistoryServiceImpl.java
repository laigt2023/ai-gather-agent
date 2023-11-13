package com.gcloud.demo.uploaddemo.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gcloud.demo.uploaddemo.exception.GcException;
import com.gcloud.demo.uploaddemo.mybatisplus.entity.AlarmFile;
import com.gcloud.demo.uploaddemo.mybatisplus.entity.AlarmHistory;
import com.gcloud.demo.uploaddemo.mybatisplus.mapper.AlarmFileMapper;
import com.gcloud.demo.uploaddemo.mybatisplus.mapper.AlarmHistoryMapper;
import com.gcloud.demo.uploaddemo.params.RequestAlarmHistoryPageParams;
import com.gcloud.demo.uploaddemo.params.RequestAlarmHistoryReportParams;
import com.gcloud.demo.uploaddemo.service.IAlarmHistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import sun.misc.BASE64Decoder;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class AlarmHistoryServiceImpl implements IAlarmHistoryService {
    private String BASE64_IMAGE_PRE="data:image/jpeg;base64,";

    @Value("${gcloud.save-dir}")
    private String SAVE_DIR;

    @Autowired
    private AlarmHistoryMapper alarmHistoryMapper;

    @Autowired
    private AlarmFileMapper alarmFileMapper;

    @Override
    public List<AlarmHistory> list(RequestAlarmHistoryPageParams params) {
        List<AlarmHistory> alarmHistoryList = alarmHistoryMapper.selectList(null);
        for(AlarmHistory alarm:alarmHistoryList) {
            System.out.println(alarm);
        }

        return alarmHistoryList;
    }

    @Override
    public IPage<AlarmHistory> page(RequestAlarmHistoryPageParams params) {
        Page<AlarmHistory> page = Page.of(params.getPageNo(),params.getPageSize());

        //queryWrapper组装查询where条件
        LambdaQueryWrapper<AlarmHistory> queryWrapper = new LambdaQueryWrapper<>();

        IPage<AlarmHistory> result = alarmHistoryMapper.selectPage(page,queryWrapper);

        return result;
    }

    @Override
    public int report(RequestAlarmHistoryReportParams params) {
        // 暂时不保存告警图片的 base64格式数据，同一张图片不需要保存多次
        String alarmFileUuid = insertAndSaveAlarmImage(params.getSiteID().toString(),params.getAlarmPicture());

        if(params.getInfo()!=null && !params.getInfo().isEmpty()){
            List<Map<String,Object>> list = params.getInfo();

            // 每一个人脸ID 存储一条记录
            for ( Map<String,Object> map : list ) {

                if(!map.containsKey("cardId") || map.get("cardId") == null || map.get("cardId").toString().trim().length() <1 ){
                    continue;
                }
                AlarmHistory entity = new AlarmHistory();

                // 存储人脸身份证ID
                entity.setFaceIdCard(map.get("cardId").toString());

                entity.setSiteID(params.getSiteID());
                entity.setType(params.getType());
                entity.setEventType(params.getEventType());
                entity.setAlarmDate(params.getAlarmDate());
                if(params.getAlarmTime() != null){
                    entity.setAlarmTime(new Date(params.getAlarmTime()));
                }else{
                    entity.setAlarmTime(new Date());
                }
                entity.setVideoName(params.getVideoName());
                entity.setCameraName(params.getCameraName());

                // 保存告警原图文件UUID
                entity.setAlarmFileUuid(alarmFileUuid);

                entity.setInfo(JSONObject.toJSONString(map));
                alarmHistoryMapper.insert(entity);
            }
        }

        return 1;
    }

    /**
     * base64格式数据转图片保存到数据库和本地中
     * @param siteId
     * @param base64_encode
     * @return uuid
     */
    private String insertAndSaveAlarmImage(String siteId,String base64_encode){
        //对字节数组字符串进行Base64解码并生成图片
        if (base64_encode == null) {
            new GcException("base64数据未空");
        }

        // 解码base64数据，生成图片文件
        BASE64Decoder decoder = new BASE64Decoder();
        byte[] b = new byte[0];
        try {
            //Base64解码
            b = decoder.decodeBuffer(base64_encode);

            for (int i = 0; i < b.length; ++i) {
                //调整异常数据
                if (b[i] < 0) {
                    b[i] += 256;
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        String savePath = getTodayFolderName(siteId);
        // 判断路径是否不存在，不存在就创建文件夹
        File fileDir = new File(savePath);

        if (!fileDir.exists() && !fileDir.isDirectory()) {
            fileDir.mkdirs();
        }

        // 生成一个空文件，自定义图片的名字
        File file = new File(savePath + File.separator + UUID.randomUUID().toString() + ".jpg");

        if (!file.exists()) {
            try {
                file.createNewFile();

                //生成jpg图片
                OutputStream out = new FileOutputStream(file.getPath());
                out.write(b);
                out.flush();
                out.close();

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        AlarmFile alarmFile = new AlarmFile();
        alarmFile.setUuid(UUID.randomUUID().toString());
        alarmFile.setPath(file.getPath());
        alarmFile.setSaveDir(SAVE_DIR);
        alarmFile.setSiteID(siteId);
        alarmFile.setCreateTime(new Date());

        alarmFileMapper.insert(alarmFile);
        return alarmFile.getUuid();
    }
    /** 获取今日日期的文件夹名称
     *
     * @param siteId 项目ID
     * @return
     */
    public String getTodayFolderName(String siteId) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String save_dir = SAVE_DIR + File.separator + "alarm";
        if(!save_dir.endsWith(File.separator)){
            save_dir = save_dir + File.separator;
        }

        // 如果配置了项目名称，则按照项目名称创建文件夹
        if(!StringUtils.isEmpty(siteId)){
            return save_dir + siteId + File.separator + sdf.format(new Date());
        }

        return save_dir + sdf.format(new Date());
    }
}
