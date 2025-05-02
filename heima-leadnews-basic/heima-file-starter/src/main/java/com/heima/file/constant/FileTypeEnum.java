package com.heima.file.constant;

public enum FileTypeEnum {

    IMAGE("image/*"),
    DOCUMENT("application/octet-stream"), // 可根据需求具体细分为 pdf、word 等
    VIDEO("video/*"),
    AUDIO("audio/*"),
    HTML("text/html");

    private final String contentType;

    FileTypeEnum(String contentType) {
        this.contentType = contentType;
    }

    public String getContentType() {
        return contentType;
    }
}
