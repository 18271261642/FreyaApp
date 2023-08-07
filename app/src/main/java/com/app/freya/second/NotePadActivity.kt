package com.app.freya.second

import android.graphics.Color
import android.util.DisplayMetrics
import android.view.Gravity
import android.widget.ImageView
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.freya.BaseApplication
import com.app.freya.EditNoteBookActivity
import com.app.freya.R
import com.app.freya.action.AppActivity
import com.app.freya.adapter.OnClickLongListener
import com.app.freya.adapter.OnCommItemClickListener
import com.app.freya.adapter.SecondNotePadAdapter
import com.app.freya.bean.DbManager
import com.app.freya.bean.NoteBookBean
import com.app.freya.ble.ConnStatus
import com.app.freya.dialog.DeleteDeviceDialog
import com.app.freya.viewmodel.NoteBookViewModel
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Calendar


/**
 * Created by Admin
 *Date 2023/7/4
 */
class NotePadActivity : AppActivity() {

    private val viewModel by viewModels<NoteBookViewModel>()

    private var secondNoteRecyclerView : RecyclerView ?= null
    private var adapter : SecondNotePadAdapter ?= null
    private var list : MutableList<NoteBookBean> ?= null



    override fun getLayoutId(): Int {
       return R.layout.activity_note_pad_layout
    }

    override fun initView() {
        secondNoteRecyclerView = findViewById(R.id.secondNoteRecyclerView)
        val linearLayoutManager = LinearLayoutManager(this)
        linearLayoutManager.orientation = LinearLayoutManager.VERTICAL
        secondNoteRecyclerView?.layoutManager = linearLayoutManager
        list = ArrayList<NoteBookBean>()

        adapter = SecondNotePadAdapter(this, list as ArrayList<NoteBookBean>)
        secondNoteRecyclerView?.adapter = adapter


        //点击
        adapter?.setOnCommClickListener(object : OnCommItemClickListener{
            override fun onItemClick(position: Int) {
                list?.get(position)?.let { showSyncDialog(it) }
            }

        })
        //长按
        adapter?.setOnLongClickListener(object : OnClickLongListener{
            override fun onLongClick(position: Int) {
                list?.get(position)?.let { showDeleteOrUpdate(it) }
            }

        })




        findViewById<ImageView>(R.id.secondAddNoteImg).setOnClickListener {
            startActivity(SecondAddEditActivity::class.java)
        }


    }

    override fun initData() {
        viewModel.allNoteBookData.observe(this){

            list?.clear()
            if(it != null && it.isNotEmpty()){
                list?.addAll(it)
                list?.sortByDescending { it.noteTimeLong }
            }

            adapter?.notifyDataSetChanged()
        }
    }

    override fun onResume() {
        super.onResume()
        getAllDbData()
    }

    //查询所有的数据
    private fun getAllDbData(){
        viewModel.getAllDbData()
    }


    private fun showSyncDialog(noteB : NoteBookBean){
        val dialog = DeleteDeviceDialog(this, com.bonlala.base.R.style.BaseDialogTheme)
        dialog.show()
        dialog.setTitleTxt("是否同步到设备?")
        dialog.setConfirmBgColor(Color.parseColor("#F86849"))
        dialog.setOnCommClickListener(object : OnCommItemClickListener{
            override fun onItemClick(position: Int) {
                dialog.dismiss()
                if(position == 0x01){
                    sendNotToDevice(noteB.noteTitle,noteB.noteTimeLong,noteB.noteContent)
                }
            }

        })
        val window = dialog.window
        val windowLayout = window?.attributes
        val metrics2: DisplayMetrics = resources.displayMetrics
        val widthW: Int = metrics2.widthPixels

        windowLayout?.width = widthW
        windowLayout?.gravity = Gravity.BOTTOM
        window?.attributes = windowLayout
    }


    private fun showDeleteOrUpdate(noteB : NoteBookBean){
        val dialog = DeleteDeviceDialog(this, com.bonlala.base.R.style.BaseDialogTheme)
        dialog.show()
        dialog.setTitleTxt("请选择删除或编辑?")
        dialog.setConfirmAndCancelTxt(resources.getString(R.string.string_edit), resources.getString(R.string.string_delete_txt))
        dialog.setCancelBgColor(Color.parseColor("#F86849"))
        dialog.setConfirmBgColor(Color.parseColor("#16AEA0"))
        dialog.setOnCommClickListener { position ->
            dialog.dismiss()
            if (position == 0x01) {   //编辑
                startActivity(
                    SecondAddEditActivity::class.java,
                    arrayOf("timeKey"),
                    arrayOf(noteB?.saveTime)
                )
            }
            if (position == 0) {  //删除

                val timeLong = noteB.noteTimeLong
                val deviceTimeLong = timeLong / 1000 - 946656000L
                if (deviceTimeLong != null) {
                    BaseApplication.getBaseApplication().bleOperate.deleteIndexNote(deviceTimeLong)
                }

                DbManager.getInstance().deleteNotebook(noteB.saveTime)
                GlobalScope.launch {
                    delay(500)
                    getAllDbData()
                }
            }
        }
        val window = dialog.window
        val windowLayout = window?.attributes
        val metrics2: DisplayMetrics = resources.displayMetrics
        val widthW: Int = metrics2.widthPixels

        windowLayout?.width = widthW
        windowLayout?.gravity = Gravity.BOTTOM
        window?.attributes = windowLayout
    }



    //发送数据
    private fun sendNotToDevice(title : String,timeLong : Long,contentStr : String){
        if(BaseApplication.getBaseApplication().connStatus != ConnStatus.CONNECTED){
            return
        }

        //时间戳
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timeLong
        BaseApplication.getBaseApplication().bleOperate.sendKeyBoardNoteBook(title,contentStr,calendar)
    }
}