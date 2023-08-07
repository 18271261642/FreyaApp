package com.app.freya.dialog

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatDialog
import com.app.freya.R
import com.app.freya.adapter.OnCommItemClickListener
import com.hjq.shape.view.ShapeTextView

/**
 * Created by Admin
 *Date 2023/6/16
 */
class UpgradeDialogView : AppCompatDialog {


    private var dialogUpgradeCancelTv : ShapeTextView ?= null
    private var dialogUpgradeConfirmTv : ShapeTextView ?= null



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
        setContentView(R.layout.dialog_show_upgrade_layout)

        dialogUpgradeCancelTv = findViewById(R.id.dialogUpgradeCancelTv)
        dialogUpgradeConfirmTv = findViewById(R.id.dialogUpgradeConfirmTv)

        dialogUpgradeCancelTv?.setOnClickListener {
            onClick?.onItemClick(0x00)
        }

        dialogUpgradeConfirmTv?.setOnClickListener {
            onClick?.onItemClick(0x01)
        }

    }
}