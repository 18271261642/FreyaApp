package com.app.freya

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.*
import android.util.DisplayMetrics
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import com.app.freya.action.AppActivity
import com.app.freya.ble.ConnStatus
import com.app.freya.dialog.DialogScanDeviceView
import com.app.freya.utils.BikeUtils
import com.app.freya.utils.BonlalaUtils
import com.app.freya.utils.MmkvUtils
import com.blala.blalable.BleConstant
import com.blala.blalable.BleOperateManager
import com.hjq.permissions.XXPermissions
import com.hjq.shape.layout.ShapeConstraintLayout
import com.hjq.shape.view.ShapeTextView
import timber.log.Timber
import java.util.*

/**
 * 设置表盘页面
 * Created by Admin
 *Date 2023/1/12
 */
class BleKeyboardActivity : AppActivity(){

    //搜索的按钮
    private var scanReScanTv : ShapeTextView ?= null
    //空的
    private var scanEmptyLayout : LinearLayout ?= null

    //蓝牙的名称
    private var keyBoardNameTv : TextView ?= null
    //mac
    private var keyBoardMacTv : TextView ?= null
    //连接状态
    private var keyBoardStatusTv : ShapeTextView ?= null
    //解除绑定
    private var keyBoardUnBindTv : ShapeTextView ?= null
    //已经连接的布局
    private var keyBoardConnLayout : ShapeConstraintLayout ?= null

    //是否正在连接
    private var isConnecting = false

    private var lowTv : TextView ?= null

    private var bluetoothAdapter : BluetoothAdapter ?= null


    private val handlers : Handler = object : Handler(Looper.getMainLooper()){
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            if(!activity.isFinishing){
                val log = BleOperateManager.getInstance().getLog()

               // lowTv?.text = log.toString()
            }


        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val intentFilter = IntentFilter()
        intentFilter.addAction(BleConstant.BLE_CONNECTED_ACTION)
        intentFilter.addAction(BleConstant.BLE_DIS_CONNECT_ACTION)
        intentFilter.addAction(BleConstant.BLE_SCAN_COMPLETE_ACTION)
        intentFilter.addAction(BleConstant.BLE_START_SCAN_ACTION)
        intentFilter.addAction("ble_action")
        registerReceiver(broadcastReceiver,intentFilter)


    }

    override fun getLayoutId(): Int {
        return R.layout.activity_device_keyboard_layout
    }

    override fun initView() {
        lowTv = findViewById(R.id.lowTv)
        scanReScanTv = findViewById(R.id.scanReScanTv)
        scanEmptyLayout = findViewById(R.id.scanEmptyLayout)

        keyBoardConnLayout = findViewById(R.id.keyBoardConnLayout)
        keyBoardNameTv = findViewById(R.id.keyBoardNameTv)
        keyBoardMacTv = findViewById(R.id.keyBoardMacTv)
        keyBoardStatusTv = findViewById(R.id.keyBoardStatusTv)
        keyBoardUnBindTv = findViewById(R.id.keyBoardUnBindTv)

//        startRunnable()

    }

    private fun startRunnable(){
      handlers.postDelayed(runnable,1000)
    }

    private var runnable : Runnable = Runnable {
       handlers.sendEmptyMessageDelayed(0x00,1000)
    }


    override fun onStop() {
        super.onStop()
        handlers.removeMessages(0x00)

    }

    override fun initData() {
        val bleManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bleManager.adapter
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {

            XXPermissions.with(this).permission(arrayOf(
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_ADVERTISE)).request { permissions, all ->
                //verifyScanFun()
            }
        }

        scanReScanTv?.setOnClickListener {
            verifyScanFun(false)

        }

        //删除
        keyBoardUnBindTv?.setOnClickListener {
            dealDevice()
        }

        //重新连接
        keyBoardStatusTv?.setOnClickListener {
            reConnect()
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN),
                0x88
            )
        }

    }

    private fun reConnect(){
        if(BaseApplication.getBaseApplication().connStatus == ConnStatus.CONNECTED){
            return
        }

        verifyScanFun(true)
    }

    override fun onResume() {
        super.onResume()
        showDeviceStatus()
    }


    private fun dealDevice(){
        val alert = AlertDialog.Builder(this)
            .setTitle(resources.getString(R.string.comm_prompt))
            .setMessage(resources.getString(R.string.string_is_unbind))
            .setPositiveButton(resources.getString(R.string.common_confirm)
            ) { p0, p1 ->
                p0?.dismiss()

                BaseApplication.getBaseApplication().bleOperate.disConnYakDevice()
                MmkvUtils.saveConnDeviceName("")
                MmkvUtils.saveConnDeviceMac("")

                showDeviceStatus()

            }
            .setNegativeButton(resources.getString(R.string.common_cancel)
            ) { p0, p1 -> p0?.dismiss()
                p0?.dismiss()
            }
        alert.create().show()
    }

    //显示状态
    private fun showDeviceStatus(){
        //是否有绑定过
        val bleMac = MmkvUtils.getConnDeviceMac()
        val isBind = BikeUtils.isEmpty(bleMac)
        keyBoardConnLayout?.visibility = if(isBind) View.GONE else View.VISIBLE
        scanEmptyLayout?.visibility = if(isBind) View.VISIBLE else View.INVISIBLE


        //是否已连接
        if(!isBind){
            val bleName = MmkvUtils.getConnDeviceName()
            keyBoardNameTv?.text = resources.getString(R.string.string_name)+": "+bleName
            keyBoardMacTv?.text = "MAC: "+bleMac
            val isConnStatus = BaseApplication.getBaseApplication().connStatus
            Timber.e("-----连接状态="+isConnStatus)
            keyBoardStatusTv?.text = if(isConnStatus == ConnStatus.CONNECTED) resources.getString(R.string.string_connected) else (if(isConnecting || isConnStatus == ConnStatus.CONNECTING) resources.getString(R.string.string_connecting) else resources.getString(R.string.string_retry_conn))

        }

    }


    //判断是否有位置权限了，没有请求权限
    private fun verifyScanFun(isReconn : Boolean){

        //判断蓝牙是否开启
        if(!BikeUtils.isBleEnable(this)){
            BikeUtils.openBletooth(this)
            return
        }
        //判断权限
        val isPermission = ActivityCompat.checkSelfPermission(this,
            Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        if(!isPermission){
            XXPermissions.with(this).permission(arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION)).request { permissions, all ->
                verifyScanFun(isReconn)
            }
            // ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION),0x00)
            return
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {

            XXPermissions.with(this).permission(arrayOf(
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_ADVERTISE)).request { permissions, all ->
                //verifyScanFun()
            }
        }


        //判断蓝牙是否打开
        val isOpenBle = BonlalaUtils.isOpenBlue(this@BleKeyboardActivity)
        if(!isOpenBle){
            BonlalaUtils.openBluetooth(this)
            return
        }

        if(isReconn){
            val mac = MmkvUtils.getConnDeviceMac()
            if(BikeUtils.isEmpty(mac))
                return
            isConnecting = true
            keyBoardStatusTv?.text = resources.getString(R.string.string_connecting)
            BaseApplication.getBaseApplication().connStatusService.autoConnDevice(mac,false)
            keyBoardStatusTv?.text = resources.getString(R.string.string_connecting)
        }else{

            showScanDialog()
        }

    }


    //开始搜索，显示dialog
    @SuppressLint("MissingPermission")
    private fun showScanDialog(){
        val bindArray   =bluetoothAdapter?.bondedDevices
       // Timber.e("----22-绑定="+bindArray?.size+" "+(bluetoothAdapter == null))
        bindArray?.forEach {
            //Timber.e("-----绑定="+it.name)
            if(it != null && !BikeUtils.isEmpty(it.name) && it.name.toLowerCase(Locale.ROOT).contains("zoom")){
               // Timber.e("---取消配对--绑定="+it.name)
                BikeUtils.unpairDevice(it)
            }
        }


        val dialog = DialogScanDeviceView(this@BleKeyboardActivity, com.bonlala.base.R.style.BaseDialogTheme)
        dialog.show()
        dialog.startScan()
        dialog.setOnDialogClickListener { position ->

            Timber.e("-----position="+position)
            if (position == 0x00) {   //显示进度条
                showDialog(resources.getString(R.string.string_connecting))
            }

            if (position == 0x01) {   //连接成功
                hideDialog()
                isConnecting = false
                showDeviceStatus()
            }
        }
        val window = dialog.window
        val windowLayout = window?.attributes
        val metrics2: DisplayMetrics = resources.displayMetrics
        val widthW: Int = (metrics2.widthPixels * 0.9f).toInt()
        val height : Int = (metrics2.heightPixels * 0.6f).toInt()
        windowLayout?.width = widthW
        windowLayout?.height = height
        window?.attributes = windowLayout

    }



    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        showScanDialog()
    }


    private val broadcastReceiver : BroadcastReceiver = object : BroadcastReceiver(){
        override fun onReceive(p0: Context?, p1: Intent?) {
            val action = p1?.action
            Timber.e("---------acdtion="+action)
            if(action == BleConstant.BLE_CONNECTED_ACTION){
                isConnecting = false
                showDeviceStatus()
            }
            if(action == BleConstant.BLE_DIS_CONNECT_ACTION){
                isConnecting = false
                showDeviceStatus()
            }

            if(action == "ble_action"){
                handlers.sendEmptyMessage(0x00)
            }

            if(action == BleConstant.BLE_START_SCAN_ACTION){
                showDeviceStatus()
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
}