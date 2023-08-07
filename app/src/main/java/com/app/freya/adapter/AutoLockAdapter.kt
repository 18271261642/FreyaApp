package com.app.freya.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Adapter
import com.app.freya.R
import com.app.freya.bean.AutoLockBean

class AutoLockAdapter(private val context: Context, val list: MutableList<AutoLockBean>) :
    Adapter<AutoLockAdapter.AutoHolder>() {


    //item点击
    private var onItemClickListener : OnCommItemClickListener?= null

    fun setOnCommClickListener(onclick : OnCommItemClickListener){
        this.onItemClickListener = onclick
    }
    inner class AutoHolder(view: View) : RecyclerView.ViewHolder(view) {
        val itemSecondAUtoCheckTv: TextView = view.findViewById(R.id.itemSecondAUtoCheckTv)
        val itemSecondAutoCheckBox: CheckBox = view.findViewById(R.id.itemSecondAutoCheckBox)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AutoHolder {
        val view = LayoutInflater.from(context)
            .inflate(R.layout.item_second_auto_lock_layout, parent, false)
        return AutoHolder(view)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: AutoHolder, position: Int) {
        val bean = list.get(position)
        holder.itemSecondAutoCheckBox.setChecked(bean.isChecked())
        val sec: Int = bean.getAutoTime()
        holder.itemSecondAUtoCheckTv.setText(
            if (sec >= 60) (sec / 60).toString() + context.getString(R.string.string_minute_time) else sec.toString() + context.getString(
                R.string.string_second_time
            )
        )


        holder.itemView.setOnClickListener {
            onItemClickListener?.onItemClick(holder.layoutPosition)
        }
    }
}