package com.app.freya.ble

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.view.View
import android.widget.TextView
import com.app.freya.R
import com.app.freya.action.AppActivity
import com.blala.blalable.BleManager
import com.hjq.bar.OnTitleBarListener
import java.lang.StringBuilder

class NotifyLogActivity : AppActivity() {


    private var notifyLogTv : TextView ?= null



    override fun getLayoutId(): Int {
       return R.layout.activity_notify_log_layout
    }

    override fun initView() {
        notifyLogTv = findViewById(R.id.notifyLogTv)
      titleBar?.setOnTitleBarListener(object : OnTitleBarListener{
          override fun onLeftClick(view: View?) {

            finish()
          }

          override fun onTitleClick(view: View?) {

          }

          override fun onRightClick(view: View?) {
              stringBuffer.delete(0,stringBuffer.length)
              notifyLogTv?.text = ""
          }

      })
    }

    override fun initData() {
        val intentFilter = IntentFilter("com.app.freya.ble.notify")
        registerReceiver(broadcastReceiver,intentFilter)
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(broadcastReceiver)
    }


    private val stringBuffer = StringBuilder()


    private val broadcastReceiver : BroadcastReceiver = object : BroadcastReceiver(){
        override fun onReceive(p0: Context?, p1: Intent?) {
            val action = p1?.action
            if(action == "com.app.freya.ble.notify"){
                val str = p1.getStringExtra("notify")
                stringBuffer.append(str+"\n")
                notifyLogTv?.text = stringBuffer.toString()+BleManager.getInstance(this@NotifyLogActivity).log
            }
        }

    }
}