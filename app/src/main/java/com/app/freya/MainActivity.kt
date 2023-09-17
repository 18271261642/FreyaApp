package com.app.freya


import android.Manifest
import android.app.Activity
import android.content.*
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.DisplayMetrics
import android.view.KeyEvent
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.viewModels
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationManagerCompat
import com.app.freya.action.ActivityManager
import com.app.freya.action.AppActivity
import com.app.freya.ble.ConnStatus
import com.app.freya.ble.ota.OtaDialogView
import com.app.freya.dialog.NoticeDialog
import com.app.freya.dialog.ShowPrivacyDialogView
import com.app.freya.dialog.UpgradeDialogView
import com.app.freya.second.SecondHomeActivity
import com.app.freya.utils.BikeUtils
import com.app.freya.utils.BonlalaUtils
import com.app.freya.utils.MmkvUtils
import com.app.freya.utils.NotificationUtils
import com.app.freya.viewmodel.KeyBoardViewModel
import com.blala.blalable.BleConstant
import com.blala.blalable.listener.OnCommBackDataListener
import com.google.gson.Gson
import com.hjq.permissions.XXPermissions
import com.hjq.toast.ToastUtils
import kotlinx.android.synthetic.main.dialog_show_upgrade_layout.*
import timber.log.Timber

/**
 * 首页面
 */
class MainActivity : AppActivity() {

    private val viewModel by viewModels<KeyBoardViewModel>()

    //记事本
    private var homeNotebookLayout: FrameLayout? = null

    //键盘页面
    private var homeKeyboardLayout: FrameLayout? = null

    private var homeDialLayout: FrameLayout? = null

    //APP版本
    private var appVersionTv : TextView ?= null
    //固件版本，连接成功后才显示，不连接䒑显示
    private var firmwareVersionTv : TextView ?= null


    override fun getLayoutId(): Int {
        return R.layout.activity_main
    }

    override fun initView() {
        firmwareVersionTv = findViewById(R.id.firmwareVersionTv)
        appVersionTv = findViewById(R.id.appVersionTv)
        homeNotebookLayout = findViewById(R.id.homeNotebookLayout)
        homeKeyboardLayout = findViewById(R.id.homeKeyboardLayout)
        homeDialLayout = findViewById(R.id.homeDialLayout)

        setOnClickListener(homeNotebookLayout, homeKeyboardLayout, homeDialLayout)


        findViewById<ImageView>(R.id.titleImgView)?.setOnClickListener {
            startActivity(LogActivity::class.java)
        }
        findViewById<ImageView>(R.id.titleImgView).setOnLongClickListener {
           // startActivity(LogActivity::class.java)
            startActivity(SecondHomeActivity::class.java)
            true
        }
        val intentFilter = IntentFilter()
        intentFilter.addAction(BleConstant.BLE_CONNECTED_ACTION)
        intentFilter.addAction(BleConstant.BLE_DIS_CONNECT_ACTION)
        intentFilter.addAction(BleConstant.BLE_SCAN_COMPLETE_ACTION)
        intentFilter.addAction(BleConstant.BLE_START_SCAN_ACTION)
        registerReceiver(broadcastReceiver,intentFilter)
//        if (Build.VERSION.SDK_INT >= 33) {
//            XXPermissions.with(this).permission(arrayOf(Manifest.permission.POST_NOTIFICATIONS)).request { permissions, all ->  }
//        }
        
        XXPermissions.with(this).permission(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE)).request { permissions, allGranted ->  }
    }


    //展示APP版本和设备的固件版本
    private fun showVersion(){
        val packageInfo = packageManager.getPackageInfo(packageName,0)
        val versionName = packageInfo.versionName
        appVersionTv?.text = String.format(resources.getString(R.string.string_app_version),versionName)

        //判断最设备是否链接
        val isConn = BaseApplication.getBaseApplication().connStatus == ConnStatus.CONNECTED
        if(isConn){
            BaseApplication.getBaseApplication().bleOperate.getDeviceVersionData(object : OnCommBackDataListener{
                override fun onIntDataBack(value: IntArray?) {
                    Timber.e("------版本好="+ (value?.get(0) ?: 0))
                   val code = value?.get(0)
                    BaseApplication.getBaseApplication().setLogStr("VersionCode="+code.toString())
                    if(code != null){
                        viewModel.checkVersion(this@MainActivity,code)
                    }
                }

                override fun onStrDataBack(vararg value: String?) {
                    firmwareVersionTv?.text =  String.format(resources.getString(R.string.string_firmware_version),value[0])
                }

            })
        }else{
            firmwareVersionTv?.text =  ""
        }
    }




    override fun onResume() {
        super.onResume()
        //showOpenNotifyDialog()
        showVersion()


       // showOtaDialog()
    }


    //判断是否连接，未连接重连
    private fun retryConn() {
        val connBleMac = MmkvUtils.getConnDeviceMac()
        if (!BikeUtils.isEmpty(connBleMac)) {
            //是否已经连接
            val isConn = BaseApplication.getBaseApplication().connStatus == ConnStatus.CONNECTED
            if (isConn) {
                return
            }

            verifyScanFun(connBleMac)
        }
    }


    //判断是否有位置权限了，没有请求权限
    private fun verifyScanFun(mac: String) {

        //判断蓝牙是否开启
        if (!BikeUtils.isBleEnable(this)) {
            BikeUtils.openBletooth(this)
            return
        }
        //判断权限
        val isPermission = ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        if (!isPermission) {
            XXPermissions.with(this).permission(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            ).request { permissions, all ->
                connToDevice(mac)
            }
            // ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION),0x00)
            return
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {

            XXPermissions.with(this).permission(
                arrayOf(
                    Manifest.permission.BLUETOOTH_CONNECT,
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_ADVERTISE
                )
            ).request { permissions, all ->
                //verifyScanFun()
            }
        }

//        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.S_V2){
//            XXPermissions.with(this).permission(arrayOf(POST_NOTIFICATIONS)).request { permissions, allGranted ->  }
//        }


        //判断蓝牙是否打开
        val isOpenBle = BonlalaUtils.isOpenBlue(this@MainActivity)
        if (!isOpenBle) {
            BonlalaUtils.openBluetooth(this)
            return
        }
        connToDevice(mac)
    }

    private fun connToDevice(mac : String){
        Handler(Looper.getMainLooper()).postDelayed({
            val service = BaseApplication.getBaseApplication().connStatusService

            if(service != null){
                BaseApplication.getBaseApplication().connStatus = ConnStatus.CONNECTING
                service.autoConnDevice(mac, false,false)
            }

        },3000)

    }

    override fun initData() {
        //判断是否需要显示隐私政策，第一次打开需要隐私政策
        val isFirstOpen = MmkvUtils.getPrivacy()
        if (!isFirstOpen) {
            showPrivacyDialog()
        } else {
            showOpenNotifyDialog()
        }

        retryConn()


        viewModel.firmwareData.observe(this){
            if(it != null){
                if(it.isError){
                    BaseApplication.getBaseApplication().logStr = it.errorMsg
                }else{
                    BaseApplication.getBaseApplication().logStr = Gson().toJson(it)
                    showUpgradeDialog(it.ota,it.fileName)
                }

            }
        }
    }

    //显示隐私弹窗
    private fun showPrivacyDialog() {
        val dialog =
            ShowPrivacyDialogView(this, com.bonlala.base.R.style.BaseDialogTheme, this@MainActivity)
        dialog.show()
        dialog.setCancelable(false)
        dialog.setOnPrivacyClickListener(object : ShowPrivacyDialogView.OnPrivacyClickListener {
            override fun onCancelClick() {
                dialog.dismiss()
                MmkvUtils.setIsAgreePrivacy(false)
                BaseApplication.getBaseApplication().bleOperate.disConnYakDevice()
                BaseApplication.getBaseApplication().connStatus = ConnStatus.NOT_CONNECTED
                ActivityManager.getInstance().finishAllActivities()
                finish()
            }

            override fun onConfirmClick() {
                dialog.dismiss()
                MmkvUtils.setIsAgreePrivacy(true)
                showOpenNotifyDialog()
            }

        })
    }


    override fun onClick(view: View?) {
        super.onClick(view)
        val id = view?.id

        when (id) {
            //表盘
            R.id.homeDialLayout -> {
                startActivity(DialHomeActivity::class.java)
            }
            //记事本
            R.id.homeNotebookLayout -> {
                startActivity(NotebookActivity::class.java)
            }

            //键盘
            R.id.homeKeyboardLayout -> {
                startActivity(BleKeyboardActivity::class.java)
            }
        }
    }

    private var mExitTime: Long = 0

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        // 过滤按键动作
        if (event.keyCode == KeyEvent.KEYCODE_BACK) {
            if (System.currentTimeMillis() - mExitTime > 2000) {
                mExitTime = System.currentTimeMillis()
                ToastUtils.show(resources.getString(R.string.string_double_click_exit))
                return true
            } else {
                BaseApplication.getBaseApplication().bleOperate.disConnYakDevice()
                BaseApplication.getBaseApplication().connStatus = ConnStatus.NOT_CONNECTED
                ActivityManager.getInstance().finishAllActivities()
                finish()
            }
        }
        return super.onKeyDown(keyCode, event)
    }


    private fun openNoti() {
        val dialog = NoticeDialog(this, com.bonlala.base.R.style.BaseDialogTheme)
        dialog.show()
        dialog.setCancelable(false)
        dialog.setOnDialogClickListener { position ->
            if (position == 0x00) {
                dialog.dismiss()
                startToNotificationListenSetting(this@MainActivity)
//                NotificationUtils.gotoSet(this)
//                XXPermissions.with(this)
//                    .permission(arrayOf(Manifest.permission.BIND_NOTIFICATION_LISTENER_SERVICE))
//                    .request { permissions, all -> }
            }
            if (position == 0x01) {
                dialog.dismiss()
            }
        }
    }

    //显示打开通知权限弹窗
    private fun showOpenNotifyDialog() {
        //判断通知权限是否打开了
        val isOpen2 = hasNotificationListenPermission(this)
        val isOpen = NotificationUtils.isNotificationEnabled(this)
        Timber.e("--------通知是否打开了=" + isOpen + " " + isOpen2)
        if (!isOpen2) {
            openNoti()
        }
    }


    /**
     * 跳转到通知内容读取权限设置
     *
     * @param context
     */
    private fun startToNotificationListenSetting(context: Activity) {
        try {
            val intent = Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS")
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivityForResult(intent, 0x08)
            //context.startActivity(intent);
        } catch (e: ActivityNotFoundException) {
            try {
                val intent = Intent()
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                val cn = ComponentName(
                    "com.android.settings",
                    "com.android.settings.Settings\$NotificationAccessSettingsActivity"
                )
                intent.component = cn
                intent.putExtra(":settings:show_fragment", "NotificationAccessSettings")
                context.startActivity(intent)
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }
    }


    /**
     * 有通知/监听读取权限
     * 迁移到SNNotificationListener依赖库
     *
     * @param context
     * @return
     */
    private fun hasNotificationListenPermission(context: Context): Boolean {
        val packageNames = NotificationManagerCompat.getEnabledListenerPackages(context)
        return !packageNames.isEmpty() && packageNames.contains(context.packageName)
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

    private var upgradeDialogView : UpgradeDialogView  ?= null


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


    private var dialog : OtaDialogView ?= null

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