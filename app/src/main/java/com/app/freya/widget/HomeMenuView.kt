package com.app.freya.widget

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.widget.LinearLayout
import com.app.freya.R
import com.app.freya.adapter.OnCommItemClickListener
import com.hjq.shape.layout.ShapeLinearLayout

class HomeMenuView : LinearLayout ,OnClickListener{


    private var onClick : OnCommItemClickListener ?= null

    fun setOnItemClick(click : OnCommItemClickListener){
        this.onClick = click
    }

    private var menuDataNormalLayout : LinearLayout ?= null
    private var menuSettingNormalLayout : LinearLayout ?= null
    private var menuDeviceNormalLayout : LinearLayout ?= null


    private var menuDataCheckedLayout : ShapeLinearLayout ?= null
    private var menuSettingCheckedLayout : ShapeLinearLayout ?= null
    private var menuDeviceCheckedLayout : ShapeLinearLayout ?= null



    constructor(context: Context) : super (context){

    }

    constructor(context: Context,attributeSet: AttributeSet) : super (context,attributeSet){
        initViews(context)
    }

    constructor(context: Context,attributeSet: AttributeSet,defaultValue : Int) : super (context,attributeSet,defaultValue){
        initViews(context)
    }


    private fun initViews(context: Context){
        val view = LayoutInflater.from(context).inflate(R.layout.layout_menu,this,true)
        menuDataNormalLayout = view.findViewById(R.id.menuDataNormalLayout)
        menuSettingNormalLayout = view.findViewById(R.id.menuSettingNormalLayout)
        menuDeviceNormalLayout = view.findViewById(R.id.menuDeviceNormalLayout)

        menuDataCheckedLayout = view.findViewById(R.id.menuDataCheckedLayout)
        menuSettingCheckedLayout = view.findViewById(R.id.menuSettingCheckedLayout)
        menuDeviceCheckedLayout = view.findViewById(R.id.menuDeviceCheckedLayout)


        menuDataNormalLayout?.setOnClickListener(this)

        menuSettingNormalLayout?.setOnClickListener(this)

        menuDeviceNormalLayout?.setOnClickListener(this)

    }

    override fun onClick(v: View?) {
        val id = v?.id
        if(id == R.id.menuDataNormalLayout){
            setChecked(0)
        }

        if(id == R.id.menuSettingNormalLayout){
            setChecked(1)
        }

        if(id == R.id.menuDeviceNormalLayout){
            setChecked(2)
        }

    }


    private fun setChecked(index : Int){

        menuDataCheckedLayout?.visibility = if(index == 0) View.VISIBLE else View.INVISIBLE

        menuSettingCheckedLayout?.visibility = if(index == 1) View.VISIBLE else View.INVISIBLE

        menuDeviceCheckedLayout?.visibility = if(index == 2) View.VISIBLE else View.INVISIBLE

        onClick?.onItemClick(index)

    }
}