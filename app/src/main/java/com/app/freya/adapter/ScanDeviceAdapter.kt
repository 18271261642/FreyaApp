package com.app.freya.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.app.freya.R
import com.app.freya.bean.BleBean

/**
 * Created by Admin
 *Date 2023/1/12
 */
class ScanDeviceAdapter(private val context: Context,private val list : MutableList<BleBean>) : RecyclerView.Adapter<ScanDeviceAdapter.ScanDeviceViewHolder>(){

    private var onItemClickListener : OnCommItemClickListener ?= null

    fun setOnItemClick(click : OnCommItemClickListener){
        this.onItemClickListener = click
    }


    class ScanDeviceViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView){
         var itemScanName  = itemView.findViewById<TextView>(R.id.itemScanName)
         var itemScanMac = itemView.findViewById<TextView>(R.id.itemScanMac)
         var itemScanStatusTv = itemView.findViewById<TextView>(R.id.itemScanStatusTv)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScanDeviceViewHolder {
       val view = LayoutInflater.from(context).inflate(R.layout.item_scan_device,parent,false)
        return ScanDeviceViewHolder(view)
    }

    @SuppressLint("MissingPermission")
    override fun onBindViewHolder(holder: ScanDeviceViewHolder, position: Int) {

        holder.itemScanName.text = list[position].bluetoothDevice.name
        holder.itemScanMac.text = list[position].bluetoothDevice.address

        holder.itemView.setOnClickListener {
            val position = holder.layoutPosition
            onItemClickListener?.onItemClick(position)
        }
    }

    override fun getItemCount(): Int {
       return list.size
    }
}