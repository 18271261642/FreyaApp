package com.app.freya.ble;


import android.app.Notification;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;

import com.app.freya.BaseApplication;
import com.app.freya.utils.BikeUtils;
import com.app.freya.utils.MmkvUtils;
import com.blala.blalable.Utils;
import com.blala.blalable.keyboard.KeyBoardConstant;

import java.util.Locale;

import timber.log.Timber;

/**
 * 功能：通知栏信息监听服务
 * 监听各个App在通知栏推送的信息
 * 1.别重写super,  否则NotificationListenerService不兼容4.3,报抽象错误, 具体原因看源码
 * 2.别重写onBind(), 否则服务不会创建 除非你super.onBind()
 */

public class SNNotificationService extends NotificationListenerService {

    int other = 2;

    /**微信**/
    private static final String WX_PACK_NAME = "com.tencent.mm";
    /**QQ**/
    private static final String QQ_PACK_NAME = "com.tencent.mobileqq";
    /**Discord**/
    private static final String DISCORD_PACK_NAME = "com.discord";

    @Override
    public IBinder onBind(Intent intent) {
        return super.onBind(intent);
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }


    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        try {
            if (sbn == null) return;

            Notification notification = sbn.getNotification();
            Timber.e("-------notification="+notification.toString());
            if (notification == null) return;


            String packName = sbn.getPackageName();
            String title = null;
            String content = null;
//            TLog.Companion.error("packName=="+packName);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && !packName.equals("com.app.smartkeyboard")) {
                Bundle extras = notification.extras;
//                TLog.Companion.error("extras+="+extras.toString());
                if (extras != null) {
                    try {
                        title = extras.getString(Notification.EXTRA_TITLE, null);
                        content = extras.getString(Notification.EXTRA_TEXT, null);
//                        TLog.Companion.error("title+="+title);
//                        TLog.Companion.error("content+="+content);
                        if (BikeUtils.isEmpty(content)) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                content = extras.getString(Notification.EXTRA_BIG_TEXT, null);
                            }
                        }



                    } catch (Exception ignored) {
                        ignored.printStackTrace();
                    }
                    //小米推送
//                    if (PermissionUtils.isPhone(PermissionUtils.MANUFACTURER_XIAOMI)) {
//                        if (SystemUtil.isMIUI12()) {
//                            try {
//                                ApplicationInfo info = (ApplicationInfo) extras.get("android.appInfo");
//                                packName = info.packageName;
//                            } catch (Exception e) {
//                                e.printStackTrace();
//                            }
//                        } else if (extras.containsKey("target_package") && !TextUtils.isEmpty(extras.getCharSequence("target_package", null))) {
//                            packName = extras.getCharSequence("target_package", null).toString();
//                        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
//                            String group = notification.getGroup();
//                            if (!TextUtils.isEmpty(group)) {
//                                packName = group;
//                            }
//                        }
//                    }
                }
                if (BikeUtils.isEmpty(content) && !BikeUtils.isEmpty((String) notification.tickerText)) {
                    content = notification.tickerText.toString();
                }
                Timber.e("--------SNNotificationService="+packName+"标题= "+title+" 内容="+content);
//                TLog.Companion.error("SNNotificationService+=" + packName + " content=" + content);
//                //   BleWrite.writeMessageCall(1, "测试", "测试内容",this);
//                TLog.Companion.error("SNNotificationService TYPE_NOTIFICATION_LISTENER_SERVICE");
//                SNNotificationPushHelper.getInstance().handleMessage(SNNotificationPushHelper.TYPE_NOTIFICATION_LISTENER_SERVICE, packName, title, content, this);

                String str = "title:"+title+" content:"+content+"\n"+"包名:"+packName+"\n";
                sendBroad(str);
                //微信
                if(packName.toLowerCase(Locale.ROOT).equals(WX_PACK_NAME)){
                    boolean isOpen = (boolean) MmkvUtils.getSaveParams(MmkvUtils.WX_KEY,false);
                    if(!isOpen){
                       return;
                    }
                    sendApps(0x05,title,content);
                }
                if(packName.toLowerCase(Locale.ROOT).equals(QQ_PACK_NAME)){
                    boolean isOpen = (boolean) MmkvUtils.getSaveParams(MmkvUtils.QQ_KEY,false);
                    if(!isOpen){
                        return;
                    }
                    sendApps(0x09,title,content);
                }
                if(packName.toLowerCase(Locale.ROOT).equals(DISCORD_PACK_NAME)){
                    boolean isOpen = (boolean) MmkvUtils.getSaveParams(MmkvUtils.DISCORD_KEY,false);
                    if(!isOpen){
                        return;
                    }
                    sendApps(0x0D,title,content);
                }

            }


        } catch (Exception ignored) {
            ignored.printStackTrace();
            //try住防止解析闪退 导致服务挂掉
        }

    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {

    }


    @Override
    public void onDestroy() {
//        TLog.Companion.error("通知监听:NotificationService#onDestroy");
        super.onDestroy();
    }



    private void sendApps(int type,String title,String content){
        Timber.e("-------发送app数据="+type+" title="+title+" content="+content);

        String st = "连接状态:"+BaseApplication.getBaseApplication().getConnStatus()+"\n"+"指令:"+ Utils.formatBtArrayToString(KeyBoardConstant.getMsgNotifyData(type, title, content));
        sendBroad(st);
        BaseApplication.getBaseApplication().getBleOperate().sendNotifyMsgData(type,title,content);

    }


    private void sendBroad(String content){
        Intent intent = new Intent();
        intent.setAction("com.app.freya.ble.notify");
        intent.putExtra("notify",content);
        this.sendBroadcast(intent);
    }

}
