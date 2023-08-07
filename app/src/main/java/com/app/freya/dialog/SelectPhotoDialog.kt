package com.app.freya.dialog

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatDialog
import com.app.freya.R
import com.app.freya.adapter.OnCommItemClickListener
import com.hjq.shape.view.ShapeTextView

/**
 * Created by Admin
 *Date 2023/1/31
 */
class SelectPhotoDialog : AppCompatDialog ,View.OnClickListener{


    private var onItemClick : OnCommItemClickListener ?= null

    fun setOnSelectListener(onClick : OnCommItemClickListener){
        this.onItemClick = onClick
    }


    private var selectorCameraTv : ShapeTextView ?= null
    private var selectorAlbumTv : ShapeTextView ?= null
    private var selectorCancelTv : ShapeTextView ?= null


    constructor(context: Context) : super (context){

    }


    constructor(context: Context,theme : Int) : super (context, theme){

    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_select_pick)

        findViews()

    }


    private fun findViews(){
        selectorCameraTv = findViewById(R.id.selectorCameraTv)
        selectorAlbumTv = findViewById(R.id.selectorAlbumTv)
        selectorCancelTv = findViewById(R.id.selectorCancelTv)


        selectorCameraTv?.setOnClickListener(this)
        selectorAlbumTv?.setOnClickListener(this)
        selectorCancelTv?.setOnClickListener(this)

    }

    override fun onClick(p0: View?) {
        val id = p0?.id
        when (id){
            //相机
            R.id.selectorCameraTv->{
                onItemClick?.onItemClick(0x00)
            }

            //相册
            R.id.selectorAlbumTv->{
                onItemClick?.onItemClick(0x01)
            }
            //关闭
            R.id.selectorCancelTv->{
                dismiss()
            }
        }
    }
}