package com.app.freya.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.app.freya.R
import com.app.freya.bean.NoteBookBean

/**
 * 记事本的adapter
 * Created by Admin
 *Date 2023/1/10
 */
class NoteBookAdapter(private val context: Context,private val list : MutableList<NoteBookBean>) : RecyclerView.Adapter<NoteBookAdapter.NoteBookViewHolder>() {

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


    class NoteBookViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView) {
        val titleTv = itemView.findViewById<TextView>(R.id.itemNoteBookTitleTv)
        val contentTv = itemView.findViewById<TextView>(R.id.itemNoteBookContentTv)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteBookViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_notebook_layout,parent,false)
        return NoteBookViewHolder(view)
    }

    override fun onBindViewHolder(holder: NoteBookViewHolder, position: Int) {
        holder.titleTv.text = list[position].noteTitle
        holder.contentTv.text = list[position].noteContent

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

    override fun getItemCount(): Int {
       return list.size
    }
}