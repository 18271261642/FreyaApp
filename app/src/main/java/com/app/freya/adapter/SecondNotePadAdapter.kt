package com.app.freya.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.app.freya.R
import com.app.freya.bean.NoteBookBean
import com.app.freya.utils.BikeUtils


/**
 * 键盘二代记事本adapter
 * Created by Admin
 *Date 2023/7/4
 */
class SecondNotePadAdapter(val context: Context, val list: MutableList<NoteBookBean>) :
    RecyclerView.Adapter<SecondNotePadAdapter.NoteViewHolder>() {


    //item点击
    private var onItemClickListener : OnCommItemClickListener ?= null

    //长按
    private var onLongClick : OnClickLongListener ?= null



    fun setOnCommClickListener(onclick : OnCommItemClickListener){
        this.onItemClickListener = onclick
    }


    fun setOnLongClickListener(ontLongListener: OnClickLongListener){
        this.onLongClick = ontLongListener
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val view = LayoutInflater.from(context)
            .inflate(R.layout.item_second_note_pad_layout, parent, false)
        return NoteViewHolder(view)
    }
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppViewHolder {
//        val view = LayoutInflater.from(context)
//            .inflate(R.layout.item_second_note_pad_layout, parent, false)
//        return NoteViewHolder(view)
//    }




    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        val bean = list.get(position)
        holder.itemSecondNoteTitleTv.text = bean.noteTitle
        holder.itemSecondTimeTv.text = BikeUtils.getFormatDate(bean.noteTimeLong,"yyyy/MM/dd")
        holder.itemSecondContentTv.text = bean.noteContent

        holder.itemView.setOnClickListener {
            val position = holder.layoutPosition
            onItemClickListener?.onItemClick(position)
        }

        holder.itemView.setOnLongClickListener {
            val position = holder.layoutPosition
            onLongClick?.onLongClick(position)
            true
        }
    }


    class NoteViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        val itemSecondNoteTitleTv: TextView = view.findViewById(R.id.itemSecondNoteTitleTv)
        val itemSecondTimeTv: TextView = view.findViewById(R.id.itemSecondTimeTv)
        val itemSecondContentTv: TextView = view.findViewById(R.id.itemSecondContentTv)


    }
}