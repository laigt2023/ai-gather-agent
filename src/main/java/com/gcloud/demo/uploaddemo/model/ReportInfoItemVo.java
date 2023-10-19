package com.gcloud.demo.uploaddemo.model;

public class ReportInfoItemVo {
    // 事件坐标： 1054,602,1230,842  left_top_x,left_top_y,right_bottom_x,right_bottom_y
   private String coordinate;
   // 事件标签名称
   private String label;
   // 颜色
   private String color;
   // 可信值 0~1 (0.9244)
   private String confidence;
   // 身份证ID
   private String cardId;

   // 人脸坐标
   private String faceBox;

    public String getCoordinate() {
        return coordinate;
    }

    public void setCoordinate(String coordinate) {
        this.coordinate = coordinate;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getConfidence() {
        return confidence;
    }

    public void setConfidence(String confidence) {
        this.confidence = confidence;
    }

    public String getCardId() {
        return cardId;
    }

    public void setCardId(String cardId) {
        this.cardId = cardId;
    }

    public String getFaceBox() {
        return faceBox;
    }

    public void setFaceBox(String faceBox) {
        this.faceBox = faceBox;
    }
}
