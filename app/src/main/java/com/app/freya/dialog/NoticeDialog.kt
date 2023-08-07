package com.app.freya.dialog

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatDialog
import com.app.freya.R
import com.app.freya.adapter.OnCommItemClickListener
import com.hjq.shape.view.ShapeTextView

class NoticeDialog : AppCompatDialog {


    private var dialogNoticeCancelTv : ShapeTextView ?= null
    private var dialogNoticeConfirmTv : ShapeTextView ?= null


    private var onClick : OnCommItemClickListener?= null

    fun setOnDialogClickListener(onCommItemClickListener: OnCommItemClickListener){
        this.onClick = onCommItemClickListener
    }


    constructor(context: Context) : super (context){

    }


    constructor(context: Context, theme : Int) : super (context, theme){

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_open_notification_layout)

        initViews()

    }


    private fun initViews(){
        dialogNoticeCancelTv = findViewById(R.id.dialogNoticeCancelTv)
        dialogNoticeConfirmTv = findViewById(R.id.dialogNoticeConfirmTv)

        dialogNoticeCancelTv?.setOnClickListener {
            onClick?.onItemClick(0x01)
        }

        dialogNoticeConfirmTv?.setOnClickListener {
            onClick?.onItemClick(0x00)
        }
    }
}