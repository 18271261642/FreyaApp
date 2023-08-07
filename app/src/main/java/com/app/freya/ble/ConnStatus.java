package com.app.freya.ble;

/**
 * 连接状态枚举
 * Created by Admin
 * Date 2022/8/17
 * @author Admin
 */
public enum ConnStatus {

    /**默认状态**/
    DEFAULT,

    /**未连接 默认状态**/
    NOT_CONNECTED,
    /**连接中**/
    CONNECTING,
    /**已经连接**/
    CONNECTED,

    /**数据同步中**/
    IS_SYNC_DATA,

    /**同步完成**/
    IS_SYNC_COMPLETE,

    /**正在同步表盘中**/
    IS_SYNC_DIAL

    ;


   private ConnStatus() {
    }
}
