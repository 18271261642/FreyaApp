package com.app.freya.second

import android.widget.TextView
import com.app.freya.BaseApplication
import com.app.freya.R
import com.app.freya.action.TitleBarFragment
import com.app.freya.utils.MmkvUtils
import com.app.freya.utils.TimeUtils
import com.app.freya.widget.SecondHomeTemperatureView
import com.blala.blalable.listener.OnSystemDataListener
import com.hjq.shape.layout.ShapeLinearLayout

/**
 * 数据页面
 */
class MenuDataFragment : TitleBarFragment<SecondHomeActivity>()
{


    private var homeTempView : SecondHomeTemperatureView ?= null
    private var homeTimeStateTv : TextView ?= null


    companion object{

        fun getInstance() : MenuDataFragment{
            return MenuDataFragment()
        }
    }


    override fun getLayoutId(): Int {
        return R.layout.fragment_menu_data_layout
    }

    override fun initView() {
        homeTempView = findViewById(R.id.homeTempView)
        homeTimeStateTv = findViewById(R.id.homeTimeStateTv)
    }

    override fun initData() {
        homeTempView?.setTemperatures("--","--","--")
    }


    override fun onFragmentResume(first: Boolean) {
        super.onFragmentResume(first)
        homeTimeStateTv?.text = TimeUtils.getTimeByNow(attachActivity)+" "+MmkvUtils.getConnDeviceName()
        //homeTempView?.setBatteryValue(88)
        BaseApplication.getBaseApplication().bleOperate.getDeviceSystemData(object : OnSystemDataListener{
            override fun onSysData(
                circleSpeed: Int,
                batteryValue: Int,
                cpuTemperatureC: Int,
                cpuTemperatureF: Int,
                gpuTemC: Int,
                gpuTemF: Int,
                hardTemC: Int,
                hardTemF: Int
            ) {
                homeTempView?.setBatteryValue(batteryValue)
            }

        })
    }

}