package com.app.freya.dialog

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatDialog
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.freya.R
import com.app.freya.adapter.AlarmWeekAdapter
import com.app.freya.bean.AlarmWeekBean
import com.bonlala.widget.view.StringScrollPicker
import com.google.gson.Gson
import com.hjq.bar.OnTitleBarListener
import com.hjq.bar.TitleBar
import timber.log.Timber
import kotlin.math.min

class AddAlarmDialog : AppCompatDialog {


    private var alarmHourScrollPicker : StringScrollPicker ?= null
    private var alarmMinuteScrollPicker : StringScrollPicker ?= null


    private var editAlarmTitleBar : TitleBar ? = null

    private var editAlarmWeekRy : RecyclerView ?= null
    private var adapter : AlarmWeekAdapter ?= null
    private var weekList : MutableList<AlarmWeekBean> ?= null

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
        editAlarmTitleBar = findViewById(R.id.editAlarmTitleBar)

        editAlarmWeekRy = findViewById(R.id.editAlarmWeekRy)
        val gridLayoutManager = GridLayoutManager(context,7)
        editAlarmWeekRy?.layoutManager = gridLayoutManager
        weekList = ArrayList<AlarmWeekBean>()
        adapter = AlarmWeekAdapter(context, weekList as ArrayList<AlarmWeekBean>)
        editAlarmWeekRy?.adapter = adapter

        adapter?.setOnItemClick{
            val bean = weekList?.get(it)

//            weekList?.get(it)?.isChecked = !bean!!.isChecked
//
            Timber.e("------周="+it+Gson().toJson(weekList))
            //adapter?.notifyItemChanged(it)


        }





        editAlarmTitleBar?.setOnTitleBarListener(object : OnTitleBarListener{
            override fun onLeftClick(view: View?) {
                dismiss()
            }

            override fun onTitleClick(view: View?) {

            }

            override fun onRightClick(view: View?) {

            }

        })

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


        weekList?.clear()
        weekList?.addAll(getWeekList())
        adapter?.notifyDataSetChanged()








    }


    private fun getWeekList() : MutableList<AlarmWeekBean>{
        val list = ArrayList<AlarmWeekBean>()
        val w1 = AlarmWeekBean(context.resources.getString(R.string.string_monday),2)
        val w2 = AlarmWeekBean(context.resources.getString(R.string.string_tuesday),4)

        val w3 = AlarmWeekBean(context.resources.getString(R.string.string_wednesday),8)
        val w4 = AlarmWeekBean(context.resources.getString(R.string.string_thursday),16)
        val w5 = AlarmWeekBean(context.resources.getString(R.string.string_friday),32)
        val w6 = AlarmWeekBean(context.resources.getString(R.string.string_saturday),64)
        val w7 = AlarmWeekBean(context.resources.getString(R.string.string_sunday),1)



        list.add(w1)
        list.add(w2)
        list.add(w3)
        list.add(w4)
        list.add(w5)
        list.add(w6)
        list.add(w7)
        return list
    }
}