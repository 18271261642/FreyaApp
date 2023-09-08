package com.app.freya.adapter

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Adapter
import com.app.freya.R
import com.app.freya.bean.AlarmWeekBean
import com.google.gson.Gson
import com.hjq.shape.view.ShapeTextView
import timber.log.Timber

class AlarmWeekAdapter(private val context: Context,private val list : MutableList<AlarmWeekBean>) : Adapter<AlarmWeekAdapter.WeekViewHolder>(){

    private var onClick : OnCommItemClickListener ?=null

    fun setOnItemClick(c : OnCommItemClickListener){
        this.onClick = c
    }


    class WeekViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val tv = itemView.findViewById<ShapeTextView>(R.id.itemEditAlarmWeekTv)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WeekViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_edit_alarm_week_layout,parent,false)
        return WeekViewHolder(view)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: WeekViewHolder, position: Int) {
       val weekBean = list[position]
        Timber.e("------item="+Gson().toJson(weekBean))

        holder.tv.text = weekBean.weekName
        val bgTv = holder.tv
        if(weekBean.isChecked){
            bgTv.shapeDrawableBuilder.setSolidColor(Color.parseColor("#17A89B") ).intoBackground()

        }else{
            bgTv.shapeDrawableBuilder.setSolidColor(Color.parseColor("#00000000")).intoBackground()

        }


        holder.itemView.setOnClickListener {
            weekBean.isChecked = weekBean.isChecked != true
            notifyDataSetChanged()
            onClick?.onItemClick(position)
        }
    }
}