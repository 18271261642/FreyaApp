package com.app.freya.second

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.DisplayMetrics
import android.view.View
import android.widget.TextView
import androidx.activity.viewModels
import com.app.freya.BaseApplication
import com.app.freya.R
import com.app.freya.action.AppActivity
import com.app.freya.ble.ConnStatus
import com.app.freya.ble.ota.OtaDialogView
import com.app.freya.dialog.UpgradeDialogView
import com.app.freya.utils.MmkvUtils
import com.app.freya.viewmodel.KeyBoardViewModel
import com.blala.blalable.BleConstant
import com.blala.blalable.listener.OnCommBackDataListener
import com.google.gson.Gson
import com.hjq.bar.OnTitleBarListener
import com.hjq.bar.TitleBar
import com.hjq.shape.layout.ShapeConstraintLayout
import com.hjq.toast.ToastUtils
import timber.log.Timber

class AboutDeviceActivity : AppActivity() {

    private val viewModel by viewModels<KeyBoardViewModel>()


    //名称
    private var aboutDeviceNameTv : TextView ?= null
    //型号
    private var aboutDeviceModelTv : TextView ?= null
    //版本
    private var aboutDeviceVersionTv : TextView ?= null


    private val handlers : Handler = object : Handler(Looper.getMainLooper()){
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            if(msg.what == 0x00){
                val bundle = msg.obj as Bundle
                val url = bundle.getString("url")
                val name = bundle.getString("name")
                val mac = bundle.getString("mac")

                if (url != null) {
                    showOtaDialog(url,name!!,mac!!)
                }
            }

            if(msg.what == 0x02){
                dialog?.dismiss()
            }
        }
    }

    override fun getLayoutId(): Int {
        return R.layout.activity_about_device_layout
    }




    override fun initView() {
        aboutDeviceNameTv = findViewById(R.id.aboutDeviceNameTv)
        aboutDeviceModelTv = findViewById(R.id.aboutDeviceModelTv)
        aboutDeviceVersionTv = findViewById(R.id.aboutDeviceVersionTv)

        findViewById<ShapeConstraintLayout>(R.id.aboutUpdateLayout).setOnClickListener {
            showVersion()
        }

    }


    private fun register(){
        val intentFilter = IntentFilter()
        intentFilter.addAction(BleConstant.BLE_CONNECTED_ACTION)
        intentFilter.addAction(BleConstant.BLE_DIS_CONNECT_ACTION)
        intentFilter.addAction(BleConstant.BLE_SCAN_COMPLETE_ACTION)
        intentFilter.addAction(BleConstant.BLE_START_SCAN_ACTION)
        registerReceiver(broadcastReceiver,intentFilter)
    }

    override fun initData() {
        register()

        viewModel.firmwareData.observe(this){
            Timber.e("------code="+it)
            if(it != null){
                if(it.isError){
                    ToastUtils.show(resources.getString(R.string.string_has_last_version))
                    BaseApplication.getBaseApplication().logStr = it.errorMsg
                }else{
                    BaseApplication.getBaseApplication().logStr = Gson().toJson(it)
                    showUpgradeDialog(it.ota,it.fileName)
                }
            }else{
                ToastUtils.show(resources.getString(R.string.string_has_last_version))
            }
        }

        val name = MmkvUtils.getConnDeviceName()
        aboutDeviceNameTv?.text = name
        aboutDeviceModelTv?.text = name

        showVersion()
    }


    val stringBuffer = StringBuilder()
    private fun showVersion(){
        stringBuffer.delete(0,stringBuffer.length)
        BaseApplication.getBaseApplication().bleOperate.getDeviceVersionData(object :
            OnCommBackDataListener {
            override fun onIntDataBack(value: IntArray?) {
                Timber.e("------版本好="+ (value?.get(0) ?: 0))
                val code = value?.get(0)
                stringBuffer.append(" "+code.toString()+" ")
                BaseApplication.getBaseApplication().setLogStr("VersionCode="+code.toString())
                if(code != null){
                    viewModel.checkVersion(this@AboutDeviceActivity,code)
                }
                aboutDeviceVersionTv?.text =  stringBuffer.toString()
            }

            override fun onStrDataBack(vararg value: String?) {
                stringBuffer.append(" "+value[0]+" ")
                aboutDeviceVersionTv?.text =  stringBuffer.toString()

            }

        })
    }


    private val broadcastReceiver : BroadcastReceiver = object : BroadcastReceiver(){
        override fun onReceive(p0: Context?, p1: Intent?) {
            val action = p1?.action
            Timber.e("---------acdtion="+action)
            if(action == BleConstant.BLE_CONNECTED_ACTION){
                ToastUtils.show(resources.getString(R.string.string_conn_success))
                BaseApplication.getBaseApplication().connStatus = ConnStatus.CONNECTED
                BaseApplication.getBaseApplication().bleOperate.stopScanDevice()
                showVersion()

                setDialogTxtShow(resources.getString(R.string.string_upgrade_success))
            }
            if(action == BleConstant.BLE_DIS_CONNECT_ACTION){
                ToastUtils.show(resources.getString(R.string.string_conn_disconn))
                showVersion()
            }

        }

    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            unregisterReceiver(broadcastReceiver)
        }catch (e : Exception){
            e.printStackTrace()
        }
    }



    private var upgradeDialogView : UpgradeDialogView?= null


    private fun showUpgradeDialog(url : String ,name : String){
        if(upgradeDialogView == null){
            upgradeDialogView = UpgradeDialogView(this, com.bonlala.base.R.style.BaseDialogTheme)
        }
        if(!upgradeDialogView!!.isShowing){
            upgradeDialogView?.show()
        }
        upgradeDialogView?.setOnDialogClickListener { position ->
            upgradeDialogView?.dismiss()
            if (position == 0x01) {
                val mac = MmkvUtils.getConnDeviceMac()
                MmkvUtils.saveConnDeviceMac(null)
                BaseApplication.getBaseApplication().bleOperate.disConnYakDevice()

                val msg = handlers.obtainMessage()
                val bundle = Bundle()
                bundle.putString("url", url)
                bundle.putString("name", name)
                bundle.putString("mac", mac)
                msg.what = 0x00
                msg.obj = bundle
                handlers.sendMessageDelayed(msg, 1000)
            }
        }


    }


    //设置弹窗显示的文字
    private fun setDialogTxtShow(txt : String){
        if(dialog != null && dialog!!.isShowing){
            dialog?.setStateShow(txt)
            dialog?.visibilityOrGone(false)
            handlers.sendEmptyMessageDelayed(0x02,3000)
        }
    }


    private var dialog : OtaDialogView?= null

    //显示升级的弹窗
    private fun showOtaDialog(url : String ,fileName :String,mac : String){
        if(dialog == null){
            dialog = OtaDialogView(this, com.bonlala.base.R.style.BaseDialogTheme)
        }

        dialog?.show()
        dialog?.downloadFile(url,fileName,mac)
        //  dialog?.startScanDevice(mac)

        val window = dialog?.window
        val windowLayout = window?.attributes
        val metrics2: DisplayMetrics = resources.displayMetrics
        val widthW: Int = (metrics2.widthPixels * 0.9f).toInt()
        val height : Int = (metrics2.heightPixels * 0.6f).toInt()
        windowLayout?.width = widthW
        windowLayout?.height = height
        window?.attributes = windowLayout
    }



}