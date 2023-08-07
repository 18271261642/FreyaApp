package com.app.freya.dialog


import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import androidx.appcompat.app.AppCompatDialog
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.freya.BaseApplication
import com.app.freya.R
import com.app.freya.adapter.OnCommItemClickListener
import com.app.freya.adapter.ScanDeviceAdapter
import com.app.freya.bean.BleBean
import com.app.freya.ble.ConnStatus
import com.app.freya.utils.BikeUtils
import com.app.freya.utils.MmkvUtils
import com.blala.blalable.Utils
import com.blala.blalable.listener.BleConnStatusListener
import com.inuker.bluetooth.library.search.SearchResult
import com.inuker.bluetooth.library.search.response.SearchResponse
import timber.log.Timber
import java.util.*

/**
 * Created by Admin
 *Date 2023/1/12
 */
class DialogScanDeviceView : AppCompatDialog {

    private var list : MutableList<BleBean> ?= null
    private var scanDeviceAdapter : ScanDeviceAdapter ?= null
    private var recyclerView : RecyclerView ?= null

    //用于去重的list
    private var repeatList : MutableList<String> ?= null



    private val handlers : Handler = object : Handler(Looper.getMainLooper()){
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            if(msg.what == 0x00){
                BaseApplication.getBaseApplication().bleOperate.stopScanDevice()
            }
        }
    }

    private var onClick : OnCommItemClickListener ?= null

    fun setOnDialogClickListener(onCommItemClickListener: OnCommItemClickListener){
        this.onClick = onCommItemClickListener
    }

    constructor(context: Context) : super (context){

    }

    constructor(context: Context,theme : Int) : super (context,theme){

    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_scan_device_layout)

        initViews()
    }

    private fun initViews(){
        recyclerView = findViewById(R.id.scanRecyclerView)
        val linearLayoutManager = LinearLayoutManager(context)
        linearLayoutManager.orientation = LinearLayoutManager.VERTICAL
        recyclerView?.addItemDecoration(DividerItemDecoration(context,DividerItemDecoration.VERTICAL))
        recyclerView?.layoutManager = linearLayoutManager
        list = mutableListOf()
        scanDeviceAdapter = ScanDeviceAdapter(context, list!!)
        recyclerView?.adapter = scanDeviceAdapter
        repeatList = mutableListOf()
        scanDeviceAdapter!!.setOnItemClick(onItemClick)
    }


    private val onItemClick : OnCommItemClickListener =
        OnCommItemClickListener { position ->
            onClick?.onItemClick(0x00)
            val service = BaseApplication.getBaseApplication().connStatusService
            val bean = list?.get(position)
            if (bean != null) {
                handlers.sendEmptyMessageDelayed(0x00,500)
                service.connDeviceBack(bean.bluetoothDevice.name,bean.bluetoothDevice.address,object :
                    BleConnStatusListener {
                    override fun onConnectStatusChanged(mac: String?, status: Int) {
                        MmkvUtils.saveConnDeviceMac(mac)
                        MmkvUtils.saveConnDeviceName(bean.bluetoothDevice.name)
                        BaseApplication.getBaseApplication().connStatus = ConnStatus.CONNECTED
                        onClick?.onItemClick(0x01)
                        dismiss()
                    }
                })
            }
        }

    //开始扫描
     fun startScan(){

        BaseApplication.getBaseApplication().bleOperate.scanBleDevice(object : SearchResponse{

            override fun onSearchStarted() {

            }

            override fun onDeviceFounded(p0: SearchResult) {
                if(p0.getScanRecord() == null || p0.getScanRecord().isEmpty())
                    return
               // Timber.e("--------扫描="+p0.name+" "+Utils.formatBtArrayToString(p0.getScanRecord()))

                    val recordStr = Utils.formatBtArrayToString(p0.getScanRecord())
                    val bleName = p0.name

                if(BikeUtils.isEmpty(bleName) || bleName.equals("NULL") || BikeUtils.isEmpty(p0.address))
                    return
                if(repeatList?.contains(p0.address) == true)
                    return
                //030543
                if(!BikeUtils.isEmpty(recordStr) && recordStr.toLowerCase(Locale.ROOT).contains("c003")){
                    //判断少于40个设备就不添加了
                    if(repeatList?.size!! >40){
                        return
                    }
                    p0.address?.let { repeatList?.add(it) }
                    list?.add(BleBean(p0.device,p0.rssi))
                    list?.sortBy {
                        Math.abs(it.rssi)
                    }

                    scanDeviceAdapter?.notifyDataSetChanged()
                }

            }

            override fun onSearchStopped() {

            }

            override fun onSearchCanceled() {

            }

        },15 * 1000,1)
    }

    override fun dismiss() {
        super.dismiss()
        Timber.e("------d关闭=====")
        BaseApplication.getBaseApplication().bleOperate.stopScanDevice()
    }
}