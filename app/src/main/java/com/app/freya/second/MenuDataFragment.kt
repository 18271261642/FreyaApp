package com.app.freya.second

import android.util.DisplayMetrics
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView
import com.app.freya.BaseApplication
import com.app.freya.R
import com.app.freya.action.TitleBarFragment
import com.app.freya.adapter.OnCommItemClickListener
import com.app.freya.dialog.DeleteDeviceDialog
import com.app.freya.utils.BikeUtils
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

        //切换设备
        findViewById<LinearLayout>(R.id.changeDeviceLayout).setOnClickListener {
            if(BikeUtils.isEmpty(MmkvUtils.getConnDeviceMac())){
                startActivity(SecondScanActivity::class.java)
                return@setOnClickListener
            }
            showConnDialog()
        }
    }

    override fun initData() {
        //homeTempView?.setTemperatures("--","--","--")

        homeTempView?.setDefaultValue()
    }


    override fun onActivityResume() {
        super.onActivityResume()
        homeTimeStateTv?.text = TimeUtils.getTimeByNow(attachActivity)+" "+MmkvUtils.getConnDeviceName()
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


    //提示请连接的dialog
    private fun showConnDialog(){
        val dialog = DeleteDeviceDialog(attachActivity, com.bonlala.base.R.style.BaseDialogTheme)
        dialog.show()
        dialog.setTitleTxt("是否切换设备?")
        dialog.setOnCommClickListener(object : OnCommItemClickListener {
            override fun onItemClick(position: Int) {
                dialog.dismiss()
                if(position == 0x01){   //确定
                    startActivity(SecondScanActivity::class.java)
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
}