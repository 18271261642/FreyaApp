package com.app.freya.second

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.freya.R
import com.app.freya.action.AppActivity
import com.app.freya.adapter.AutoLockAdapter
import com.app.freya.adapter.OnCommItemClickListener
import com.app.freya.bean.AutoLockBean
import com.app.freya.utils.MmkvUtils


class SecondAutoLockActivity : AppActivity(){



    private var autoLockRecyclerView : RecyclerView ?= null

    private var adapter : AutoLockAdapter ?= null

    private var list : MutableList<AutoLockBean> ?= null


    override fun getLayoutId(): Int {
        return R.layout.activity_secod_auto_lock_layout
    }

    override fun initView() {
        list= ArrayList<AutoLockBean>()
        autoLockRecyclerView = findViewById(R.id.autoLockRecyclerView)
        val linearLayoutManager = LinearLayoutManager(this)
        linearLayoutManager.orientation = LinearLayoutManager.VERTICAL
        autoLockRecyclerView?.layoutManager = linearLayoutManager
        adapter = AutoLockAdapter(this, list as ArrayList<AutoLockBean>)
        autoLockRecyclerView?.adapter = adapter

        adapter?.setOnCommClickListener(object : OnCommItemClickListener{
            override fun onItemClick(position: Int) {
                list?.forEach {
                    it.isChecked = false
                }
                list?.get(position)?.isChecked = true
                adapter?.notifyDataSetChanged()
                list?.get(position)?.autoTime?.let { MmkvUtils.saveAutoLock(it) }
            }

        })

    }

    override fun initData() {
       val tempList = ArrayList<AutoLockBean>()
        val time = MmkvUtils.getAutoLock()
        tempList.add(AutoLockBean(15,time==15))
        tempList.add(AutoLockBean(30,time==30))
        tempList.add(AutoLockBean(60,time==60))
        tempList.add(AutoLockBean(120,time==120))
        tempList.add(AutoLockBean(300,time==300))
        tempList.add(AutoLockBean(600,time==600))
        list?.clear()
        list?.addAll(tempList)
        adapter?.notifyDataSetChanged()

    }
}