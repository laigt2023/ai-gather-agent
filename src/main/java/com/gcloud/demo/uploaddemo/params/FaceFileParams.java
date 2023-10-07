package com.gcloud.demo.uploaddemo.params;

import org.springframework.web.multipart.MultipartFile;

public class FaceFileParams {
    private Float sim;
    private MultipartFile file;

    public Float getSim() {
        return sim;
    }

    public void setSim(Float sim) {
        this.sim = sim;
    }

    public MultipartFile getFile() {
        return file;
    }

    public void setFile(MultipartFile file) {
        this.file = file;
    }
}
