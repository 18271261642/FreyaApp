package com.blala.blalable;


import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.Toolbar;

import com.blala.blalable.bean.WeatherBean;
import com.blala.blalable.blebean.AlarmBean;
import com.blala.blalable.blebean.CommBleSetBean;
import com.blala.blalable.blebean.CommTimeBean;
import com.blala.blalable.keyboard.KeyBoardConstant;
import com.blala.blalable.listener.BleConnStatusListener;
import com.blala.blalable.listener.ConnStatusListener;
import com.blala.blalable.listener.OnBleStatusBackListener;
import com.blala.blalable.listener.OnCommBackDataListener;
import com.blala.blalable.listener.OnCommTimeSetListener;
import com.blala.blalable.listener.OnExerciseDataListener;
import com.blala.blalable.listener.OnKeyBoardListener;
import com.blala.blalable.listener.OnMeasureDataListener;
import com.blala.blalable.listener.OnRealTimeDataListener;
import com.blala.blalable.listener.OnSendWriteDataListener;
import com.blala.blalable.listener.OnSystemDataListener;
import com.blala.blalable.listener.OnWatchFaceVerifyListener;
import com.blala.blalable.listener.OnWriteProgressListener;
import com.blala.blalable.listener.WriteBack24HourDataListener;
import com.blala.blalable.listener.WriteBackDataListener;
import com.google.gson.Gson;
import com.inuker.bluetooth.library.search.response.SearchResponse;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Timer;

import androidx.annotation.NonNull;
import androidx.core.graphics.BitmapKt;


/**
 * Created by Admin
 * Date 2022/8/8
 */
public class BleOperateManager {

    private static final String TAG = "BleOperateManager";

    private static BleOperateManager bleOperateManager;

    private final BleManager bleManager = BleApplication.getInstance().getBleManager();

    private final BleConstant bleConstant = new BleConstant();

    private final KeyBoardConstant keyBoardConstant = new KeyBoardConstant();

    public static BleOperateManager getInstance() {
        if (bleOperateManager == null) {
            synchronized (BleOperateManager.class) {
                if (bleOperateManager == null)
                    bleOperateManager = new BleOperateManager();
            }
        }
        return bleOperateManager;
    }

    public BleOperateManager() {
    }

    private OnKeyBoardListener keyBoardListener;


    private List<byte[]> detailDialList = new ArrayList<>();
    private int detailDialCount = 0;


    private final Handler handler = new Handler(Looper.myLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            if (msg.what == 0x00) {
                Log.e(TAG, "-----发送表盘index=" + dialCount + "  " + dialList.size());
                if (dialCount < dialList.size()) {
                    detailDialList.clear();
                    List<byte[]> indexData = dialList.get(dialCount);
                    detailDialList.addAll(indexData);
                    detailDialCount = 0;
                    dialCount++;
                    handler.sendEmptyMessageDelayed(0x01, 20);
                    // sendWriteKeyBoardData(indexData);
                } else { //发送完了
                    Log.e(TAG, "---------全部发送万了");
                    if (keyBoardListener != null) {
                        keyBoardListener.onSyncFlash(0x02);
                    }

                }

            }

            //每个4K包的分包发送
            if (msg.what == 0x01) {
                Log.e(TAG, "------4K包详细发送=" + detailDialCount + " " + detailDialList.size());
                if (detailDialCount < detailDialList.size()) {
                    byte[] detailData = detailDialList.get(detailDialCount);
                    detailDialCount++;
                    sendWriteKeyBoardData(detailData);
                } else {
                    //一个4K包里面的内容发送完了
                    Log.e(TAG, "---------一个4K包发送全部发送万了");
                    handler.sendEmptyMessageDelayed(0x00, 50);
                }
            }
        }
    };


    public String getLog() {
        return bleManager.getLog();
    }

    public void clearLog() {
        bleManager.clearLog();
    }



    //搜索
    public void scanBleDevice(SearchResponse searchResponse, int duration, int times) {
        bleManager.startScanBleDevice(searchResponse, duration, times);
    }

    //搜索
    public void scanBleDevice(SearchResponse searchResponse, boolean isScanClass, int duration, int times) {
        bleManager.startScanBleDevice(searchResponse, isScanClass, duration, times);
    }

    //停止搜索
    public void stopScanDevice() {
        bleManager.stopScan();
    }

    //设置连接状态监听，在连接之前设置
    public void setBleConnStatusListener(BleConnStatusListener bleConnStatusListener) {
        bleManager.setBleConnStatusListener(bleConnStatusListener);
    }


    /**
     * 恢复出厂设置
     */
    public void setRecyclerDevice(){
        bleManager.writeDataToDevice(Utils.getFullPackage(new byte[]{0x01, 0x01}), new WriteBackDataListener() {
            @Override
            public void backWriteData(byte[] data) {

            }
        });
    }


    public void setClearListener() {
        bleManager.setClearListener();
    }

    public void setClearExercisListener() {
        bleManager.setClearExercise();
    }

    //设置手表监听，用于监听音乐状态和查找手机
    public void setBleBackStatus(OnBleStatusBackListener onBleStatusBackListener) {
        bleManager.setOnBleBackListener(onBleStatusBackListener);
    }

    //连接
    public void connYakDevice(String bleName, String bleMac, ConnStatusListener connStatusListener) {
        bleManager.connBleDeviceByMac(bleMac, bleName, connStatusListener);
    }

    //断连连接
    public void disConnYakDevice() {
        bleManager.disConnDevice();
    }

    public void disConnNotRemoveMac() {
        bleManager.disConnDeviceNotRemoveMac();
    }

    //写通用的设置，直接写数据
    public void writeCommonByte(byte[] bytes, WriteBackDataListener writeBackDataListener) {
        bleManager.writeDataToDevice(bytes, writeBackDataListener);
    }


    //获取版本信息
    public void getDeviceVersionData(WriteBackDataListener writeBackDataListener) {
        bleManager.writeDataToDevice(bleConstant.getDeviceVersion(), writeBackDataListener);
    }

    //获取版本信息
    public void getDeviceVersionData(OnCommBackDataListener onCommBackDataListener) {
        bleManager.writeDataToDevice(Utils.getFullPackage(new byte[]{0x00, 0x01, 0x00}), new WriteBackDataListener() {
            @Override
            public void backWriteData(byte[] data) {
                //88 00 00 00 00 00 13 3e 00 02 c0 03   00 01 23   054373c2bd97ffffffffffff
                Log.e(TAG, "------获取版本=" + Utils.formatBtArrayToString(data));
                if (data.length > 19 && data[9] == 2) {
                    String numberStr = String.format("%02x",data[10])+String.format("%02x",data[11]);


                    //版本
                    int oneStr = data[12] & 0xff;
                    int secondStr = data[13] & 0xff;
                    int thirdStr = data[14] & 0xff;
                    String version = "V" + oneStr + "." + secondStr + "." + thirdStr;

                    //将16进制转换成10进制
                    int versionCode = Utils.getIntFromBytes((byte) 0x00,data[12],data[13],data[14]);

                    if (onCommBackDataListener != null) {
                        onCommBackDataListener.onStrDataBack(version,numberStr,versionCode+"");
                        onCommBackDataListener.onIntDataBack(new int[]{versionCode});
                    }

                }

            }
        });
    }



    private final StringBuilder setBuilder = new StringBuilder();


    /**
     * 将back清空
     **/
    public void setDay24HourClear() {
        bleManager.clearListener();
    }



    public void getDeviceSystemData(OnSystemDataListener onSystemDataListener){
        bleManager.writeDataToDevice(Utils.getFullPackage(keyBoardConstant.getSysData()), new WriteBackDataListener() {
            @Override
            public void backWriteData(byte[] data) {
                Log.e(TAG,"---------获取系统信息="+Utils.formatBtArrayToString(data));
                if(data.length> 14 && (data[9] & 0xff) == 24){
                    //风扇转速
                    int circleSpeed = Utils.getIntFromBytes(data[13],data[12],data[11],data[10]);
                    //电量
                    int battery = data[14] & 0xff;

                    if(onSystemDataListener != null){
                        onSystemDataListener.onSysData(circleSpeed,battery,0,0,0,0,0,0);
                    }
                }

            }
        });
    }


    /**
     * 同步键盘的时间
     */
    public void syncKeyBoardTime() {

        byte[] timeByte = bleConstant.syncTime();
        byte[] resultData = Utils.getFullPackage(timeByte);
        bleManager.writeDataToDevice(resultData, writeBackDataListener);

    }

    /**
     * 同步键盘的时间
     */
    public void syncKeyBoardTime(WriteBackDataListener writeBackDataListener) {

        byte[] timeByte = bleConstant.syncTime();
        byte[] resultData = Utils.getFullPackage(timeByte);
        Log.e(TAG,"---------同步键盘时间="+Utils.formatBtArrayToString(resultData));
        bleManager.writeDataToDevice(resultData, writeBackDataListener);

    }


    /**
     * 设置设备基本信息，连接成功后就设置
     */
    public void setFirstDeviceInfo(boolean isChinese, WriteBackDataListener writeBackDataListener) {
        byte[] data = KeyBoardConstant.deviceInfoData(isChinese);

        byte[] resultData = Utils.getFullPackage(data);

        bleManager.writeDataToDevice(resultData, writeBackDataListener);
    }


    /**
     * 消息推送，目前只推送微信
     */
    public void sendNotifyMsgData(int key, String title, String content) {
        bleManager.writeDataToDevice(KeyBoardConstant.getMsgNotifyData(key, title, content));
    }


    //连接成功第一步获取设备的状态
    public void getKeyBoardStatus() {
        byte[] btArray = new byte[]{0x00, 0x13, 0x00};
        byte[] statusArray = Utils.getFullPackage(btArray);

        bleManager.writeDataToDevice(statusArray, new WriteBackDataListener() {
            @Override
            public void backWriteData(byte[] data) {
                Log.e(TAG, "-----状态返回=" + Utils.formatBtArrayToString(data) + " " + (data[10]));
                //88 00 00 00 00 00 06 14 00 14 01 00 01 00
                //88 00 00 00 00 00 06 14 00 14 01 00 01 00
                /**
                 *
                 */

                if (data.length == 14 && data[6] == 6 && data[9] == 20 && (data[13] == 0)) {
                    sendKeyBoardScreen(1);
                }

                if (data.length == 14 && data[6] == 6 && data[9] == 20 && (data[13] == 1)) {
                    sendKeyBoardScreen(1);
                }

                if (data.length == 14 && data[6] == 6 && data[9] == 20 && data[10] == 4) {
                    sendKeyBoardScreen(2);
                }

            }
        });
    }


    /**
     * 获取状态是否是2，不是2根据不同状态设置状态
     */

    public void getKeyBoardStatus(OnCommBackDataListener onCommBackDataListener) {
        byte[] btArray = new byte[]{0x00, 0x13, 0x00};
        byte[] statusArray = Utils.getFullPackage(btArray);

        bleManager.writeDataToDevice(statusArray, new WriteBackDataListener() {
            @Override
            public void backWriteData(byte[] data) {
                Log.e(TAG, "----222-状态返回=" + Utils.formatBtArrayToString(data));
                //88 00 00 00 00 00 06 14 00 14 01 00 01 00
                //88 00 00 00 00 00 06 16 00 14 01 00 01 02
                //88 00 00 00 00 00 06 16 00 14 01 00 01 02
//                if (data.length == 14 && data[6] == 6 && data[9] == 20 && (data[10] == 2 )){
//                    onCommBackDataListener.onIntDataBack(new int[]{88});
//                }

                if (data.length == 14 && data[6] == 6 && data[9] == 20) {
                    int code = data[13] & 0xff;
                    onCommBackDataListener.onIntDataBack(new int[]{code});
                    BleOperateManager.getInstance().setClearListener();
                }

//                if(data.length == 14 && data[6] == 6 && data[9] == 20 && (data[13] == 3 )){
//                    onCommBackDataListener.onIntDataBack(new int[]{3});
//                    BleOperateManager.getInstance().setClearListener();
//                }else{
//                    onCommBackDataListener.onIntDataBack(new int[]{0});
//                    BleOperateManager.getInstance().setClearListener();
//                }


//                if (data.length == 14 && data[6] == 6 && data[9] == 20 && (data[13] == 0 )) {
//                    new Handler().postDelayed(new Runnable() {
//                        @Override
//                        public void run() {
//                            getKeyBoardStatus();
//                        }
//                    },3000);
//                }
//
//                if (data.length == 14 && data[6] == 6 && data[9] == 20 && (data[13] == 1 )) {
//                    new Handler().postDelayed(new Runnable() {
//                        @Override
//                        public void run() {
//                            onCommBackDataListener.onIntDataBack(new int[]{88});
//                        }
//                    },2000);
//                }
//
//                if (data.length == 14 && data[6] == 6 && data[9] == 20 && data[10] == 4) {
//                    sendKeyBoardScreen(2);
//                    new Handler().postDelayed(new Runnable() {
//                        @Override
//                        public void run() {
//                            onCommBackDataListener.onIntDataBack(new int[]{88});
//                        }
//                    },2000);
//                }

            }
        });
    }


    //亮屏
    public void sendKeyBoardScreen(int status) {
        byte[] data = new byte[]{0x01, 0x1C, 0x01, 0x00, 0x01, (byte) status};
        byte[] logoArray = Utils.getFullPackage(data);

        bleManager.writeDataToDevice(logoArray, new WriteBackDataListener() {
            @Override
            public void backWriteData(byte[] data) {

            }
        });
    }

    /**
     * 将string类型的byte数组转成bytep[]
     *
     * @param str
     * @return
     */
    public static byte[] stringToByte(String str) {
        byte[] data = new byte[str.length() / 2];
        for (int i = 0; i < data.length; i++) {
            data[i] = Integer.valueOf(str.substring(0 + i * 2, 2 + i * 2), 16).byteValue();
        }
        return data;
    }


    //设置非固化表盘
    public void setLocalKeyBoardDial() {
        String str = "880000000000060009070000FFFE";
        byte[] array = Utils.hexStringToByte(str);
        bleManager.writeDataToDevice(array, new WriteBackDataListener() {
            @Override
            public void backWriteData(byte[] data) {

            }
        });

    }

    //发送记事本
    public void sendKeyBoardNoteBook(String title, String contentStr, Calendar noteCalendar) {
        Log.e(TAG,"--------内容="+contentStr);
        //标题
        String unitCode = Utils.getUnicode(title).replace("\\u", "");
        //内容 最大40个长度
        String tempContent = contentStr.length() > 100 ? contentStr.substring(0, 100) : contentStr;
        String contentUnitCode = (tempContent.length()==0) ? "00" : Utils.getUnicode(tempContent).replace("\\u", "");
        //  00 68 00 68 00 68 00 6a 00 6a 00 68

        byte[] titleArray = stringToByte(unitCode);

        Log.e(TAG,"-------contentUnitCode="+contentUnitCode.length());
        byte[] tempConArray = stringToByte(contentUnitCode);

        //  Log.e(TAG, "-------标题=" + tempConArray.length + "\n" + Utils.formatBtArrayToString(Utils.intToSecondByteArray(tempConArray.length)) + "\n" + Utils.formatBtArrayToString(titleArray));

        int year = noteCalendar.get(Calendar.YEAR);
        int month = noteCalendar.get(Calendar.MONTH) + 1;
        int day = noteCalendar.get(Calendar.DAY_OF_MONTH);
        int hour = noteCalendar.get(Calendar.HOUR_OF_DAY);
        int minute = noteCalendar.get(Calendar.MINUTE);
        int second = noteCalendar.get(Calendar.SECOND);

        //星期
        int week = noteCalendar.get(Calendar.DAY_OF_WEEK) - 1;
        Log.e(TAG, "---------week=" + week);

        byte[] timeArray = new byte[11];
        timeArray[0] = 0x01;
        timeArray[2] = 0x08;
        timeArray[3] = Utils.intToSecondByteArray(year)[1];
        timeArray[4] = Utils.intToSecondByteArray(year)[0];
        timeArray[5] = (byte) (month & 0xff);
        timeArray[6] = (byte) (day & 0xff);
        timeArray[7] = (byte) (hour & 0xff);
        timeArray[8] = (byte) (minute & 0xff);
        timeArray[9] = (byte) (second & 0xff);
        timeArray[10] = (byte) week;
        //时间
        String timeStr = Utils.getHexString(timeArray);

        Log.e(TAG, "------时间=" + timeStr);

        //内容

        int contentLength = titleArray.length;
        byte l1 = Utils.intToSecondByteArray(contentLength)[1];
        byte l2 = Utils.intToSecondByteArray(contentLength)[0];

        byte[] cBy = Utils.intToSecondByteArray(tempConArray.length);

        String conStr = "02" + String.format("%02x", l1) + String.format("%02x", l2) + Utils.getHexString(titleArray);
        String msgStr = "03" + String.format("%02x", cBy[1]) + String.format("%02x", cBy[0]) + Utils.getHexString(tempConArray);
        String resultStr = "040A" + timeStr+conStr;

        byte[] tempContentArray = Utils.hexStringToByte(resultStr);

        //88 00 00 00 00 00 1c d1
        // Log.e(TAG, "-------标题" + conStr + "\n" + msgStr);
        byte[] noteArray = Utils.getFullPackage(tempContentArray);


        String noteMsgStr = "040A"+msgStr;
        byte[] tempNoteMsgArray = Utils.hexStringToByte(noteMsgStr);
        byte[] noteMsgArray = Utils.getFullPackage(tempNoteMsgArray);

        bleManager.writeDataToDevice(noteArray, new WriteBackDataListener() {
            @Override
            public void backWriteData(byte[] data) {
                //88 00 00 00 00 00 05 0a 07 02 01 04 0a
                Log.e(TAG,"-------记事本返回="+Utils.formatBtArrayToString(data));
                if(data.length == 13 && (data[9] & 0xff) == 2 && data[11] == 4){
                    bleManager.writeDataToDevice(noteMsgArray, new WriteBackDataListener() {
                        @Override
                        public void backWriteData(byte[] data) {

                        }
                    });
                }
            }
        });
    }


    //第一步，开始写入表盘
    public void startFirstDial(byte[] data, WriteBackDataListener writeBackDataListener) {
        bleManager.writeDataToDevice(data, writeBackDataListener);
    }


    //APP 端设擦写设备端指定的 FLASH 数据块
    public void setIndexDialFlash(byte[] data, WriteBackDataListener writeBackDataListener) {
        bleManager.writeDataToDevice(data, writeBackDataListener);
    }


    //总的大小
    private int dialCount = 0;
    //表盘数据
    private List<List<byte[]>> dialList = new ArrayList<>();


    //开始写入表盘flash数据
    public void writeDialFlash(List<List<byte[]>> sourceList, OnKeyBoardListener onKeyBoardListener) {
        dialList.clear();
        dialList.addAll(sourceList);
        dialCount = 0;
        this.keyBoardListener = onKeyBoardListener;
        handler.sendEmptyMessageDelayed(0x00, 200);
    }


    private void sendWriteKeyBoardData(byte[] data) {
        bleManager.writeKeyBoardDialData(data, new WriteBackDataListener() {
            @Override
            public void backWriteData(byte[] data) {
                Log.e(TAG, "---------4K包里面的内容返回=" + Utils.formatBtArrayToString(data) + " " + (data[10]) + " " + (data[0] & 0xff));
//                handler.sendEmptyMessageDelayed(0x01,100);
                //4K包里面的内容返回  880000000000030c080602
                //                  88 00 00 00 00 00 03 0c 08 06 02
                /**
                 * 0x01：更新失败
                 * 0x02：更新成功
                 * 0x03：第 1 个 4K 数据块异常（含 APP 端发擦写和实际写入的数据地址不一致），APP 需要重走流程
                 * 0x04：非第 1 个 4K 数据块异常，需要重新发送当前 4K 数据块
                 * 0x05：4K 数据块正常，发送下一个 4K 数据
                 * 0x06：异常退出（含超时，或若干次 4K 数据错误，设备端处理）
                 */

                if (data.length == 11 && ((data[0] & 0xff) == 136) && data[8] == 8 && data[9] == 6) {
                    int code = data[10] & 0xff;

                    if (keyBoardListener != null) {
                        keyBoardListener.onSyncFlash(code);
                    }
                }

            }
        });
        handler.sendEmptyMessageDelayed(0x01, 20);
    }


    /**
     * 获取设备记事本信息
     */
    public void getDeviceNoteMsg(OnCommBackDataListener onCommBackDataListener){
        byte[] noteArray = new byte[]{0x00,0x15,0x00,0x00};
        byte[] resultArray =  Utils.getFullPackage(noteArray);
        bleManager.writeDataToDevice(resultArray, new WriteBackDataListener() {
            @Override
            public void backWriteData(byte[] data) {
                Log.e(TAG,"--------设备记事本信息="+Utils.formatBtArrayToString(data));

                //88 00 00 00 00 00 06 4f 00 16 2c 4e 7b 40
                if(data.length == 14 && (data[9]& 0xff) == 22){
                    //时间
                    long timeLong = Utils.getIntFromBytes(data[10],data[11],data[12],data[13]);

                    //转换成正常的时间，加上2000-01-01 00:00:00的时间戳
                    long realTime = 946656000L+timeLong;
                    Log.e(TAG,"-------时间="+timeLong+" "+realTime);
                    if(onCommBackDataListener != null){
                        String st = Utils.getFormatDateStr(realTime*1000, "yyyy-MM-dd HH:mm:ss");
                        onCommBackDataListener.onStrDataBack(st,String.valueOf(timeLong));
                    }
                }

            }
        });
    }


    public void deleteIndexNote(long time){
        byte[] indexArray = Utils.intToByteArray((int) time);
        byte[] noteArray = new byte[]{0x04,0x0B,indexArray[3],indexArray[2],indexArray[1],indexArray[0]};
        byte[] resultArray = Utils.getFullPackage(noteArray);
        bleManager.writeDataToDevice(resultArray, new WriteBackDataListener() {
            @Override
            public void backWriteData(byte[] data) {
                Log.e(TAG,"------删除记事本返回="+Utils.formatBtArrayToString(data));
            }
        });
    }








    private final WriteBackDataListener writeBackDataListener = new WriteBackDataListener() {
        @Override
        public void backWriteData(byte[] data) {

        }
    };


}
