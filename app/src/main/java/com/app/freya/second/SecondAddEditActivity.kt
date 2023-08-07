package com.app.freya.second

import android.text.SpannableString
import android.text.Spanned
import android.text.SpannedString
import android.text.TextUtils
import android.text.style.AbsoluteSizeSpan
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import com.app.freya.R
import com.app.freya.action.AppActivity
import com.app.freya.bean.DbManager
import com.app.freya.bean.NoteBookBean
import com.app.freya.utils.BikeUtils
import com.hjq.toast.ToastUtils

/**
 * 二代键盘编辑添加
 */
class SecondAddEditActivity :  AppActivity() {


    private var secondEditTitleEdit : EditText ?= null
    private var secondEditContentEdit : EditText ?= null
    private var secondEditSubmitTv : TextView ?= null
    private var secondAddBackTv : TextView ?= null
    private var secondAddBackImg : ImageView ?= null


    //时间
    private var timeStr : String?= null

    override fun getLayoutId(): Int {
        return R.layout.activity_second_add_edit_layout
    }

    override fun initView() {
        secondAddBackImg = findViewById(R.id.secondAddBackImg)
        secondEditTitleEdit = findViewById(R.id.secondEditTitleEdit)
        secondEditContentEdit = findViewById(R.id.secondEditContentEdit)
        secondEditSubmitTv = findViewById(R.id.secondEditSubmitTv)
        secondAddBackTv = findViewById(R.id.secondAddBackTv)


        //设置hit的颜色
        // 新建一个可以添加属性的文本对象
        val ss = SpannableString(resources.getString(R.string.string_title))
        // 新建一个属性对象,设置文字的大小
        val ass = AbsoluteSizeSpan(23, true)
        // 附加属性到文本
        ss.setSpan(ass, 0, ss.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        secondEditTitleEdit?.setHintTextColor(resources.getColor(R.color.white))
        secondEditTitleEdit?.hint = SpannedString(ss)


        // 新建一个可以添加属性的文本对象
        val ss2 = SpannableString(resources.getString(R.string.string_start_write))
        // 新建一个属性对象,设置文字的大小
        val ass2 = AbsoluteSizeSpan(20, true)
        // 附加属性到文本
        ss2.setSpan(ass2, 0, ss2.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        secondEditContentEdit?.setHintTextColor(resources.getColor(R.color.white))
        secondEditContentEdit?.hint = SpannedString(ss2)


        //保存
        secondEditSubmitTv?.setOnClickListener {
            saveOrUpdateData()

        }

        secondAddBackTv?.setOnClickListener {
            finish()
        }
        secondAddBackImg?.setOnClickListener {
            finish()
        }
    }

    override fun initData() {
        //时间戳
        timeStr = intent.getStringExtra("timeKey")
        timeStr?.let { queryNoteBookData(it) }
    }



    //查询对应的数据，根据时间戳查询 yyyy-MM-dd HH:mm:ss格式
    private fun queryNoteBookData(timeStr : String){
        val dataBean = DbManager.getInstance().queryNoteBookByTime(timeStr)
        if(dataBean != null){
            secondEditTitleEdit?.setText(dataBean.noteTitle.toString())
          //  editNoteBookTimeTv?.text = BikeUtils.formatKeyboardTime(dataBean.noteTimeLong,this)
            secondEditContentEdit?.setText(dataBean.noteContent)
        }

    }


    //保存或修改数据
    private fun saveOrUpdateData(){
        //标题
        val inputTitle = secondEditTitleEdit?.text.toString()
        //内容
        val inputContent = secondEditContentEdit?.text.toString()

        if(TextUtils.isEmpty(inputTitle)){
            ToastUtils.show(resources.getString(R.string.string_please_input_title))
            return
        }

//        if(TextUtils.isEmpty(inputContent)){
//            ToastUtils.show(resources.getString(R.string.string_please_input_title))
//            return
//        }

        val noteBookBean = NoteBookBean()
        noteBookBean.noteTitle = inputTitle
        noteBookBean.noteContent = inputContent.trim()
        noteBookBean.noteTimeLong = System.currentTimeMillis()
        noteBookBean.saveTime = BikeUtils.getCurrDate()
        noteBookBean.saveTime = if(timeStr != null ) timeStr else BikeUtils.getFormatDate(System.currentTimeMillis(),"yyyy-MM-dd HH:mm:ss")

        val  isSave = DbManager.getInstance().saveOrUpdateData(noteBookBean)
        if(isSave){
            //ToastUtils.show("保存成功!")
            finish()
        }
    }

}