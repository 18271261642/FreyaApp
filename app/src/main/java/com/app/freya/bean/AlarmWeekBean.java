package com.app.freya.bean;

public class AlarmWeekBean {

    /**名称 周一 周二**/
    private String weekName;

    /**名称表示 1周日 ；2周一；4周二；8周三；16周四；32周五；64周六**/
    private int weekNumber;

    /**是否被选中**/
    private boolean isChecked;

    public AlarmWeekBean() {
    }

    public AlarmWeekBean(String weekName, int weekNumber) {
        this.weekName = weekName;
        this.weekNumber = weekNumber;
    }



    public String getWeekName() {
        return weekName;
    }

    public void setWeekName(String weekName) {
        this.weekName = weekName;
    }

    public int getWeekNumber() {
        return weekNumber;
    }

    public void setWeekNumber(int weekNumber) {
        this.weekNumber = weekNumber;
    }

    public boolean isChecked() {
        return isChecked;
    }

    public void setChecked(boolean checked) {
        isChecked = checked;
    }
}
