package com.app.freya.dialog

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatDialog
import com.app.freya.R
import com.bonlala.widget.view.SmartTextView

class ShowProgressDialog : AppCompatDialog {


    private var dialogMessage : SmartTextView ?= null

    constructor(context: Context) : super (context){

    }

    constructor(context: Context,theme : Int) : super (context, theme){

    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_progress_view)

        dialogMessage = findViewById(R.id.dialogMessage)

    }


    //设置显示的文字
    fun setShowMsg(msg : String){
        dialogMessage?.text = msg
    }

}