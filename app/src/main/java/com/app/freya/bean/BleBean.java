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


    public BleBean(BluetoothDevice bluetoothDevice, int rssi) {
        this.bluetoothDevice = bluetoothDevice;
        this.rssi = rssi;

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
}
