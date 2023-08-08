package com.app.freya.second

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.Message
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.freya.BaseApplication
import com.app.freya.R
import com.app.freya.action.AppActivity
import com.app.freya.adapter.OnCommItemClickListener
import com.app.freya.adapter.SecondScanAdapter
import com.app.freya.bean.BleBean
import com.app.freya.ble.ConnStatus
import com.app.freya.utils.BikeUtils
import com.app.freya.utils.BonlalaUtils
import com.app.freya.utils.MmkvUtils
import com.blala.blalable.Utils
import com.hjq.permissions.XXPermissions
import com.inuker.bluetooth.library.search.SearchResult
import com.inuker.bluetooth.library.search.response.SearchResponse
import java.util.Locale

/**
 * Created by Admin
 *Date 2023/7/12
 */
class SecondScanActivity : AppActivity() {

    private var secondScanRy: RecyclerView? = null

    private var adapter: SecondScanAdapter? = null
    private var list: MutableList<BleBean>? = null

    //用于去重的list
    private var repeatList: MutableList<String>? = null

    private val handlers: Handler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            if (msg.what == 0x00) {
                BaseApplication.getBaseApplication().bleOperate.stopScanDevice()
            }
        }
    }

    override fun getLayoutId(): Int {
        return R.layout.activity_second_scan_layout
    }

    override fun initView() {
        secondScanRy = findViewById(R.id.secondScanRy)
        val linearLayoutManager = LinearLayoutManager(context)
        linearLayoutManager.orientation = LinearLayoutManager.VERTICAL
        secondScanRy?.layoutManager = linearLayoutManager
        list = mutableListOf()
        adapter = SecondScanAdapter(context, list!!)
        secondScanRy?.adapter = adapter
        repeatList = mutableListOf()
        adapter!!.setOnItemClick(onItemClick)
    }

    override fun initData() {
        verifyScanFun(false)
    }


    //判断是否有位置权限了，没有请求权限
    private fun verifyScanFun(isReconn: Boolean) {

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
                verifyScanFun(isReconn)
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
        val isOpenBle = BonlalaUtils.isOpenBlue(this@SecondScanActivity)
        if (!isOpenBle) {
            BonlalaUtils.openBluetooth(this)
            return
        }

        if (isReconn) {
            val mac = MmkvUtils.getConnDeviceMac()
            if (BikeUtils.isEmpty(mac))
                return
            BaseApplication.getBaseApplication().connStatusService.autoConnDevice(mac, false)

        } else {

            startScan()
        }

    }


    @SuppressLint("MissingPermission")
    private val onItemClick: OnCommItemClickListener =
        OnCommItemClickListener { position ->
            val service = BaseApplication.getBaseApplication().connStatusService
            val bean = list?.get(position)
            if (bean != null) {
                showDialog("连接中..")
                handlers.sendEmptyMessageDelayed(0x00, 500)
                service.connDeviceBack(
                    bean.bluetoothDevice.name, bean.bluetoothDevice.address
                ) { mac, status ->
                    hideDialog()
                    MmkvUtils.saveConnDeviceMac(mac)
                    MmkvUtils.saveConnDeviceName(bean.bluetoothDevice.name)
                    BaseApplication.getBaseApplication().connStatus = ConnStatus.CONNECTED
                    finish()
                }
            }
        }


    //开始扫描
    private fun startScan() {

        BaseApplication.getBaseApplication().bleOperate.scanBleDevice(object : SearchResponse {

            override fun onSearchStarted() {

            }

            override fun onDeviceFounded(p0: SearchResult) {
                if (p0.getScanRecord() == null || p0.getScanRecord().isEmpty())
                    return
                // Timber.e("--------扫描="+p0.name+" "+Utils.formatBtArrayToString(p0.getScanRecord()))

                val recordStr = Utils.formatBtArrayToString(p0.getScanRecord())
                val bleName = p0.name

                if (BikeUtils.isEmpty(bleName) || bleName.equals("NULL") || BikeUtils.isEmpty(p0.address))
                    return
                if (repeatList?.contains(p0.address) == true)
                    return
                //030543  && recordStr.toLowerCase(Locale.ROOT)
                //                        .contains("c003")
                val isContains = recordStr.toLowerCase(Locale.ROOT).contains("03c0") ||
                        recordStr.toLowerCase(Locale.ROOT).contains("c003")  ||
                        recordStr.toLowerCase(Locale.ROOT).contains("c013") || recordStr.toLowerCase(Locale.ROOT).contains("13c0")
                if (!BikeUtils.isEmpty(recordStr) && isContains) {

                    //判断少于40个设备就不添加了
                    if (repeatList?.size!! > 40) {
                        return
                    }
                    p0.address?.let { repeatList?.add(it) }
                    list?.add(BleBean(p0.device, p0.rssi))
                    list?.sortBy {
                        Math.abs(it.rssi)
                    }

                    adapter?.notifyDataSetChanged()
                }

            }

            override fun onSearchStopped() {

            }

            override fun onSearchCanceled() {

            }

        }, 15 * 1000, 1)
    }

    override fun onDestroy() {
        super.onDestroy()
        BaseApplication.getInstance().bleManager.stopScan()
    }
}