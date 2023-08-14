package com.gcloud.demo.uploaddemo.thread;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StreamUtils;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class DownloadAndPredictThread extends Thread{
    // 下载地址
    private String url;
    // 保存路径
    private String filePath;
    // 1/2 意思是每隔2秒生成一张图片
    private String predictFrameRate = "1/2";
    // 文件下载中，生成文件名，如：PU_23020036_00_20230429_163238_LA0800.mp4.downloading
    private String downloadingFileName;
    // 文件下载成功后生成文件名，如：PU_23020036_00_20230429_163238_LA0800.mp4.success
    private String successFileName;
    // 是否下载已完成
    private Boolean isDownloadCompleted= false;
    // AI解析接口地址
    private String aiType = "";

    // 推理类型对应的推理接口地址Map
    private Map<String,String> predictUrlTypeMap = new HashMap<>();


    public DownloadAndPredictThread(String url, String filePath,String predictFrameRate,String downloadingFileName, String successFileName,String aiType, Map<String,String> predictUrlTypeMap) {
        this.url = url;
        this.filePath = filePath;
        this.predictFrameRate = predictFrameRate;
        this.downloadingFileName = downloadingFileName;
        this.successFileName = successFileName;
        if(predictUrlTypeMap != null){
            this.predictUrlTypeMap = predictUrlTypeMap;
        }
        this.aiType = aiType;
    }

    @Override
    public void run() {
        // 文件下载成功后生成文件名，如：PU_23020036_00_20230429_163238_LA0800.mp4.success
        String startDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        HttpURLConnection connection = null;
        InputStream inputStream = null;
        FileOutputStream outputStream = null;

        File file = new File(filePath);
        try {
            if(!file.getCanonicalFile().exists()){
                file.getParentFile().mkdirs();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        try {
            // 文件已经存在时，不需要重复下载
            if(!isDownloadCompleted){
                URL fileUrl = new URL(url);
                System.out.println("开始下载视频文件(：" + filePath + ") : " + url);
                log.info("开始下载视频文件(：" + filePath + ") : " + url);


                // 进行视频下载
                connection = (HttpURLConnection) fileUrl.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(5000);
                connection.setReadTimeout(5000);

                inputStream = connection.getInputStream();
                outputStream = new FileOutputStream(new File(filePath));

                // 下载中，生成文件名为：PU_23020036_00_20230429_163238_LA0800.mp4.downloading
                createDownloadingFile(startDate);

                // 使用Apache Commons IO库将输入流复制到输出流
                StreamUtils.copy(inputStream, outputStream);
            }


            // 下载成功后，生成文件名为：PU_23020036_00_20230429_163238_LA0800.mp4.success
            createSuccessFile(startDate);
            // 下载成功后，删除文件名为：PU_23020036_00_20230429_163238_LA0800.mp4.downloading
            delDownloadingFile();


            // 正式处理视频文件
            aiPredict();


        }catch (IOException e) {
            throw new RuntimeException(e);
        }
        finally {
            if (connection != null) {
                connection.disconnect();
            }
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    private void createDownloadingFile(String startDate) throws IOException {
        // 下载中，生成文件名为：PU_23020036_00_20230429_163238_LA0800.mp4.downloading
        File downloadingFile = new File(downloadingFileName);
        if(!downloadingFile.exists()){
            FileUtils.touch(downloadingFile);
            // 写入文件
            try {
                FileWriter writer = new FileWriter(downloadingFile);
                writer.write("startTime: " + startDate + "\n");
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    private void delDownloadingFile(){
        // 下载中，生成文件名为：PU_23020036_00_20230429_163238_LA0800.mp4.downloading
        File downloadingFile = new File(downloadingFileName);
        if(downloadingFile.exists()){
            downloadingFile.delete();
        }
    }

    private void createSuccessFile(String startDate) throws IOException {
        // 下载成功后，生成文件名为：PU_23020036_00_20230429_163238_LA0800.mp4.success
        File successFile = new File(successFileName);
        if(!successFile.exists()){
            String endDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
            FileUtils.touch(successFile);

            // 写入文件
            try {
                FileWriter writer = new FileWriter(successFile);
                writer.write("startTime: " + startDate + "\n");
                writer.write("file: " + filePath + "\n");
                writer.write("endTime: " + endDate + "\n");
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    // 视频文件处理，生成图片（按照predictFrameRate 帧率进行）
    private void mp4ToJpg() throws FileNotFoundException {


        String outPutPath = filePath.replace(".mp4", "").replace(".MP4", "");
        File dir = new File(outPutPath);
        String  fileName = new File(filePath).getName().replace(".mp4", "").replace(".MP4", "");
        if(!dir.exists()){
            dir.mkdirs();
        }
        String endDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        System.out.println(endDate + "download success: " + filePath);
        log.info(endDate + "download success: " + filePath);
        String cmd = "ffmpeg -i " + filePath  + " -q:v 0 -vf fps=" + predictFrameRate + " " + outPutPath + File.separator  + fileName + "_%06d.jpg";
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) {
            // Windows系统
            // windows下执行命令 ffmpeg 阻塞不完全，可能需要多请求一次（第一次请求时，阻塞不完全，没有完成全部图片的生成）
            executeCommandWindows(cmd);
        } else {
            // 类Unix系统（例如Linux、Mac OS）
            executeCommandUnix(cmd);
        }


        // 进行图片预测
        // jpgPredictToXmlAndJpeg();
    }
    // windows下执行命令会会弹出黑框 可能出现命令阻塞不完全，需要多请求一次（第一次请求时，阻塞不完全，没有完成全部图片的生成）
    private void executeCommandWindows(String command) {
        StringBuilder sb = new StringBuilder();
        Runtime runtime = Runtime.getRuntime();
        try {
            java.lang.Process process = runtime.exec( "cmd /c start " + command);
//            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            int exitCode = process.waitFor();
            System.out.println(command+ " 命令执行完毕，退出码：" + exitCode);
            log.info(command+ " 命令执行完毕，退出码：" + exitCode);


        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
    // 类Unix系统（例如Linux、Mac OS）下执行命令  阻塞完全
    private void executeCommandUnix(String command) {
        try {
            Process process = Runtime.getRuntime().exec(new String[]{"bash", "-c", command});

            InputStream inputStream = process.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

            int exitCode = process.waitFor();
            System.out.println(command+ "命令执行完毕，退出码：" + exitCode);
            log.info(command+ " 命令执行完毕，退出码：" + exitCode);
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
    // 图片预测并生成响应的xml文件和jpeg图片（待推理标识）
    private void jpgPredictToXmlAndJpeg() throws FileNotFoundException {
        if(aiType.equals("all")){
            for (Map.Entry<String, String> entry : predictUrlTypeMap.entrySet()) {
                // 获取键和值
                String aiTypeName = entry.getKey();
                String aiApiUrl = entry.getValue();

                mptToJpgCmd(aiTypeName, aiApiUrl);
            }
        }else{

            String aiTypeName = aiType;
            String aiApiUrl = predictUrlTypeMap.get(aiType);

            if(aiApiUrl!=null && !aiApiUrl.equals("")) {
                mptToJpgCmd(aiTypeName, aiApiUrl);
            }
        }
    }
    // 获取执行命令
    private void mptToJpgCmd(String aiTypeName, String aiApiUrl){
        String scriptDir = System.getProperty("user.dir");
        String predictScriptName = scriptDir + File.separator + "py_script" + File.separator + "ai_predict_to_xml.py";

        String jpgPath = filePath.replace(".mp4", "").replace(".MP4", "");
        String xmlOutPutPath = jpgPath + "_" + aiTypeName + "_xml";
        //  执行图片预测脚本 入参1.图片路径 2.输出路径 3.是否跳过重复识别文件 4.是否输出jpeg推理图片 5.AI推理服务地址 6.是否上报AI推理结果
        String cmd = "python3 " +  predictScriptName + " " + jpgPath + " " + xmlOutPutPath + " true" + " true" + " " + aiApiUrl + " true";

        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) {
            // Windows系统 本地测试时，python3 替换为 python
            cmd = cmd.replace("python3", "python");
            executeCommandWindows(cmd);
        } else {
            // 类Unix系统（例如Linux、Mac OS）
            executeCommandUnix(cmd);
        }
    }

    // AI预测统一开启方法
    public void aiPredict() throws FileNotFoundException {
        // 视频转图片
        mp4ToJpg();

        // 进行图片预测
        jpgPredictToXmlAndJpeg();
    }

    public Boolean getDownloadCompleted() {
        return isDownloadCompleted;
    }

    public void setDownloadCompleted(Boolean downloadCompleted) {
        isDownloadCompleted = downloadCompleted;
    }
}
