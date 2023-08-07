package com.app.freya.bean;


public class OtaBean {



    private String fileName;

    private String ota;

    private int versionCode;

    private boolean forceUpdate;

    private String content;

    private String platform;


    private boolean isError;
    private String errorMsg;

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getOta() {
        return ota;
    }

    public void setOta(String ota) {
        this.ota = ota;
    }

    public int getVersionCode() {
        return versionCode;
    }

    public void setVersionCode(int versionCode) {
        this.versionCode = versionCode;
    }

    public boolean isForceUpdate() {
        return forceUpdate;
    }

    public void setForceUpdate(boolean forceUpdate) {
        this.forceUpdate = forceUpdate;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public boolean isError() {
        return isError;
    }

    public void setError(boolean error) {
        isError = error;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }

    @Override
    public String toString() {
        return "OtaBean{" +
                "fileName='" + fileName + '\'' +
                ", ota='" + ota + '\'' +
                ", versionCode=" + versionCode +
                ", forceUpdate=" + forceUpdate +
                ", content='" + content + '\'' +
                ", platform='" + platform + '\'' +
                '}';
    }
}
