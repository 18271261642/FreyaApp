package com.app.freya.second

import android.Manifest
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.KeyEvent
import android.view.View
import android.widget.LinearLayout
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager.widget.ViewPager
import com.app.freya.BaseApplication
import com.app.freya.R
import com.app.freya.action.ActivityManager
import com.app.freya.action.AppActivity
import com.app.freya.action.AppFragment
import com.app.freya.adapter.OnCommItemClickListener
import com.app.freya.ble.ConnStatus
import com.app.freya.ble.OnConnStateListener
import com.app.freya.dialog.NoticeDialog
import com.app.freya.utils.BikeUtils
import com.app.freya.utils.BonlalaUtils
import com.app.freya.utils.MmkvUtils
import com.app.freya.utils.NotificationUtils
import com.app.freya.viewmodel.SecondHomeViewModel
import com.app.freya.widget.HomeMenuView
import com.blala.blalable.BleConstant
import com.bonlala.base.FragmentPagerAdapter
import com.hjq.permissions.XXPermissions
import com.hjq.shape.layout.ShapeLinearLayout
import com.hjq.toast.ToastUtils
import timber.log.Timber

/**
 * 键盘二代主页，三个底部菜单
 */
class SecondHomeActivity : AppActivity() {


    private var onStateListener: OnCommItemClickListener? = null



    private var onConnStatusListener : OnConnStateListener ?= null

    fun setOnConnStateListener(c : OnConnStateListener){
        this.onConnStatusListener = c
    }

    fun setOnStateListener(li: OnCommItemClickListener) {
        this.onStateListener = li
    }


    private var viewModel: SecondHomeViewModel? = null

    private var scanHolderLayout: LinearLayout? = null
    private var dataAddLayout: ShapeLinearLayout? = null

    private val INTENT_KEY_IN_FRAGMENT_INDEX = "fragmentIndex"
    private val INTENT_KEY_IN_FRAGMENT_CLASS = "fragmentClass"

    private var mViewPager: ViewPager? = null

    private var mPagerAdapter: FragmentPagerAdapter<AppFragment<*>>? = null

    private var secondHomeMenuView: HomeMenuView? = null

    override fun getLayoutId(): Int {
        return R.layout.activity_second_home_layout
    }

    override fun initView() {
        dataAddLayout = findViewById(R.id.dataAddLayout)
        mViewPager = findViewById(R.id.vp_home_pager)
        secondHomeMenuView = findViewById(R.id.secondHomeMenuView)
        scanHolderLayout = findViewById(R.id.scanHolderLayout)

        secondHomeMenuView?.setOnItemClick(object : OnCommItemClickListener {
            override fun onItemClick(position: Int) {
                switchFragment(position)
            }

        })

        dataAddLayout?.setOnClickListener { startActivity(SecondScanActivity::class.java) }

    }

    override fun initData() {
        mPagerAdapter = FragmentPagerAdapter(this)
        mPagerAdapter?.addFragment(MenuDataFragment.getInstance())
        mPagerAdapter?.addFragment(MenuSettingFragment.getInstance())
        mPagerAdapter?.addFragment(MenuDeviceFragment.getInstance())
        mViewPager?.adapter = mPagerAdapter
        viewModel = ViewModelProvider(this).get(SecondHomeViewModel::class.java)
        viewModel?.getAllSupportDeviceType(this)


        val intentFilter = IntentFilter()
        intentFilter.addAction(BleConstant.BLE_CONNECTED_ACTION)
        intentFilter.addAction(BleConstant.BLE_DIS_CONNECT_ACTION)
        intentFilter.addAction(BleConstant.BLE_SCAN_COMPLETE_ACTION)
        intentFilter.addAction(BleConstant.BLE_START_SCAN_ACTION)
        registerReceiver(broadcastReceiver,intentFilter)

        retryConn()
    }


    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        switchFragment(mPagerAdapter!!.getFragmentIndex(getSerializable(INTENT_KEY_IN_FRAGMENT_CLASS)))
    }


    override fun onResume() {
        super.onResume()
        showIsAddDevice()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        // 保存当前 Fragment 索引位置
        outState.putInt(INTENT_KEY_IN_FRAGMENT_INDEX, mViewPager!!.currentItem)
    }


    private fun switchFragment(fragmentIndex: Int) {
        if (fragmentIndex == -1) {
            return
        }

        Timber.e("-------swww=" + fragmentIndex)

        when (fragmentIndex) {
            0, 1, 2 -> {
                mViewPager!!.currentItem = fragmentIndex

            }

            else -> {}
        }
    }

    //是否显示添加设备
    fun showIsAddDevice() {
        //是否有连接过
        val isMac = MmkvUtils.getConnDeviceMac()
        Timber.e("-----isMac=" + isMac)
        scanHolderLayout?.visibility = if (BikeUtils.isEmpty(isMac)) View.VISIBLE else View.GONE

        onStateListener?.onItemClick(0x00)
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


    private fun openNoti() {
        val dialog = NoticeDialog(this, com.bonlala.base.R.style.BaseDialogTheme)
        dialog.show()
        dialog.setCancelable(false)
        dialog.setOnDialogClickListener { position ->
            if (position == 0x00) {
                dialog.dismiss()
                startToNotificationListenSetting(this@SecondHomeActivity)
            }
            if (position == 0x01) {
                dialog.dismiss()
            }
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


    //判断是否连接，未连接重连
    fun retryConn() {
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


        //判断蓝牙是否打开
        val isOpenBle = BonlalaUtils.isOpenBlue(this@SecondHomeActivity)
        if (!isOpenBle) {
            BonlalaUtils.openBluetooth(this)
            return
        }
        connToDevice(mac)
    }

    private fun connToDevice(mac: String) {
        Handler(Looper.getMainLooper()).postDelayed({
            val service = BaseApplication.getBaseApplication().connStatusService

            if (service != null) {
                BaseApplication.getBaseApplication().connStatus = ConnStatus.CONNECTING
                onConnStatusListener?.onConnState(ConnStatus.CONNECTING)
                service.autoConnDevice(mac, false)
            }

        }, 3000)

    }


    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(broadcastReceiver)
    }



    private val broadcastReceiver : BroadcastReceiver = object : BroadcastReceiver(){
        override fun onReceive(p0: Context?, p1: Intent?) {
            val action = p1?.action
            Timber.e("---------acdtion="+action)
            if(action == BleConstant.BLE_START_SCAN_ACTION){ //开始连接了
                BaseApplication.getBaseApplication().connStatus = ConnStatus.CONNECTING
                onConnStatusListener?.onConnState(ConnStatus.CONNECTING)
            }
            if(action == BleConstant.BLE_CONNECTED_ACTION){
                ToastUtils.show(resources.getString(R.string.string_conn_success))
                BaseApplication.getBaseApplication().connStatus = ConnStatus.CONNECTED
                BaseApplication.getBaseApplication().bleOperate.stopScanDevice()
                onConnStatusListener?.onConnState(ConnStatus.CONNECTED)
              //  showVersion()

               // setDialogTxtShow(resources.getString(R.string.string_upgrade_success))
            }
            if(action == BleConstant.BLE_DIS_CONNECT_ACTION){
                ToastUtils.show(resources.getString(R.string.string_conn_disconn))
                onConnStatusListener?.onConnState(ConnStatus.NOT_CONNECTED)
              //  showVersion()
            }
        }

    }
}