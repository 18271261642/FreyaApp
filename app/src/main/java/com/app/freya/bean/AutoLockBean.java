package com.app.freya.bean;

/**
 * 自动锁定 bean
 */
public class AutoLockBean {

    /**时间，单位秒**/
    private int autoTime;

    /**是否选中**/
    private boolean isChecked;


    public AutoLockBean() {
    }

    public AutoLockBean(int autoTime) {
        this.autoTime = autoTime;
    }

    public AutoLockBean(int autoTime, boolean isChecked) {
        this.autoTime = autoTime;
        this.isChecked = isChecked;
    }

    public int getAutoTime() {
        return autoTime;
    }

    public void setAutoTime(int autoTime) {
        this.autoTime = autoTime;
    }

    public boolean isChecked() {
        return isChecked;
    }

    public void setChecked(boolean checked) {
        isChecked = checked;
    }
}
