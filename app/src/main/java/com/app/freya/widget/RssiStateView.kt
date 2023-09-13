package com.app.freya.widget

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.LinearLayout
import com.app.freya.R

class RssiStateView : LinearLayout {


    private var rssiStateImageView : ImageView ?= null


    constructor(context: Context) : super (context){
        intViews(context)
    }

    constructor(context: Context,atrrs : AttributeSet) :super (context,atrrs){
        intViews(context)
    }



    private fun intViews(context: Context){
        val view = LayoutInflater.from(context).inflate(R.layout.rssi_view,this,true)

        rssiStateImageView = view.findViewById(R.id.rssiStateImageView)

    }


    //设置信号值
    fun setRssiValue(value : Int){
        if(value == 0){
            rssiStateImageView?.setImageResource(R.mipmap.ic_bind_img)
            return
        }

        if(value<=65){
            rssiStateImageView?.setImageResource(R.mipmap.ic_rssi_full)
            return
        }

        if(value<=70){
            rssiStateImageView?.setImageResource(R.mipmap.ic_rssi_3)
            return
        }
        if(value<=75){
            rssiStateImageView?.setImageResource(R.mipmap.ic_rssi_2)
            return
        }

        if(value<=80){
            rssiStateImageView?.setImageResource(R.mipmap.ic_rssi_1)
            return
        }
        if(value<=85){
            rssiStateImageView?.setImageResource(R.mipmap.ic_rssi_0)
            return
        }

        invalidate()
    }
}