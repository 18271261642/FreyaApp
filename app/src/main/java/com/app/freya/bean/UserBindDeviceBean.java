package com.app.freya.bean;

import org.litepal.crud.LitePalSupport;

/**
 * 记录用户绑定过的设备
 */
public class UserBindDeviceBean extends LitePalSupport {

    private String deviceName;

    private String deviceMac;

    /**绑定的时间 yyyy-MM-dd HH:mm:ss格式，排序**/
    private String bindTime;

    /**rssi**/
    private int rssi;

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getDeviceMac() {
        return deviceMac;
    }

    public void setDeviceMac(String deviceMac) {
        this.deviceMac = deviceMac;
    }

    public String getBindTime() {
        return bindTime;
    }

    public void setBindTime(String bindTime) {
        this.bindTime = bindTime;
    }

    public int getRssi() {
        return rssi;
    }

    public void setRssi(int rssi) {
        this.rssi = rssi;
    }
}
