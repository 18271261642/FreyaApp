package com.blala.blalable.listener;

/**
 * 系统数据返回
 */
public interface OnSystemDataListener {

    /**
     *
     * @param circleSpeed 风扇转速
     * @param batteryValue 电池电量
     * @param cpuTemperatureC CPU温度摄氏度
     * @param cpuTemperatureF CPU温度华摄氏度
     * @param gpuTemC 显卡温度摄氏度
     * @param gpuTemF 显卡温度华摄氏度
     * @param hardTemC 硬盘温度摄氏度
     * @param hardTemF 硬盘温度华氏十度
     */
    void onSysData(int circleSpeed,int batteryValue,int cpuTemperatureC,int cpuTemperatureF,int gpuTemC,int gpuTemF,int hardTemC,int hardTemF);
}
