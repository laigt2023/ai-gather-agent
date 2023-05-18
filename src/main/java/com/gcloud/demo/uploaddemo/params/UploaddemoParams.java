package com.gcloud.demo.uploaddemo.params;

import org.springframework.web.multipart.MultipartFile;

public class UploaddemoParams {
    private String file_name;
    private MultipartFile[] file;

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
}
