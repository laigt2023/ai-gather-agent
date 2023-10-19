package com.gcloud.demo.uploaddemo.model;

public class FaceItemVo {
    // 身份信息 例：李建芳_132440196902121014
    private String name;
    // 识别值 0-1 例：0.3934230878458037
    private String sim;
    // 人脸坐标 例：781,417,848,506
    private String box;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSim() {
        return sim;
    }

    public void setSim(String sim) {
        this.sim = sim;
    }

    public String getBox() {
        return box;
    }

    public void setBox(String box) {
        this.box = box;
    }
}
