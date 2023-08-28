package com.app.freya.second

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.util.DisplayMetrics
import android.view.Gravity
import android.widget.TextView
import androidx.activity.viewModels
import androidx.fragment.app.viewModels
import com.app.freya.BaseApplication
import com.app.freya.R
import com.app.freya.action.TitleBarFragment
import com.app.freya.ble.ConnStatus
import com.app.freya.ble.OnConnStateListener

import com.app.freya.dialog.DeleteDeviceDialog
import com.app.freya.dialog.UpgradeDialogView
import com.app.freya.utils.MmkvUtils
import com.app.freya.viewmodel.KeyBoardViewModel
import com.hjq.shape.layout.ShapeConstraintLayout
import com.hjq.shape.view.ShapeTextView
import com.hjq.toast.ToastUtils

/**
 * 设备页面
 */
class MenuDeviceFragment : TitleBarFragment<SecondHomeActivity>(){

    private val viewModel by viewModels<KeyBoardViewModel>()

    //设备名称
    private var deviceDeviceNameTv : TextView ?= null

    //连接状态
    private var secondMenuDeviceConnStateTv : TextView ?= null

    private var menuDeviceAboutAppLayout : ShapeConstraintLayout ?= null


    private var menuDeviceAboutAppVersionTv :TextView?=null

    //电量
    private var menuBatteryTv : TextView ?= null




    companion object{

        fun getInstance() : MenuDeviceFragment{
            return MenuDeviceFragment()
        }
    }

    override fun getLayoutId(): Int {
       return R.layout.fragment_menu_device_layout
    }

    override fun initView() {
        menuBatteryTv = findViewById(R.id.menuBatteryTv)
        menuDeviceAboutAppVersionTv = findViewById(R.id.menuDeviceAboutAppVersionTv)
        menuDeviceAboutAppLayout = findViewById(R.id.menuDeviceAboutAppLayout)
        secondMenuDeviceConnStateTv= findViewById(R.id.secondMenuDeviceConnStateTv)
        deviceDeviceNameTv = findViewById(R.id.deviceDeviceNameTv)
        findViewById<ShapeTextView>(R.id.deviceNotifyTv).setOnClickListener {
            startActivity(NotifyOpenActivity::class.java)
        }

        findViewById<ShapeTextView>(R.id.deviceUnBindTv).setOnClickListener {
            showUnBindDialog(true)
        }
        //关于设备
        findViewById<ShapeTextView>(R.id.deviceAboutTv).setOnClickListener {
            startActivity(AboutDeviceActivity::class.java)
        }
        //恢复出厂设置
        findViewById<ShapeTextView>(R.id.menuDeviceRecyclerLayout).setOnClickListener {
            showUnBindDialog(false)
        }




        secondMenuDeviceConnStateTv?.setOnClickListener {
            val connState = BaseApplication.getBaseApplication().connStatus
            if(connState == ConnStatus.CONNECTED || connState == ConnStatus.CONNECTING){
                return@setOnClickListener
            }
            attachActivity.retryConn()
        }
    }

    override fun initData() {


        viewModel.appVersionData.observe(this){
            if(it?.isError == false){
                showAppUpgradeDialog(it.ota)
            }else{
                ToastUtils.show(resources.getString(R.string.string_has_last_version))
            }
        }

        attachActivity.setOnStateListener{
            showConnState()
        }

        attachActivity.setOnConnStateListener(object : OnConnStateListener{
            override fun onConnState(connStatus: ConnStatus?) {
                secondMenuDeviceConnStateTv?.text =  if(connStatus == ConnStatus.CONNECTED) resources.getString(R.string.string_connected) else (if( connStatus == ConnStatus.CONNECTING) resources.getString(R.string.string_connecting) else resources.getString(R.string.string_retry_conn))
                if(connStatus == ConnStatus.CONNECTED){
                    getBattery()
                }else{
                    menuBatteryTv?.text = String.format(resources.getString(R.string.string_battery),"--")
                }
            }
        })

        try {
            val packManager = attachActivity.packageManager
            val packInfo = packManager.getPackageInfo(attachActivity.packageName,0)
            val versioiName = packInfo.versionName
            menuDeviceAboutAppVersionTv?.text = versioiName
        }catch (e : Exception){
            e.printStackTrace()
        }


        //app版本更新
        menuDeviceAboutAppLayout?.setOnClickListener {
            val packManager = attachActivity.packageManager
            val packInfo = packManager.getPackageInfo(attachActivity.packageName,0)
            viewModel.checkAppVersion(packInfo.versionCode)
        }
    }


    override fun onFragmentResume(first: Boolean) {
        super.onFragmentResume(first)
        showConnState()
    }

    private fun showConnState(){
        deviceDeviceNameTv?.text = MmkvUtils.getConnDeviceName()
        val isConnStatus = BaseApplication.getBaseApplication().connStatus
        secondMenuDeviceConnStateTv?.text =  if(isConnStatus == ConnStatus.CONNECTED) resources.getString(R.string.string_connected) else (if( isConnStatus == ConnStatus.CONNECTING) resources.getString(R.string.string_connecting) else resources.getString(R.string.string_retry_conn))
        if(isConnStatus == ConnStatus.CONNECTED){
            getBattery()
        }else{
            menuBatteryTv?.text =  String.format(resources.getString(R.string.string_battery),"--")
        }


    }


    //获取电量
    private fun getBattery(){
        BaseApplication.getBaseApplication().bleOperate.getDeviceSystemData { circleSpeed, batteryValue, cpuTemperatureC, cpuTemperatureF, gpuTemC, gpuTemF, hardTemC, hardTemF ->
            menuBatteryTv?.text = String.format(resources.getString(R.string.string_battery),batteryValue.toString()+"%")

        }
    }

    private fun showUnBindDialog(isUnBind : Boolean){
        val dialog = DeleteDeviceDialog(attachActivity, com.bonlala.base.R.style.BaseDialogTheme)
        dialog.show()
        if(!isUnBind){
            dialog.setTitleTxt(resources.getString(R.string.string_recycler_yes_or_not))
            dialog.setConfirmBgColor(Color.parseColor("#16AEA0"))
        }else{
            dialog.setTitleTxt(resources.getString(R.string.string_unbind_alert))
        }
        dialog.setOnCommClickListener { position ->
            dialog.dismiss()
            if (position == 0x01) {   //解绑
                if(isUnBind){

                    BaseApplication.getBaseApplication().bleOperate.disConnYakDevice()
                    MmkvUtils.saveConnDeviceName("")
                    MmkvUtils.saveConnDeviceMac("")
                    attachActivity.showIsAddDevice()
                }else{
                    BaseApplication.getBaseApplication().bleOperate.setRecyclerDevice()

                }

            }
        }

        val window = dialog.window
        val windowLayout = window?.attributes
        val metrics2: DisplayMetrics = resources.displayMetrics
        val widthW: Int = metrics2.widthPixels

        windowLayout?.width = widthW
        windowLayout?.gravity = Gravity.BOTTOM
        window?.attributes = windowLayout
    }


    private fun showAppUpgradeDialog(url : String){
        val upgradeDialogView = UpgradeDialogView(attachActivity, com.bonlala.base.R.style.BaseDialogTheme)
        upgradeDialogView.show()
        upgradeDialogView.setContentTxt(resources.getString(R.string.string_app_new_version))
        upgradeDialogView.setOnDialogClickListener{
            upgradeDialogView.dismiss()
            if(it == 0x01){
                val uri = Uri.parse(url)
                val intent = Intent(Intent.ACTION_VIEW,uri)
                attachActivity.startActivity(intent)
            }
        }

    }
}