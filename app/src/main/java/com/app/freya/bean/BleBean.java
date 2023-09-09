package com.app.freya.bean;

import android.bluetooth.BluetoothDevice;

/**
 * Created by Admin
 * Date 2022/8/4
 * @author Admin
 */
public class BleBean {

    private BluetoothDevice bluetoothDevice;

    private int rssi;

    /**VersionNumber**/
    private String productNumber;


    private String recordStr;


    private String bleName;

    private String bleMac;



    //是否是连接过的设备
    private boolean isBind;

    public boolean isBind() {
        return isBind;
    }

    public void setBind(boolean bind) {
        isBind = bind;
    }

    public BleBean() {
    }

    public BleBean(BluetoothDevice bluetoothDevice, int rssi) {
        this.bluetoothDevice = bluetoothDevice;
        this.rssi = rssi;

    }

    public BleBean(BluetoothDevice bluetoothDevice, int rssi, String recordStr) {
        this.bluetoothDevice = bluetoothDevice;
        this.rssi = rssi;
        this.recordStr = recordStr;
    }

    public BleBean(BluetoothDevice bluetoothDevice, int rssi, String productNumber, String recordStr) {
        this.bluetoothDevice = bluetoothDevice;
        this.rssi = rssi;
        this.productNumber = productNumber;
        this.recordStr = recordStr;
    }

    public BluetoothDevice getBluetoothDevice() {
        return bluetoothDevice;
    }

    public void setBluetoothDevice(BluetoothDevice bluetoothDevice) {
        this.bluetoothDevice = bluetoothDevice;
    }

    public int getRssi() {
        return rssi;
    }

    public void setRssi(int rssi) {
        this.rssi = rssi;
    }


    public String getRecordStr() {
        return recordStr;
    }

    public void setRecordStr(String recordStr) {
        this.recordStr = recordStr;
    }

    public String getProductNumber() {
        return productNumber;
    }

    public void setProductNumber(String productNumber) {
        this.productNumber = productNumber;
    }

    public String getBleName() {
        return bleName;
    }

    public void setBleName(String bleName) {
        this.bleName = bleName;
    }

    public String getBleMac() {
        return bleMac;
    }

    public void setBleMac(String bleMac) {
        this.bleMac = bleMac;
    }
}
