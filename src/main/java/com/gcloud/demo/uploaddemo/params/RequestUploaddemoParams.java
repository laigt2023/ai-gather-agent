package com.gcloud.demo.uploaddemo.params;

import org.springframework.web.multipart.MultipartFile;

public class RequestUploaddemoParams {
    private String file_name;
    private MultipartFile[] file;

    private String AppKeyID;
    private String AppKeySecret;

    public MultipartFile[] getFile() {
        return file;
    }

    public void setFile(MultipartFile[] file) {
        this.file = file;
    }

    public String getFile_name() {
        return file_name;
    }

    public void setFile_name(String file_name) {
        this.file_name = file_name;
    }

    public String getAppKeyID() {
        return AppKeyID;
    }

    public void setAppKeyID(String appKeyID) {
        AppKeyID = appKeyID;
    }

    public String getAppKeySecret() {
        return AppKeySecret;
    }

    public void setAppKeySecret(String appKeySecret) {
        AppKeySecret = appKeySecret;
    }
}
