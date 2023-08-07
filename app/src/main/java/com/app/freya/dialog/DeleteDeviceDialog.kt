package com.app.freya.dialog

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.View.OnClickListener
import android.widget.TextView
import androidx.appcompat.app.AppCompatDialog
import com.app.freya.R
import com.app.freya.adapter.OnCommItemClickListener
import com.hjq.shape.view.ShapeTextView

/**
 * 删除设备的dialog
 */
class DeleteDeviceDialog : AppCompatDialog,OnClickListener{



    //item点击
    private var onItemClickListener : OnCommItemClickListener?= null

    fun setOnCommClickListener(onclick : OnCommItemClickListener){
        this.onItemClickListener = onclick
    }


    //取消
    private var dialogDeleteCancelTv : ShapeTextView ?= null
    //确定
    private var dialogDeleteConfirmTv : ShapeTextView ?= null


    private var deleteDialogTitleTv : TextView ?= null


    constructor(context: Context) : super (context){

    }


    constructor(context: Context, theme : Int) : super (context, theme){

    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_second_delete_device_layout)

        dialogDeleteCancelTv = findViewById(R.id.deleteDeviceCancelTv)
        dialogDeleteConfirmTv = findViewById(R.id.deleteDeviceConfirmTv)
        deleteDialogTitleTv = findViewById(R.id.deleteDialogTitleTv)

        dialogDeleteCancelTv?.setOnClickListener(this)
        dialogDeleteConfirmTv?.setOnClickListener(this)

    }

    override fun onClick(v: View?) {
       val id = v?.id
        //取消
        if(id == R.id.deleteDeviceCancelTv){
            onItemClickListener?.onItemClick(0x00)
        }
        //确定
        if(id == R.id.deleteDeviceConfirmTv){
            onItemClickListener?.onItemClick(0x01)
        }
    }


    fun setTitleTxt(title : String){
        deleteDialogTitleTv?.text = title
    }


    fun setConfirmAndCancelTxt(confirmTxt : String,cancelTv : String){
        dialogDeleteCancelTv?.text = cancelTv
        dialogDeleteConfirmTv?.text = confirmTxt
    }


    //设置取消按钮颜色
    fun setCancelBgColor(color : Int){
        dialogDeleteCancelTv!!.shapeDrawableBuilder.setSolidColor(color).intoBackground()
        dialogDeleteCancelTv!!.shapeDrawableBuilder.setSolidPressedColor(Color.parseColor("#90292E3C")).intoBackground()
    }


    fun setConfirmBgColor(color: Int){
        dialogDeleteConfirmTv!!.shapeDrawableBuilder.setSolidColor(color).intoBackground()
        dialogDeleteConfirmTv!!.shapeDrawableBuilder.setSolidPressedColor(Color.parseColor("#90292E3C")).intoBackground()
    }
}