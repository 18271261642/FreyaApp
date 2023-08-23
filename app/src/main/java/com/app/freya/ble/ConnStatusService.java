package com.app.freya.ble;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;

import com.app.freya.BaseApplication;
import com.app.freya.bean.DbManager;
import com.app.freya.bean.NoteBookBean;
import com.app.freya.utils.BikeUtils;
import com.app.freya.utils.MmkvUtils;
import com.blala.blalable.BleConstant;
import com.blala.blalable.BleManager;
import com.blala.blalable.BleOperateManager;
import com.blala.blalable.Utils;
import com.blala.blalable.listener.BleConnStatusListener;
import com.blala.blalable.listener.ConnStatusListener;
import com.blala.blalable.listener.OnCommBackDataListener;
import com.blala.blalable.listener.WriteBackDataListener;
import com.inuker.bluetooth.library.BluetoothClient;
import com.inuker.bluetooth.library.Constants;
import com.inuker.bluetooth.library.connect.listener.BluetoothStateListener;
import com.inuker.bluetooth.library.search.SearchResult;
import com.inuker.bluetooth.library.search.response.SearchResponse;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Locale;
import java.util.Map;

import timber.log.Timber;


/**
 * Created by Admin
 * Date 2022/8/15
 * @author Admin
 */
public class ConnStatusService extends Service {


    public IBinder iBinder = new ConnBinder();


    private final Handler handler = new Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            if(msg.what == 0x08){
//                BaseApplication.getInstance().setConnStatus(ConnStatus.CONNECTED);

                sendActionBroad(BleConstant.BLE_24HOUR_SYNC_COMPLETE_ACTION);
            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BleConstant.BLE_CONNECTED_ACTION);
        intentFilter.addAction(BleConstant.BLE_DIS_CONNECT_ACTION);
        intentFilter.addAction(BleConstant.COMM_BROADCAST_ACTION);
        intentFilter.addAction(BleConstant.BLE_COMPLETE_EXERCISE_ACTION);

        intentFilter.addAction(BleConstant.BLE_SOURCE_DIS_CONNECTION_ACTION);
        intentFilter.addAction(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED);
        intentFilter.addAction(BluetoothDevice.ACTION_PAIRING_REQUEST);
        registerReceiver(broadcastReceiver,intentFilter);

        BluetoothClient bluetoothClient = BleManager.getInstance(this).getBluetoothClient();
        if(bluetoothClient != null){
            bluetoothClient.registerBluetoothStateListener(new BluetoothStateListener() {
                @Override
                public void onBluetoothStateChanged(boolean b) {
                    if(!b){
                        BaseApplication.getBaseApplication().setConnStatus(ConnStatus.NOT_CONNECTED);
                        sendActionBroad(BleConstant.BLE_DIS_CONNECT_ACTION);
                    }
                }
            });
        }
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return iBinder;
    }


    public class ConnBinder extends Binder{
        public ConnStatusService getService(){
            return ConnStatusService.this;
        }
    }




    //是否扫描到了
    private boolean isScanDevice = false;

    /**重新连接设备，扫描连接**/
    public void autoConnDevice(String mac,boolean isScanClass){
        BleOperateManager.getInstance().scanBleDevice(new SearchResponse() {
            @Override
            public void onSearchStarted() {
                Timber.e("----onSearchStarted--");
                isScanDevice = false;

                sendActionBroad(BleConstant.BLE_START_SCAN_ACTION,"");
            }

            @Override
            public void onDeviceFounded(SearchResult searchResult) {
                String bleName = searchResult.getName();
                if(TextUtils.isEmpty(bleName) || bleName.equals("NULL"))
                    return;
                if(searchResult.getAddress().equals(mac)){
                    BleOperateManager.getInstance().stopScanDevice();
                    Timber.e("-------扫描到了，开始连接="+mac+" "+(searchResult.getScanRecord() ==null));
                    if(searchResult.getScanRecord() != null){
                        Timber.e("-------ssss="+Utils.formatBtArrayToString(searchResult.getScanRecord()));
                    }
                    isScanDevice = true;
                    connDevice(bleName,mac,searchResult.getScanRecord());

                }
            }

            @Override
            public void onSearchStopped() {
//                Timber.e("----onSearchStopped--");
                if(!isScanDevice){
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            sendActionBroad(BleConstant.BLE_SCAN_COMPLETE_ACTION,"0");
                            if(BaseApplication.getBaseApplication().getConnStatus() != ConnStatus.CONNECTED){
                                BaseApplication.getBaseApplication().setConnStatus(ConnStatus.NOT_CONNECTED);
                            }
                        }
                    },3 *1000);

                }
            }

            @Override
            public void onSearchCanceled() {
//                Timber.e("----onSearchCanceled--");
            }
        },isScanClass,2000 * 1000,1);
    }


    private void setConnListener(){
        BleOperateManager.getInstance().setBleConnStatusListener(new BleConnStatusListener() {
            @Override
            public void onConnectStatusChanged(String mac, int status) {
//                Timber.e("------连接状态="+mac+" status="+status+" "+Constants.STATUS_DISCONNECTED);
                // Constants.STATUS_DISCONNECTED
                //连接失败
                if(status == Constants.STATUS_DISCONNECTED){
                    handler.removeMessages(0x08);

//                    sendActionBroad(BleConstant.BLE_DIS_CONNECT_ACTION,"");
//                    BleOperateManager.getInstance().disConnYakDevice();
                    new Handler(Looper.myLooper()).postDelayed(new Runnable() {
                        @Override
                        public void run() {
//                            autoConnDevice(mac,true);
                        }
                    },2 * 1000);

                }

            }
        });
    }

    public void connDeviceBack(String bleName, String mac, BleConnStatusListener bleConnStatusListener){

        setConnListener();
        BleOperateManager.getInstance().connYakDevice(bleName, mac, new ConnStatusListener() {
            @Override
            public void connStatus(int status) {

            }

            @Override
            public void setNoticeStatus(int code) {

                BaseApplication.getBaseApplication().setConnStatus(ConnStatus.CONNECTED);
//                Timber.e("-------连接成功="+code);
                MmkvUtils.saveConnDeviceMac(mac);
                MmkvUtils.saveConnDeviceName(bleName);

                //判断是否是心率带，心率带直接连接成功
                //同步时间
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        //BleOperateManager.getInstance().syncDeviceTime(writeBackDataListener);
                        if(bleConnStatusListener != null)
                            bleConnStatusListener.onConnectStatusChanged(mac,code);
                    }
                },1000);

               // getKeyBoardStatus(mac,code);

                setDeviceInfo(mac);
            }
        });
    }


    //连接成功设置设备信息
    private void setDeviceInfo(String mac){

        //同步时间
        BaseApplication.getBaseApplication().getBleOperate().syncKeyBoardTime(new WriteBackDataListener() {
            @Override
            public void backWriteData(byte[] data) {
                Timber.e("--------同步时间返回="+Utils.formatBtArrayToString(data));
                syncSet();

            }
        });


    }

    private void syncSet(){
        BaseApplication.getBaseApplication().getBleOperate().setFirstDeviceInfo(Utils.isZh(ConnStatusService.this), new WriteBackDataListener() {
            @Override
            public void backWriteData(byte[] data) {
                Timber.e("--------设置设备信息="+Utils.formatBtArrayToString(data));
                getKeyBoardStatus(null,0);
            }
        });
    }

    //获取最新一条的记事本，有的话


    /**获取设备的状态**/
    private void getKeyBoardStatus(String mac,int code){
       // BaseApplication.getBaseApplication().getBleOperate().getKeyBoardStatus();

        BaseApplication.getBaseApplication().getBleOperate().getDeviceNoteMsg(new OnCommBackDataListener() {
            @Override
            public void onIntDataBack(int[] value) {

            }

            @Override
            public void onStrDataBack(String... value) {
                //记事本的时间
                String timeStr = value[0];
                //键盘中的时间戳
                String deviceTime = value[1];
                if(!BikeUtils.isEmpty(timeStr)){
                    NoteBookBean nb = DbManager.getInstance().queryNoteBookByTime(timeStr);
                    if(nb == null){
                        if(!BikeUtils.isEmpty(deviceTime)){
                            long t = Long.parseLong(deviceTime);
                            BleOperateManager.getInstance().deleteIndexNote(t);
                        }
                    }
                }
            }
        });
    }



    //连接
    public void connDevice(String name,String bleMac,byte[] scanRecord){
        setConnListener();
        BleOperateManager.getInstance().connYakDevice(name, bleMac, new ConnStatusListener() {
            @Override
            public void connStatus(int status) {

            }

            @Override
            public void setNoticeStatus(int code) {
                Map<String,String> typeMap = BaseApplication.supportDeviceTypeMap;
                BaseApplication.getBaseApplication().setConnStatus(ConnStatus.CONNECTED);

                if(scanRecord != null){
                    String recordStr = Utils.formatBtArrayToString(scanRecord);
                    for(Map.Entry<String,String> m : typeMap.entrySet()){
                        String keyStr = m.getKey();
                        String tempK = Utils.changeStr(keyStr);
                        String scanRecord = recordStr.toLowerCase(Locale.CHINA);
                        boolean front = scanRecord.contains(keyStr.toLowerCase(Locale.CHINA));
                        boolean back = scanRecord.contains(tempK.toLowerCase(Locale.CHINA));
                        MmkvUtils.saveProductNumberCode(keyStr);

                    }
                }

//                Timber.e("-------连接成功="+code);
                MmkvUtils.saveConnDeviceMac(bleMac);
                MmkvUtils.saveConnDeviceName(name);
                sendActionBroad(BleConstant.BLE_CONNECTED_ACTION);
//                Timber.e("-------连接成功="+code);
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        //getKeyBoardStatus(bleMac,code);
                        setDeviceInfo(bleMac);
                    }
                },1000);

            }
        });
    }


    private final WriteBackDataListener writeBackDataListener = new WriteBackDataListener() {
        @Override
        public void backWriteData(byte[] data) {

            //设备回复: 02 FF 30 00
            //同步时间返回
            if(data.length == 4 && data[0] ==2 && (data[1] & 0xff) == 255 && (data[2] & 0xff) == 48){
            }
        }
    };



    private void sendActionBroad(String action,String...params){
        Intent intent = new Intent();
        intent.setAction(action);
        intent.putExtra("ble_key",params);
        sendBroadcast(intent);
    }


    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(action == null)
                return;

            //连接断开
            if(action.equals(BleConstant.BLE_SOURCE_DIS_CONNECTION_ACTION)){
                sendActionBroad(BleConstant.BLE_DIS_CONNECT_ACTION,"");
                BaseApplication.getBaseApplication().setConnStatus(ConnStatus.NOT_CONNECTED);
                //判断是否主动断开，主动断开无Mac地址
                String saveMac = MmkvUtils.getConnDeviceMac();
                Timber.e("----------锻炼联了=");
                if(BikeUtils.isEmpty(saveMac)){
                    return;
                }
                autoConnDevice(saveMac,true);
            }


            //配对
            if(action.equals(BluetoothDevice.ACTION_PAIRING_REQUEST)){
               // abortBroadcast();
            }

//
//            //连接断开
//            if(action.equals(BleConstant.BLE_DIS_CONNECT_ACTION)){
//
//                BaseApplication.getBaseApplication().setConnStatus(ConnStatus.NOT_CONNECTED);
//                //判断是否主动断开，主动断开无Mac地址
//                String saveMac = MmkvUtils.getConnDeviceMac();
//                Timber.e("----------锻炼联了=");
//                if(BikeUtils.isEmpty(saveMac)){
//                    return;
//                }
//                autoConnDevice(saveMac,true);
//            }

            if(action.equals(BleConstant.COMM_BROADCAST_ACTION)){
                int[] valueArray = intent.getIntArrayExtra(BleConstant.COMM_BROADCAST_KEY);
                if(valueArray[0] == BleConstant.MEASURE_COMPLETE_VALUE){
//                    DataOperateManager.getInstance(ConnStatusService.this).setMeasureDataSave(BleOperateManager.getInstance());
                }
            }

            //锻炼结束了，获取一次锻炼数据
            if(action.equals(BleConstant.BLE_COMPLETE_EXERCISE_ACTION)){
//                DataOperateManager.getInstance(ConnStatusService.this).getExerciseData();
            }
        }
    };
}
