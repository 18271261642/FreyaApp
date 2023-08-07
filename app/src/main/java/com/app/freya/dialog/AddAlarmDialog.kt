package com.app.freya.dialog

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatDialog
import com.app.freya.R
import com.bonlala.widget.view.StringScrollPicker
import kotlin.math.min

class AddAlarmDialog : AppCompatDialog {


    private var alarmHourScrollPicker : StringScrollPicker ?= null
    private var alarmMinuteScrollPicker : StringScrollPicker ?= null


    constructor(context: Context) : super (context){

    }


    constructor(context: Context, theme : Int) : super (context, theme){

    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_edit_alarm_layout)

        initViews()
        initData()
    }

    private fun initViews(){
        alarmHourScrollPicker  = findViewById(R.id.alarmHourScrollPicker)
        alarmMinuteScrollPicker = findViewById(R.id.alarmMinuteScrollPicker)


    }


    private fun initData(){
        val houList = ArrayList<String>()
        for(i in 0 until 24){
            houList.add(String.format("%02d",i))
        }

        alarmHourScrollPicker?.data = houList as List<CharSequence>?

        val minuteList = ArrayList<String>()

        for(k in 0 until 60){
            minuteList.add(String.format("%02d",k))
        }
        alarmMinuteScrollPicker?.data = minuteList as List<CharSequence>?
    }
}