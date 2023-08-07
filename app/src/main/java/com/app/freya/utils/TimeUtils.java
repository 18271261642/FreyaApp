package com.app.freya.utils;

import android.content.Context;

import com.app.freya.R;

import java.util.Calendar;

public class TimeUtils {


    /**
     * 获取时间
     */
    public static String getTimeByNow(Context context){
        Calendar calendar =Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        if(hour>=5 && hour<12){
            return context.getResources().getString(R.string.string_morning_good);
        }

        if(hour>=12 && hour<=18){
            return context.getResources().getString(R.string.string_afternoon_good);
        }

        return context.getResources().getString(R.string.string_evening_good);
    }
}
