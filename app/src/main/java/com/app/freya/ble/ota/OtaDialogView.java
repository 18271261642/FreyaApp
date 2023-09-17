package com.app.freya.ble.ota;

import static android.bluetooth.BluetoothGatt.GATT_SUCCESS;
import static android.content.ContentValues.TAG;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialog;
import com.app.freya.BaseApplication;
import com.app.freya.R;
import com.app.freya.utils.BikeUtils;
import com.app.freya.widget.CusScheduleView;
import com.hjq.shape.view.ShapeTextView;
import com.hjq.toast.ToastUtils;
import com.inuker.bluetooth.library.search.SearchResult;
import com.inuker.bluetooth.library.search.response.SearchResponse;
import com.liulishuo.filedownloader.BaseDownloadTask;
import com.liulishuo.filedownloader.FileDownloadListener;
import com.liulishuo.filedownloader.FileDownloader;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;
import timber.log.Timber;
public class OtaDialogView extends AppCompatDialog {

    //固件包下载的地址
    private String downloadFileUrl;
    //下载成功的文件
    private File sdFile;

    private TextView lastVersionTv;
    private TextView currentVersionTv;
    private SeekBar upgradeSeekBar;

    private TextView upgradeStateTv;
    private long leng;
    private final static int OTA_CMD_NVDS_TYPE = 0;
    private final static int OTA_CMD_REBOOT = 9;
    private final static int DEVICE_8010 = 0;
    private final static int DEVICE_8010H = 1;
    private final static int OTA_CMD_WRITE_DATA = 5;
    private final static int OTA_CMD_READ_DATA = 6;
    private final static int OTA_CMD_WRITE_MEM = 7;
    private final static int OTA_CMD_READ_MEM = 8;

    private final static String UUID_SERVICE_DATA_H = "02f00000-0000-0000-0000-00000000fe00";
    private final static String UUID_SEND_DATA_H = "02f00000-0000-0000-0000-00000000ff01";
    private final static String UUID_RECV_DATA_H = "02f00000-0000-0000-0000-00000000ff02";
    private final static String UUID_DES = "00002902-0000-1000-8000-00805f9b34fb";

    private BluetoothLeClass bleclass;
    private WriterOperation woperation;
    private boolean writeStatus = false;
    private BluetoothGattCharacteristic mgattCharacteristic = null;
    private BluetoothGattDescriptor descriptor = null;
    private final static int OTA_CMD_GET_STR_BASE = 1;
    private final static int OTA_CMD_PAGE_ERASE = 3;

    private InputStream input;
    private FileInputStream isfile = null;

    private int sencondaddr = 0x14000;
    private int firstaddr = 0;

    private int delay_num;
    private byte[] recvValue = null;
    private int recv_data;

    private int writePrecent;

    private ShapeTextView dialogUpgradeCancelTv;
    private CusScheduleView cusScheduleView;

    private String connMac;

    //是否是升级中的断开，升级中的断开不关闭弹窗
    private boolean isUpgradeDisConn = false;

    private final Handler handler = new Handler(Looper.myLooper()){
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            Timber.e("-------msg.what="+msg.what);
            if(msg.what == 0x88){  //停止扫描
                BaseApplication.getBaseApplication().getBleOperate().stopScanDevice();
            }

            if(msg.what == 0x81){
                upgradeStateTv.setText(getContext().getResources().getString(R.string.string_upgrade_success));
                dismiss();
            }

            if(msg.what == 0){
                try {
                    Timber.e("---------写入成功");
                    isUpgradeDisConn = true;
                    upgradeStateTv.setText(getContext().getResources().getString(R.string.string_upgrade_success));

                    bleclass.disconnect();
                    upgradeStateTv.setText(getContext().getResources().getString(R.string.string_upgrade_restart));
                    BaseApplication.getBaseApplication().getConnStatusService().autoConnDevice(connMac,false,true);
                    handler.sendEmptyMessageDelayed(0x81,4000);
                }catch (Exception e){
                    e.printStackTrace();
                }

            }

            if(msg.what == 1){  //发送进度
                Timber.e("-------发送进度="+writePrecent);
                if(upgradeSeekBar != null){
                    upgradeSeekBar.setProgress(writePrecent);
                    cusScheduleView.setCurrScheduleValue(writePrecent);
                    upgradeStateTv.setText(getContext().getResources().getString(R.string.string_upgrading)+":"+writePrecent+"%");
                }
            }

            if(msg.what == 5){  //找到端口
               // upgradeStateTv.setText("找到端口");
            }

            if(msg.what == 0x06){   //使能成功
              //  upgradeStateTv.setText("使能成功");
               handler.sendEmptyMessageDelayed(0x66,1000);

            }


            if(msg.what == 7){  //断开连接
                if(bleclass != null){
                    bleclass.disconnect();
                }

                if(!isUpgradeDisConn){
                    upgradeStateTv.setText(getContext().getResources().getString(R.string.string_upgrade_failed)+7);
                    ToastUtils.show(getContext().getResources().getString(R.string.string_upgrade_failed)+7);

                    dismiss();
                }


            }
            if(msg.what == 8){  //未找到对应的ota端口
                upgradeStateTv.setText(getContext().getResources().getString(R.string.string_upgrade_failed)+8);
                ToastUtils.show(getContext().getResources().getString(R.string.string_upgrade_failed)+8);
                if(bleclass != null){
                    bleclass.disconnect();
                }
                dismiss();
            }

            if(msg.what == 0x66){
                if(sdFile != null ){

                    File otaFile = sdFile;
                    startUpgrade(otaFile);
                }
            }
        }
    };

    public OtaDialogView(@NonNull Context context) {
        super(context);
    }

    public OtaDialogView(@NonNull Context context, int theme) {
        super(context, theme);
    }

    protected OtaDialogView(@NonNull Context context, boolean cancelable, @Nullable OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_ota_layout);
        initViews();
        initData();

    }


    private void initData(){

        downloadFileUrl = getContext().getExternalFilesDir(null).getPath()+"/";
        bleclass = new BluetoothLeClass(getContext());
        woperation = new WriterOperation();
        if(!bleclass.initialize()){
            dismiss();
        }
        bleclass.setOnConnectListener(mOnConnect);
        bleclass.setOnDisconnectListener(mOnDisconnect);
        bleclass.setOnServiceDiscoverListener(mOnServiceDiscover);
        bleclass.setOnRecvDataListener(mOnRecvData);
        bleclass.setOnWriteDataListener(mOnWriteData);
    }

    protected void initViews(){
        cusScheduleView = findViewById(R.id.cusScheduleView);
        dialogUpgradeCancelTv = findViewById(R.id.dialogUpgradeCancelTv);
        upgradeStateTv = findViewById(R.id.upgradeStateTv);
        lastVersionTv = findViewById(R.id.lastVersionTv);
        currentVersionTv = findViewById(R.id.currentVersionTv);
        upgradeSeekBar = findViewById(R.id.upgradeSeekBar);
        assert upgradeSeekBar != null;
        upgradeSeekBar.setMax(100);
        cusScheduleView.setAllScheduleValue(100);

        dialogUpgradeCancelTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
    }


    //设置当前版本和最新版本
    public void setVersions(String current,String lastVersion){
        lastVersionTv.setText(lastVersion);
        currentVersionTv.setText(current);
    }


    public void setStateShow(String txt){
        if(upgradeStateTv != null){
            upgradeStateTv.setText(txt);
        }

    }


    //显示或隐隐藏按钮
    public void visibilityOrGone(boolean isShow){
        dialogUpgradeCancelTv.setVisibility(isShow ? View.VISIBLE : View.GONE);
    }


    //开启扫描，断开后开启扫描，扫描到了使用ota的连接
    public void startScanDevice(String goalMac){
        this.connMac = goalMac;
        Timber.e("-------gomac="+goalMac);
        upgradeStateTv.setText(getContext().getResources().getString(R.string.string_upgrading)+"..");
        BaseApplication.getBaseApplication().getBleOperate().scanBleDevice(new SearchResponse() {
            @Override
            public void onSearchStarted() {

            }

            @Override
            public void onDeviceFounded(SearchResult searchResult) {
                String mac = searchResult.getAddress();
                Timber.e("--------扫描的mac="+mac);
                if(!BikeUtils.isEmpty(mac) && mac.equals(goalMac)){
                    Timber.e("---------扫描到了mac="+mac);
                   // upgradeStateTv.setText("扫描到了目标设备，开始连接");
                   boolean isConn =  bleclass.connect(mac);
                    Timber.e("------连接状态="+isConn);

                    handler.sendEmptyMessageDelayed(0x88,500);
                    return;
                }
            }

            @Override
            public void onSearchStopped() {

            }

            @Override
            public void onSearchCanceled() {

            }
        },15 * 1000,1);
    }


    private final BluetoothLeClass.OnConnectListener mOnConnect = new BluetoothLeClass.OnConnectListener() {
        @Override
        public void onConnect(BluetoothGatt gatt) {
            Timber.e("-----onConn="+gatt.getDevice().getAddress());
            //3秒超时 , 重连? myhandler.sendEmptyMessageDelayed(3,3000);
            gatt.discoverServices();
        }
    };

    private final BluetoothLeClass.OnDisconnectListener mOnDisconnect = new BluetoothLeClass.OnDisconnectListener() {
        @Override
        public void onDisconnect(BluetoothGatt gatt) {
            handler.sendEmptyMessage(7);//断线
        }
    };


    private final BluetoothLeClass.OnServiceDiscoverListener mOnServiceDiscover = new BluetoothLeClass.OnServiceDiscoverListener() {
        @Override
        public void onServiceDiscover(BluetoothGatt gatt, int status) {
            if(status == GATT_SUCCESS){
                UUID UUID_SERVICE_H = UUID.fromString(UUID_SERVICE_DATA_H);
                UUID UUID_SEND_H = UUID.fromString(UUID_SEND_DATA_H);
                UUID UUID_RECV_H = UUID.fromString(UUID_RECV_DATA_H);
                try{
                    BluetoothGattCharacteristic gattCharacteristic = gatt.getService(UUID_SERVICE_H).getCharacteristic(UUID_SEND_H);
                    Log.d(TAG,"GATT uuid:"+gattCharacteristic.getUuid());
                    String uuidString = gattCharacteristic.getUuid().toString();
                    if(uuidString.equals(UUID_SEND_DATA_H)){
                        //setTitle("找到端口");
                        mgattCharacteristic = gattCharacteristic;
                        handler.sendEmptyMessage(5);//找到端口
                    }
                    gattCharacteristic = gatt.getService(UUID_SERVICE_H).getCharacteristic(UUID_RECV_H);
                    uuidString = gattCharacteristic.getUuid().toString();
                    if(uuidString.equals(UUID_RECV_DATA_H)){

                        descriptor = gattCharacteristic.getDescriptor(UUID.fromString(UUID_DES));
                        if (descriptor != null) {
                            bleclass.setCharacteristicNotification(gattCharacteristic, true);

                            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                            bleclass.writeDescriptor(descriptor);
                            handler.sendEmptyMessage(6);//使能成功
                        }
                    }
                } catch(Exception e) {
                    e.printStackTrace();
                    handler.sendEmptyMessage(8);//未找到UUID
                }
            }
        }
    };


    private final BluetoothLeClass.OnRecvDataListerner mOnRecvData = new BluetoothLeClass.OnRecvDataListerner() {

        @Override
        public void OnCharacteristicRecv(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic) {
            recvValue = characteristic.getValue();
            setRecv_data(1);
        }
    };

    private final BluetoothLeClass.OnWriteDataListener mOnWriteData = new BluetoothLeClass.OnWriteDataListener() {

        @Override
        public void OnCharacteristicWrite(BluetoothGatt gatt,
                                          BluetoothGattCharacteristic characteristic, int status) {
            //System.out.println("status " + status);
            if(status == 0){
                writeStatus = true;
            }
        }

    };

    public int getRecv_data() {
        return recv_data;
    }

    public void setRecv_data(int recv_data) {
        this.recv_data = recv_data;
    }


    //下载文件
    public void downloadFile(String downUrl,String fileName,String mac){
        isUpgradeDisConn = false;
        sdFile = null;
        upgradeStateTv.setText(getContext().getResources().getString(R.string.string_start_download));
        upgradeStateTv.setText(getContext().getResources().getString(R.string.string_downloading)+"..");
        visibilityOrGone(false);

        Timber.e("-----downloadUrl="+downUrl);

        FileDownloader.getImpl().create(downUrl)
                .setPath(downloadFileUrl+fileName)
                .setListener(new FileDownloadListener() {
                    @Override
                    protected void pending(BaseDownloadTask task, int soFarBytes, int totalBytes) {
                        Timber.e("-------pending=");
                    }

                    @Override
                    protected void connected(BaseDownloadTask task, String etag, boolean isContinue, int soFarBytes, int totalBytes) {
                        Timber.e("-------connected=");
                    }

                    @Override
                    protected void progress(BaseDownloadTask task, int soFarBytes, int totalBytes) {
                        Timber.e("-------progress="+soFarBytes+" "+totalBytes);
                    }

                    @Override
                    protected void blockComplete(BaseDownloadTask task) {
                        Timber.e("-------blockComplete=");
                    }

                    @Override
                    protected void retry(final BaseDownloadTask task, final Throwable ex, final int retryingTimes, final int soFarBytes) {
                        Timber.e("-------retry=");
                    }

                    @Override
                    protected void completed(BaseDownloadTask task) {
                        Timber.e("-------completed=");
                        upgradeStateTv.setText(getContext().getString(R.string.string_upgrading)+"..");
                        String p = task.getTargetFilePath();
                        Timber.e("---p="+p);
                        File file = new File(p);

                        sdFile = file;
                        startScanDevice(mac);
                    }

                    @Override
                    protected void paused(BaseDownloadTask task, int soFarBytes, int totalBytes) {
                    }

                    @Override
                    protected void error(BaseDownloadTask task, Throwable e) {
                        Timber.e("-------error="+e.getMessage());
                        upgradeStateTv.setText("固件包下载失败"+e.getMessage());
                        visibilityOrGone(true);
                        sdFile = null;
                    }

                    @Override
                    protected void warn(BaseDownloadTask task) {
                        Timber.e("-------warn="+task.getTargetFilePath());
                    }
                }).start();




//        EasyHttp.download(this).url(downUrl)
//                .file(downloadFileUrl+fileName)
//                .listener(new OnDownloadListener() {
//                    @Override
//                    public void onDownloadProgressChange(File file, int progress) {
//
//                    }
//
//                    @Override
//                    public void onDownloadSuccess(File file) {
//                        upgradeStateTv.setText(getContext().getString(R.string.string_upgrading)+"..");
//                        sdFile = file;
//                        startScanDevice(mac);
//                    }
//
//                    @Override
//                    public void onDownloadFail(File file, Exception e) {
//                        upgradeStateTv.setText("固件包下载失败"+e.getMessage());
//                        visibilityOrGone(true);
//                        Timber.e("----onError-----=%s", e.getMessage());
//                        sdFile = null;
//                    }
//
//                }).start();

    }


    //开始升级
    protected void startUpgrade(File file){
        Timber.e("-------开始升级=%s",file.getPath());
       // upgradeStateTv.setText("开始升级"+bleclass.isDisconnected);
        if(bleclass.isDisconnected) {
            Toast.makeText(getContext(), "未连接", Toast.LENGTH_SHORT).show();
            return;
        }
        Timber.e("-------开始升级=file.exists()"+file.exists());
        if(!file.exists()){
            return;
        }
        bleclass.mtuChange = false;

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    doSendFileByBluetooth(file.getPath());
                } catch (FileNotFoundException e) {
                    throw new RuntimeException(e);
                }
            }
        }).start();
    }





    public void doSendFileByBluetooth(String filePath)
            throws FileNotFoundException {
        if (filePath != null) {
            int read_count;
            int i = 0;
            int addr;
            int lastReadCount = 0;
            int packageSize = 235;//bleclass.mtuSize - 3; //235;
            int send_data_count = 0;
            int deviceType;

            File file = new File(filePath);// 成文件路径中获取文件
            isfile = new FileInputStream(file);
            leng = file.length();
            input = new BufferedInputStream(isfile);
            //crc 校验
            int fileCRC=0;
            try {
                fileCRC = OtaUtils.getCRC32new(filePath);
            } catch (Exception e) {
                e.printStackTrace();
            }
            //Log.d("TAG CRC",Integer.toHexString(fileCRC));
            setRecv_data(0);
            woperation.send_data(OTA_CMD_NVDS_TYPE, 0, null, 0,
                    mgattCharacteristic, bleclass);
            while (getRecv_data() != 1){
                if(checkDisconnect()){
                    return;
                }
            }
            if ((woperation.bytetochar(recvValue) & 0x10) == 0) {
                deviceType = DEVICE_8010;
                bleclass.requestMtu(247);
            } else {
                deviceType = DEVICE_8010H;
                bleclass.requestMtu(512);

            }
            while(bleclass.mtuChange == false){
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                System.out.println("mtuChange " + bleclass.mtuChange);
            }
            packageSize = bleclass.mtuSize - 3 - 9;
            byte[] inputBuffer = new byte[packageSize];
            setRecv_data(0);
            woperation.send_data(OTA_CMD_GET_STR_BASE, 0, null, 0,
                    mgattCharacteristic, bleclass);
            while (getRecv_data() != 1){
                if(checkDisconnect()){
                    return;
                }
            }
            if(deviceType == DEVICE_8010){
                if (woperation.bytetoint(recvValue) == firstaddr) {
                    addr = sencondaddr;
                } else {
                    addr = firstaddr;
                }
            }else if(deviceType == DEVICE_8010H){
                addr = woperation.bytetoint(recvValue);
            }else{
                return;
            }
            setRecv_data(0);
            page_erase(addr, leng, mgattCharacteristic, bleclass);

            try {
                while (((read_count = input.read(inputBuffer, 0, packageSize)) != -1)) {
//    					woperation.send_data(OTA_CMD_WRITE_DATA, addr, inputBuffer,
//    							read_count, mgattCharacteristic, bleclass);
                    //20201116 修改
                    while ( ! woperation.send_data(OTA_CMD_WRITE_DATA, addr, inputBuffer,
                            read_count, mgattCharacteristic, bleclass)) {
                        try{
                            Thread.sleep(50);
                            Log.d("TAG","send_data error");
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    delay_num =0;
                    while(!writeStatus) {
                        delay_num++;
                        if(delay_num % 8000 == 0) {
                            Log.d("TAG","send_data once more");
                            woperation.send_data(OTA_CMD_WRITE_DATA, addr, inputBuffer, read_count, mgattCharacteristic, bleclass);
                        }
                    }
                    writeStatus = false;
                    //for(delay_num = 0;delay_num < 10000;delay_num++);
                    addr += read_count;
                    lastReadCount = read_count;
                    send_data_count += read_count;
                    //System.out.println("times" + i + " " + read_count);
                    i ++;
                    if(writePrecent != (int)(((float)send_data_count / leng) * 100)) {
                        writePrecent = (int) (((float) send_data_count / leng) * 100);
                        handler.sendEmptyMessage(1);
                    }

                    while (getRecv_data() != 1){
                        if(checkDisconnect()){
                            return;
                        }
                    }
                    setRecv_data(0);
                }
                while(woperation.bytetoint(recvValue) != (addr - lastReadCount)){
                    if(checkDisconnect()){
                        return;
                    }
                }

                //woperation.send_data(OTA_CMD_REBOOT, 0, null, 0, mgattCharacteristic, bleclass);
                //crc 校验
                woperation.send_data_long(OTA_CMD_REBOOT, fileCRC, null, leng, mgattCharacteristic, bleclass);
                handler.sendEmptyMessage(0);
            } catch (IOException e) {
                e.printStackTrace();
            }

        } else {
            Toast.makeText(getContext(), "请选择要发送的文件!",
                    Toast.LENGTH_LONG).show();
        }
    }

    boolean checkDisconnect(){
        if(bleclass != null && bleclass.isDisconnected){
            handler.sendEmptyMessage(2);
            return true;
        }
        return false;
    }


    private int page_erase(int addr, long length, BluetoothGattCharacteristic mgattCharacteristic, BluetoothLeClass bleclass) {

        long count = length / 0x1000;
        if ((length % 0x1000) != 0) {
            count++;
        }
        for (int i = 0; i < count; i++) {
            while ( ! woperation.send_data(OTA_CMD_PAGE_ERASE, addr, null, 0, mgattCharacteristic, bleclass)) {
                try{
                    Thread.sleep(50);
                    Log.d("TAG","send_data error");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            delay_num =0;
            while(!writeStatus) {
                delay_num++;
                if(delay_num % 8000 == 0) {
                    Log.d("TAG","send_data once more");
                    woperation.send_data(OTA_CMD_PAGE_ERASE, addr, null, 0, mgattCharacteristic, bleclass);
                }
            }
            while (getRecv_data() != 1);
            setRecv_data(0);
            addr += 0x1000;
        }
        return 0;
    }
}
