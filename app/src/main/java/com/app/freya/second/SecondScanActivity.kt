package com.app.freya.second

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.DisplayMetrics
import android.view.Gravity
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.freya.BaseApplication
import com.app.freya.R
import com.app.freya.action.AppActivity
import com.app.freya.adapter.OnCommItemClickListener
import com.app.freya.adapter.OnCommMenuClickListener
import com.app.freya.adapter.SecondScanAdapter
import com.app.freya.bean.BleBean
import com.app.freya.bean.DbManager
import com.app.freya.ble.ConnStatus
import com.app.freya.ble.ConnStatusService
import com.app.freya.dialog.DeleteDeviceDialog
import com.app.freya.utils.BikeUtils
import com.app.freya.utils.BonlalaUtils
import com.app.freya.utils.MmkvUtils
import com.blala.blalable.BleConstant
import com.blala.blalable.Utils
import com.blala.blalable.listener.BleScanListener
import com.google.gson.Gson
import com.hjq.permissions.XXPermissions
import com.hjq.toast.ToastUtils
import com.inuker.bluetooth.library.search.SearchResult
import com.inuker.bluetooth.library.search.response.SearchResponse
import timber.log.Timber
import java.util.Locale
import kotlin.math.abs

/**
 * Created by Admin
 *Date 2023/7/12
 */
class SecondScanActivity : AppActivity() {

    private var secondScanRy: RecyclerView? = null

    private var adapter: SecondScanAdapter? = null
    private var list: MutableList<BleBean>? = null


    private var bindList : MutableList<BleBean>?=null
    private var bindAdapter : SecondScanAdapter ?= null
    //已连接的设备
    private var secondConnRecyclerView : RecyclerView ?= null

    private val connStatusService = BaseApplication.getBaseApplication().connStatusService

    //用于去重的list
    private var repeatList: MutableList<String>? = null

    private val handlers: Handler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            if (msg.what == 0x00) {
                BaseApplication.getBaseApplication().bleOperate.stopScanDevice()

            }

            if(msg.what == 0x01){
                BaseApplication.getBaseApplication().bleOperate.disConnYakDevice()
            }
        }
    }

    override fun getLayoutId(): Int {
        return R.layout.activity_second_scan_layout
    }

    override fun initView() {
        secondConnRecyclerView = findViewById(R.id.secondConnRecyclerView)
        secondScanRy = findViewById(R.id.secondScanRy)
        val linearLayoutManager = LinearLayoutManager(context)
        linearLayoutManager.orientation = LinearLayoutManager.VERTICAL
        secondScanRy?.layoutManager = linearLayoutManager

        val lm = LinearLayoutManager(this)
        lm.orientation = LinearLayoutManager.VERTICAL
        secondConnRecyclerView?.layoutManager = lm
        bindList = mutableListOf()
        bindAdapter = SecondScanAdapter(this, bindList!!)
        secondConnRecyclerView?.adapter = bindAdapter

        bindAdapter?.setOnCommMenuClick(onBIndMenuClick)


        list = mutableListOf()
        adapter = SecondScanAdapter(context, list!!)
        secondScanRy?.adapter = adapter
        repeatList = mutableListOf()
//        adapter!!.setOnItemClick(onItemClick)

        adapter?.setOnCommMenuClick(onMenuClick)
    }

    override fun initData() {

        val intentFilter = IntentFilter()
        intentFilter.addAction(BleConstant.BLE_CONNECTED_ACTION)
        intentFilter.addAction(BleConstant.BLE_DIS_CONNECT_ACTION)
        intentFilter.addAction(BleConstant.BLE_SCAN_COMPLETE_ACTION)
        intentFilter.addAction(BleConstant.BLE_START_SCAN_ACTION)
        registerReceiver(broadcastReceiver,intentFilter)

    }


    private fun backScanDevice(){
        //已经绑定的设备
        val bindUserList = DbManager.getInstance().allBindDevice
        bindUserList?.sortByDescending { it ->it.bindTime }
        val saveMac = MmkvUtils.getConnDeviceMac()
       connStatusService.setBleScanListener(object : BleScanListener{
           override fun onSearchStarted() {

           }

           override fun onDeviceFounded(searchResult: SearchResult?) {
               stringBuilder.delete(0,stringBuilder.length);
               if (searchResult?.getScanRecord() == null || searchResult.getScanRecord().isEmpty())
                   return
               val bleMac = searchResult.device.address
               val tempStr = Utils.formatBtArrayToString(searchResult.getScanRecord())
               // stringBuilder.append(tempStr)
               val recordStr = tempStr
               val bleName = searchResult.name
               // Timber.e("--------扫描="+p0.name+" "+recordStr)
               if (BikeUtils.isEmpty(bleName) || bleName.equals("NULL") || BikeUtils.isEmpty(searchResult.address))
                   return
               if (repeatList?.contains(searchResult.address) == true)
                   return
               if(BikeUtils.isEmpty(recordStr)){
                   return
               }

               if(bleName.lowercase(Locale.ROOT).contains("huawei")){
                   return
               }
               if(!BikeUtils.isEmpty(saveMac) && saveMac.lowercase(Locale.ROOT) ==searchResult.address.toLowerCase(
                       Locale.ROOT)){
                   return
               }
               if (repeatList?.size!! > 40) {
                   return
               }
               if(abs(searchResult.rssi) >85)
                   return
               if(bindUserList!=null){
                   bindUserList.forEach {
                       if(it.deviceMac == bleMac){
                           it.rssi = searchResult.rssi
                       }
                   }
                   bindAdapter?.notifyDataSetChanged()
               }


               if(!repeatList!!.contains(searchResult.address) ){
                   searchResult.address?.let { repeatList?.add(it) }
                   list?.add(BleBean(searchResult.device, searchResult.rssi,"",recordStr))
                   list?.sortBy {
                       Math.abs(it.rssi)
                   }
               }

               adapter?.notifyDataSetChanged()

               /* typeMap.forEach {
                    val keyStr = it.key
                    val tempK = Utils.changeStr(keyStr)
                    val scanRecord = recordStr.lowercase(Locale.ROOT)
                    val front = scanRecord.contains(keyStr.lowercase(Locale.ROOT))
                    val back = scanRecord.contains(tempK.lowercase(Locale.ROOT))
                    Timber.e("----转换="+tempK)
                    if(front || back){
                        //判断少于40个设备就不添加了
                        if (repeatList?.size!! > 40) {
                            return
                        }
                        if(!repeatList!!.contains(p0.address)){
                            searchResult.address?.let { repeatList?.add(it) }
                            list?.add(BleBean(searchResult.device, searchResult.rssi,keyStr,scanRecord))
                            list?.sortBy {
                                Math.abs(it.rssi)
                            }
                        }

                        adapter?.notifyDataSetChanged()
                    }
                }*/
           }

           override fun onSearchStopped() {

           }

           override fun onSearchCanceled() {

           }

       })
    }


    override fun onResume() {
        super.onResume()
        dealScanDevice()
    }


    private fun dealScanDevice(){
        BaseApplication.getBaseApplication().isActivityScan = true
        repeatList?.clear()
        list?.clear()

        //  backScanDevice()

        getBindDeviceList()
        verifyScanFun(false)
    }


    //获取绑定的设备
    private fun getBindDeviceList(){
        bindList?.clear()

        val isCOnn = BaseApplication.getBaseApplication().connStatus == ConnStatus.CONNECTED
        val bindUserList = DbManager.getInstance().allBindDevice
        val mac = MmkvUtils.getConnDeviceMac()
        if(bindUserList != null){
            bindUserList.sortByDescending { it ->it.bindTime }
            bindUserList.forEach {
                val bean = BleBean()
                bean.isBind = true
                bean.rssi = 0
                bean.productNumber = ""
                bean.bleMac = it.deviceMac
                bean.bleName = it.deviceName
                if(!BikeUtils.isEmpty(mac) && mac == it.deviceMac){
                    bean.connStatus =  BaseApplication.getBaseApplication().connStatus
                }
                bindList?.add(bean)
            }
            bindAdapter?.notifyDataSetChanged()
        }else{
            bindList?.clear()
            bindAdapter?.notifyDataSetChanged()
        }
    }


    //判断是否有位置权限了，没有请求权限
    private fun verifyScanFun(isReconn: Boolean) {

        //判断蓝牙是否开启
        if (!BikeUtils.isBleEnable(this)) {
            BikeUtils.openBletooth(this)
            return
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {

            XXPermissions.with(this).permission(
                arrayOf(
                    Manifest.permission.BLUETOOTH_CONNECT,
                    Manifest.permission.BLUETOOTH_SCAN
                )
            ).request { permissions, all ->
                //verifyScanFun()
            }
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
                verifyScanFun(isReconn)
            }
            // ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION),0x00)
            return
        }

        //判断蓝牙是否打开
        val isOpenBle = BonlalaUtils.isOpenBlue(this@SecondScanActivity)
        if (!isOpenBle) {
            BonlalaUtils.openBluetooth(this)
            return
        }
        startScan()

    }


    private val onBIndMenuClick : OnCommMenuClickListener = object : OnCommMenuClickListener{
        override fun onItemClick(position: Int) {
            val bean = bindList?.get(position)
            if(BaseApplication.getBaseApplication().connStatus == ConnStatus.CONNECTING){
                ToastUtils.show("正在连接中,请稍后!")
                return
            }
            if(bean == null)
                return
            if(BaseApplication.getBaseApplication().connStatus == ConnStatus.CONNECTED){
                //判断是否是连接当前的设备
                if(bean?.bleMac == MmkvUtils.getConnDeviceMac()){
                    ToastUtils.show("当前设备已经连接!")
                    return
                }
                val service = BaseApplication.getBaseApplication().connStatusService
                if (bean != null) {
                    showConnDialogView(bean,service,true,position)
                }

                return
            }
            bindList?.get(position)?.connStatus = ConnStatus.CONNECTING
            bindAdapter?.notifyItemChanged(position)
            connStatusService.connDeviceBack(
                bean.bleName, bean.bleMac
            ) { mac, status ->
                hideDialog()
                ToastUtils.show(resources.getString(R.string.string_conn_success))
                DbManager.getInstance().saveUserBindDevice(bean.bleName,bean.bleMac,BikeUtils.getFormatDate(System.currentTimeMillis(),"yyyy-MM-dd HH:mm:ss"))
                BaseApplication.getBaseApplication().connStatus = ConnStatus.CONNECTED
                bindList?.get(position)?.connStatus = ConnStatus.CONNECTED
                bindList?.get(position)?.rssi = 0
                bindAdapter?.notifyItemChanged(position)
                MmkvUtils.saveProductNumberCode(bean.productNumber)
                MmkvUtils.saveConnDeviceMac(mac)
                MmkvUtils.saveConnDeviceName(bean.bleName)

                dealScanDevice()
                //initData()
            }

        }

        override fun onChildItemClick(position: Int) {
            val mac = bindList!!.get(position).bleMac
            startActivity(SecondHasBindDeviceActivity::class.java, arrayOf("bind_mac"),
                arrayOf(mac)
            )
        }

    }


    private val onMenuClick : OnCommMenuClickListener = object : OnCommMenuClickListener{
        override fun onItemClick(position: Int) {
            val service = BaseApplication.getBaseApplication().connStatusService
            val bean = list?.get(position)
            if (bean != null) {
                if(BaseApplication.getBaseApplication().connStatus == ConnStatus.CONNECTING){
                    ToastUtils.show("正在连接中,请稍后!")
                    return
                }
                if(BaseApplication.getBaseApplication().connStatus == ConnStatus.CONNECTED){
                    //判断是否是连接当前的设备
                    if(bean.bleMac == MmkvUtils.getConnDeviceMac()){
                        ToastUtils.show("当前设备已经连接!")
                        return
                    }else{
                        showConnDialogView(bean,service,false,position)
                    }

                    return
                }



               // handlers.sendEmptyMessageDelayed(0x00, 500)
                showDialog("连接中..")
                bean.connStatus = ConnStatus.CONNECTING
                adapter?.notifyItemChanged(position)

                service.connDeviceBack(
                    bean.bluetoothDevice.name, bean.bluetoothDevice.address
                ) { mac, status ->
                    hideDialog()
                    DbManager.getInstance().saveUserBindDevice(bean.bluetoothDevice.name, bean.bluetoothDevice.address,BikeUtils.getFormatDate(System.currentTimeMillis(),"yyyy-MM-dd HH:mm:ss"))
                    MmkvUtils.saveProductNumberCode(bean.productNumber)
                    MmkvUtils.saveConnDeviceMac(mac)
                    MmkvUtils.saveConnDeviceName(bean.bluetoothDevice.name)
                    BaseApplication.getBaseApplication().connStatus = ConnStatus.CONNECTED
                    dealScanDevice()
                    //  finish()
                }
            }
        }

        override fun onChildItemClick(position: Int) {


        }

    }


    val stringBuilder = StringBuilder()
    //开始扫描
    private fun startScan() {
        val typeMap = BaseApplication.supportDeviceTypeMap
        stringBuilder.delete(0,stringBuilder.length)
        val saveMac = MmkvUtils.getConnDeviceMac()
        //已经绑定的设备
        val bindUserList = DbManager.getInstance().allBindDevice
        bindUserList?.sortByDescending { it ->it.bindTime }
        BaseApplication.getBaseApplication().bleOperate.scanBleDevice(object : SearchResponse {

            override fun onSearchStarted() {

            }

            override fun onDeviceFounded(p0: SearchResult) {
                stringBuilder.delete(0,stringBuilder.length);
                if (p0.getScanRecord() == null || p0.getScanRecord().isEmpty())
                    return
                val bleMac = p0.device.address
               // Timber.e("----搜索="+bleMac+" "+p0?.address)
                val tempStr = Utils.formatBtArrayToString(p0.getScanRecord())
                // stringBuilder.append(tempStr)
                val recordStr = tempStr
                val bleName = p0.name
                // Timber.e("--------扫描="+p0.name+" "+recordStr)
                if (BikeUtils.isEmpty(bleName) || bleName.equals("NULL") || BikeUtils.isEmpty(bleMac))
                    return
                if (repeatList?.contains(p0.address) == true)
                    return
                if(BikeUtils.isEmpty(recordStr)){
                    return
                }

                if(bleName.lowercase(Locale.ROOT).contains("huawei")){
                    return
                }
                if (repeatList?.size!! > 40) {
                    return
                }
                if(abs(p0.rssi) >85)
                    return

                Timber.e("------bidList="+(bindList != null) +" "+Gson().toJson(bindList))

                if(bindList?.isNotEmpty()==true){
                    bindList?.forEach {
                        Timber.e("--------bindList="+it.bleName+" bindMac="+it.bleMac+ " rssi="+p0.rssi +" scan="+bleMac)
                        if(it.bleMac == bleMac){
                            it.rssi = p0.rssi
                        }
                    }

                    bindAdapter?.notifyDataSetChanged()

                  val isHas =  bindList?.any {
                       it.bleMac == bleMac
                   }

                    Timber.e("------是否包含="+isHas)
                    if(isHas == true){

                        return
                    }

                    typeMap.forEach {
                        val keyStr = it.key
                        val tempK = Utils.changeStr(keyStr)
                        val scanRecord = recordStr.lowercase(Locale.ROOT)
                        val front = scanRecord.contains(keyStr.lowercase(Locale.ROOT))
                        val back = scanRecord.contains(tempK.lowercase(Locale.ROOT))
                        Timber.e("----转换="+tempK)
                        if(front || back){
                            //判断少于40个设备就不添加了
                            if (repeatList?.size!! > 40) {
                                return
                            }
                            if(!repeatList!!.contains(p0.address)){
                                p0.address?.let { repeatList?.add(it) }
                                list?.add(BleBean(p0.device, p0.rssi,keyStr,scanRecord))
                                list?.sortBy {
                                    Math.abs(it.rssi)
                                }
                            }

                        }
                    }

                    adapter?.notifyDataSetChanged()
                    return
                }

                typeMap.forEach {
                    val keyStr = it.key
                    val tempK = Utils.changeStr(keyStr)
                    val scanRecord = recordStr.lowercase(Locale.ROOT)
                    val front = scanRecord.contains(keyStr.lowercase(Locale.ROOT))
                    val back = scanRecord.contains(tempK.lowercase(Locale.ROOT))
                    Timber.e("----转换="+tempK)
                    if(front || back){
                        //判断少于40个设备就不添加了
                        if (repeatList?.size!! > 40) {
                            return
                        }
                        if(!repeatList!!.contains(p0.address)){
                            p0.address?.let { repeatList?.add(it) }
                            list?.add(BleBean(p0.device, p0.rssi,keyStr,scanRecord))
                            list?.sortBy {
                                Math.abs(it.rssi)
                            }
                        }

                    }
                }
                adapter?.notifyDataSetChanged()

            }

            override fun onSearchStopped() {

            }

            override fun onSearchCanceled() {

            }

        }, 20000 * 1000, 1)
    }

    override fun onDestroy() {
        super.onDestroy()
        BaseApplication.getBaseApplication().isActivityScan = false
        BaseApplication.getInstance().bleManager.stopScan()
        unregisterReceiver(broadcastReceiver)
    }



    //提示请连接的dialog
    private fun showConnDialogView(bean: BleBean,service : ConnStatusService,isBind : Boolean,index : Int){
        val dialog = DeleteDeviceDialog(this, com.bonlala.base.R.style.BaseDialogTheme)
        dialog.show()
        dialog.setTitleTxt("是否断开当前连接，并连接此设备?")
        dialog.setOnCommClickListener(object : OnCommItemClickListener{
            override fun onItemClick(position: Int) {
                dialog.dismiss()
                if(position == 0x01){   //确定
                    if(isBind){
                        bindList?.get(index)?.connStatus = ConnStatus.CONNECTING
                        bindAdapter?.notifyItemChanged(index)
                    }else{
                        list?.get(index)?.connStatus = ConnStatus.CONNECTING
                        adapter?.notifyItemChanged(index)
                        showDialog("连接中..")
                    }

                    handlers.sendEmptyMessage(0x01)
                    handlers.postDelayed(Runnable {

                        service.connDeviceBack(
                            bean.bleName, bean.bleMac
                        ) { mac, status ->
                            hideDialog()

                            DbManager.getInstance().saveUserBindDevice(bean.bleName,bean.bleMac,BikeUtils.getFormatDate(System.currentTimeMillis(),"yyyy-MM-dd HH:mm:ss"))
                            MmkvUtils.saveProductNumberCode(bean.productNumber)
                            MmkvUtils.saveConnDeviceMac(mac)
                            MmkvUtils.saveConnDeviceName(bean.bleName)
                            BaseApplication.getBaseApplication().connStatus = ConnStatus.CONNECTED

                            dealScanDevice()
                        }
                    },2000)

                }
            }

        })
        val window = dialog.window
        val windowLayout = window?.attributes
        val metrics2: DisplayMetrics = resources.displayMetrics
        val widthW: Int = metrics2.widthPixels

        windowLayout?.width = widthW
        windowLayout?.gravity = Gravity.BOTTOM
        window?.attributes = windowLayout
    }



    private val broadcastReceiver : BroadcastReceiver = object : BroadcastReceiver(){
        override fun onReceive(p0: Context?, p1: Intent?) {
            val action = p1?.action
            Timber.e("---------acdtion="+action)
            if(action == BleConstant.BLE_START_SCAN_ACTION){ //开始连接了


            }
            if(action == BleConstant.BLE_CONNECTED_ACTION){
                ToastUtils.show(resources.getString(R.string.string_conn_success))
                BaseApplication.getBaseApplication().connStatus = ConnStatus.CONNECTED
                val saveMac = MmkvUtils.getConnDeviceMac()
                if(!BikeUtils.isEmpty(saveMac)){
                    bindList?.forEach {
                        if(it.bleMac ==saveMac){
                            it.connStatus = ConnStatus.CONNECTED
                        }
                    }
                    bindAdapter?.notifyDataSetChanged()
                }


            }
            if(action == BleConstant.BLE_DIS_CONNECT_ACTION){
                ToastUtils.show(resources.getString(R.string.string_conn_disconn))

            }
        }

    }

}