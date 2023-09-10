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
import com.app.freya.widget.RssiStateView

/**
 * Created by Admin
 *Date 2023/7/12
 */
class SecondScanAdapter(private val context: Context,private val list : MutableList<BleBean>) : RecyclerView.Adapter<SecondScanAdapter.ScanDeviceViewHolder>() {

    private var onItemClickListener : OnCommItemClickListener ?= null

    fun setOnItemClick(click : OnCommItemClickListener){
        this.onItemClickListener = click
    }


    class ScanDeviceViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView){
        var itemScanName  = itemView.findViewById<TextView>(R.id.itemSecondNameTv)
        val itemSecondMacTv = itemView.findViewById<TextView>(R.id.itemSecondMacTv)
        val itemRecordTv = itemView.findViewById<TextView>(R.id.itemRecordTv)
        val itemProductNameTv = itemView.findViewById<TextView>(R.id.itemProductNameTv)
        val rssiView = itemView.findViewById<RssiStateView>(R.id.itemRssiTv)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScanDeviceViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_second_scan_layout,parent,false)
        return ScanDeviceViewHolder(view)
    }

    @SuppressLint("MissingPermission")
    override fun onBindViewHolder(holder: SecondScanAdapter.ScanDeviceViewHolder, position: Int) {

        val b = list[position]
        if(b.isBind){
            holder.itemScanName.text = b.bleName
            holder.itemSecondMacTv.text = b.bleMac
            holder.rssiView.setRssiValue(0)
//            holder.itemRecordTv.text = list[position].recordStr
//            holder.itemProductNameTv.text = list[position].productNumber.toString()

        }else{
            holder.itemScanName.text = list[position].bluetoothDevice.name
            holder.itemSecondMacTv.text = list[position].bluetoothDevice.address

            holder.itemRecordTv.text = list[position].recordStr
            holder.itemProductNameTv.text = list[position].productNumber.toString()
            holder.rssiView.setRssiValue(Math.abs(b.rssi))
        }



        holder.itemView.setOnClickListener {
            val position = holder.layoutPosition
            onItemClickListener?.onItemClick(position)
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }
}