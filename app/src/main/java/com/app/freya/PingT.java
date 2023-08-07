package com.app.freya;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.app.freya.action.AppActivity;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;

public class PingT extends AppActivity implements Runnable{


    private Handler handlers = new Handler(Looper.myLooper()){
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            pingTv.setText(stringBuffer.toString());

        }
    };


    private TextView pingTv;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_ping_layout;
    }

    @Override
    protected void initView() {
        pingTv = findViewById(R.id.pingTv);
    }

    private Thread thread1;
    @Override
    protected void initData() {
        thread1 = new Thread(this);
        thread1.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }

    private StringBuffer stringBuffer  = new StringBuffer();


    private void startPing(){
        stringBuffer.delete(0,stringBuffer.length());
        try {
            String address = "47.106.139.220";
            new Thread(new Runnable() {
                @Override
                public void run() {

                    Process process = null;
                    try {
                        process = Runtime.getRuntime().exec("ping "+address);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    InputStreamReader r = new InputStreamReader(process.getInputStream());
                    LineNumberReader returnData = new LineNumberReader(r);
                    String returnMsg="";
                    String line = "";
                    while (true) {
                        try {
                            if (!((line = returnData.readLine()) != null)) break;
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                        System.out.println(line);
                        returnMsg += line;
                        stringBuffer.append(line);
                        handlers.sendEmptyMessageDelayed(0x00,300);
                    }

                    if(returnMsg.indexOf("100% loss")!=-1){
                        System.out.println("与 " +address +" 连接不畅通.");
                    }  else{
                        System.out.println("与 " +address +" 连接畅通.");
                    }
                }
            }).start();
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    @Override
    public void run() {
        startPing();
    }
}
