package com.app.freya.adapter

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.view.animation.LinearInterpolator
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.app.freya.R
import com.app.freya.bean.BleBean
import com.app.freya.ble.ConnStatus
import com.app.freya.utils.BikeUtils
import com.app.freya.utils.MmkvUtils
import com.app.freya.widget.RssiStateView
import timber.log.Timber

/**
 * Created by Admin
 *Date 2023/7/12
 */
class SecondScanAdapter(private val context: Context,private val list : MutableList<BleBean>) : RecyclerView.Adapter<SecondScanAdapter.ScanDeviceViewHolder>() {

    private var onItemClickListener : OnCommItemClickListener ?= null

    private var onCommMenuClickListener : OnCommMenuClickListener ?= null


    fun setOnCommMenuClick(c : OnCommMenuClickListener){
        this.onCommMenuClickListener = c
    }

    fun setOnItemClick(click : OnCommItemClickListener){
        this.onItemClickListener = click
    }


    class ScanDeviceViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView){
        var itemScanName  = itemView.findViewById<TextView>(R.id.itemSecondNameTv)
        val itemSecondMacTv = itemView.findViewById<TextView>(R.id.itemSecondMacTv)
        val itemRecordTv = itemView.findViewById<TextView>(R.id.itemRecordTv)
        val itemProductNameTv = itemView.findViewById<TextView>(R.id.itemProductNameTv)
        val rssiView = itemView.findViewById<RssiStateView>(R.id.itemRssiTv)
        val itemConningImageView = itemView.findViewById<ImageView>(R.id.itemConningImageView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScanDeviceViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_second_scan_layout,parent,false)
        return ScanDeviceViewHolder(view)
    }

    @SuppressLint("MissingPermission")
    override fun onBindViewHolder(holder: SecondScanAdapter.ScanDeviceViewHolder, position: Int) {

        val b = list[position]
        Timber.e("-----rssi="+Math.abs(b.rssi))
        if(b.isBind){
            val saveMac = MmkvUtils.getConnDeviceMac()
            holder.itemScanName.text = b.bleName
            holder.itemSecondMacTv.text = b.bleMac
            holder.rssiView.setRssiValue(0)
            if(!BikeUtils.isEmpty(saveMac) && saveMac == b.bleMac){
                holder.itemScanName.setTextColor(context.resources.getColor(com.bonlala.base.R.color.red))
            }else{
                holder.itemScanName.setTextColor(context.resources.getColor(com.bonlala.base.R.color.white))
            }
//            holder.itemRecordTv.text = list[position].recordStr
//            holder.itemProductNameTv.text = list[position].productNumber.toString()

            if(b.connStatus == ConnStatus.CONNECTING){
                holder.itemConningImageView.visibility = View.VISIBLE
                holder.itemSecondMacTv.visibility = View.GONE
                routeImg(holder.itemConningImageView)
            }else{
                holder.itemConningImageView.visibility = View.GONE
                holder.itemSecondMacTv.visibility = View.VISIBLE
                holder.itemConningImageView.clearAnimation()
            }

        }else{
            holder.itemScanName.text = list[position].bluetoothDevice.name
            holder.itemSecondMacTv.text = list[position].bluetoothDevice.address

            holder.itemRecordTv.text = list[position].recordStr
            holder.itemProductNameTv.text = list[position].productNumber.toString()
            holder.rssiView.setRssiValue(Math.abs(b.rssi))
        }



        holder.rssiView.setOnClickListener {
            val position = holder.layoutPosition
            onCommMenuClickListener?.onChildItemClick(position)
        }

        holder.itemView.setOnClickListener {
            val position = holder.layoutPosition
            onCommMenuClickListener?.onItemClick(position)
        }


    }

    override fun getItemCount(): Int {
        return list.size
    }


    //旋转
    private fun routeImg(imageView: ImageView){
        val animation = ObjectAnimator.ofFloat(0F,360F)
        animation.duration = 1000
        animation.repeatCount = -1
        animation.interpolator = LinearInterpolator()
        animation.start()
    }
}