package com.app.freya.second

import android.graphics.Color
import android.util.DisplayMetrics
import android.view.Gravity
import android.widget.TextView
import com.app.freya.BaseApplication
import com.app.freya.R
import com.app.freya.action.TitleBarFragment
import com.app.freya.ble.ConnStatus

import com.app.freya.dialog.DeleteDeviceDialog
import com.app.freya.utils.MmkvUtils
import com.hjq.shape.layout.ShapeConstraintLayout
import com.hjq.shape.view.ShapeTextView

/**
 * 设备页面
 */
class MenuDeviceFragment : TitleBarFragment<SecondHomeActivity>(){

    //设备名称
    private var deviceDeviceNameTv : TextView ?= null

    //连接状态
    private var secondMenuDeviceConnStateTv : TextView ?= null

    private var menuDeviceAboutAppLayout : ShapeConstraintLayout ?= null


    private var menuDeviceAboutAppVersionTv :TextView?=null


    companion object{

        fun getInstance() : MenuDeviceFragment{
            return MenuDeviceFragment()
        }
    }

    override fun getLayoutId(): Int {
       return R.layout.fragment_menu_device_layout
    }

    override fun initView() {
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
    }

    override fun initData() {

        attachActivity.setOnStateListener{
            showConnState()
        }

        try {
            val packManager = attachActivity.packageManager
            val packInfo = packManager.getPackageInfo(attachActivity.packageName,0)
            val versioiName = packInfo.versionName
            menuDeviceAboutAppVersionTv?.text = versioiName
        }catch (e : Exception){
            e.printStackTrace()
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
    }


    private fun showUnBindDialog(isUnBind : Boolean){
        val dialog = DeleteDeviceDialog(attachActivity, com.bonlala.base.R.style.BaseDialogTheme)
        dialog.show()
        if(!isUnBind){
            dialog.setTitleTxt("是否恢复出厂设置?")
            dialog.setConfirmBgColor(Color.parseColor("#16AEA0"))
        }
        dialog.setOnCommClickListener { position ->
            dialog.dismiss()
            if (position == 0x01) {   //解绑
                if(isUnBind){
                    BaseApplication.getBaseApplication().bleOperate.disConnYakDevice()
                    MmkvUtils.saveConnDeviceName("")
                    MmkvUtils.saveConnDeviceMac("")
                    attachActivity.showIsAddDevice()
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
}