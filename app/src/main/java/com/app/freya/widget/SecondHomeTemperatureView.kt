package com.app.freya.widget

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.TextView
import com.app.freya.R
import com.app.freya.utils.SpannableUtils
import com.bonlala.widget.view.CircleProgress

class SecondHomeTemperatureView : LinearLayout {


    //cpu
    private var cpuTempTv : TextView ?= null
    //gpu
    private var gpuTempTv : TextView ?= null
    //hhd
    private var hdTempTv : TextView ?= null


    //电量
    private var homeBatteryCircleProgress : CircleProgress ?= null
    //kpm
    private var homeKpmCircleProgress : CircleProgress ?= null

    constructor(context: Context) : super (context){

    }

    constructor(context: Context, attributeSet: AttributeSet) : super (context,attributeSet){
        initViews(context)
    }

    constructor(context: Context, attributeSet: AttributeSet, defaultValue : Int) : super (context,attributeSet,defaultValue){
        initViews(context)
    }



    private fun initViews(context: Context){
        val view = LayoutInflater.from(context).inflate(R.layout.view_home_chart_layout,this,true)

        cpuTempTv = view.findViewById(R.id.cpuTempTv)
        gpuTempTv = view.findViewById(R.id.gpuTempTv)
        hdTempTv = view.findViewById(R.id.hdTempTv)
        homeBatteryCircleProgress = view.findViewById(R.id.homeBatteryCircleProgress)
        homeKpmCircleProgress = view.findViewById(R.id.homeKpmCircleProgress)


        val colors = IntArray(2)// arrayOf(Color.parseColor("#04B9AB"),Color.parseColor("#F55B38"))
        colors[0] = Color.parseColor("#04B9AB")
        colors[1] = Color.parseColor("#F55B38")
        homeBatteryCircleProgress?.setmGradientColors(colors)
        homeBatteryCircleProgress?.maxValue = 100F

        val colors2 = IntArray(2)// arrayOf(Color.parseColor("#04B9AB"),Color.parseColor("#F55B38"))
        colors2[0] = Color.parseColor("#04B9AB")
        colors2[1] = Color.parseColor("#F55B38")
        homeKpmCircleProgress?.setmGradientColors(colors)
        homeKpmCircleProgress?.maxValue = 100F



    }


    //默认值，无数据状态
    fun setDefaultValue(){
        cpuTempTv?.text = context.resources.getString(R.string.string_no_data)
        gpuTempTv?.text = context.resources.getString(R.string.string_no_data)
        hdTempTv?.text = context.resources.getString(R.string.string_no_data)

        homeBatteryCircleProgress?.isShowNoData = true
        homeBatteryCircleProgress?.value = 80F
        homeKpmCircleProgress?.isShowNoData = true
        homeKpmCircleProgress?.value = 75F
    }



    //设置温度
    fun setTemperatures(cpuT : String,gpuT : String,hdT : String){
        cpuTempTv?.text = SpannableUtils.getTargetType(cpuT,"℃")
        gpuTempTv?.text = SpannableUtils.getTargetType(gpuT,"℃")
        hdTempTv?.text = SpannableUtils.getTargetType(hdT,"℃")
    }


    //设置电量
    fun setBatteryValue(battery : Int){
        homeBatteryCircleProgress?.value = battery.toFloat()
    }
}