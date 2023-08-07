package com.app.freya

import android.text.*
import android.text.style.AbsoluteSizeSpan
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.AppCompatEditText
import com.app.freya.action.AppActivity
import com.app.freya.bean.DbManager
import com.app.freya.bean.NoteBookBean
import com.app.freya.utils.BikeUtils
import com.hjq.toast.ToastUtils


/**
 * 编辑笔记页面
 * Created by Admin
 *Date 2023/1/10
 */
class EditNoteBookActivity : AppActivity() {

    //标题
    private var editNoteBookTitleTv : TextView ?= null
    //输入的标题
    private var editNoteBookTitleEdit : EditText ?= null
    //字数
    private var editNoteBookNumberTv : TextView ?= null
    //输入的内容
    private var editNoteBookEditText : AppCompatEditText ?= null
    //返回
    private var editNoteBookBackImgView : ImageView ?= null
    //保存
    private var editNoteBookSaveImgView : ImageView ?= null
    //时间
    private var editNoteBookTimeTv : TextView ?= null

    //时间
    private var timeStr : String?= null

    override fun getLayoutId(): Int {
        return R.layout.activity_edit_notebook_layout
    }

    override fun initView() {
        editNoteBookTitleTv = findViewById(R.id.editNoteBookTitleTv)
        editNoteBookNumberTv = findViewById(R.id.editNoteBookNumberTv)
        editNoteBookEditText = findViewById(R.id.editNoteBookEditText)
        editNoteBookBackImgView = findViewById(R.id.editNoteBookBackImgView)
        editNoteBookSaveImgView = findViewById(R.id.editNoteBookSaveImgView)
        editNoteBookTitleEdit = findViewById(R.id.editNoteBookTitleEdit)
        editNoteBookTimeTv = findViewById(R.id.editNoteBookTimeTv)

        setOnClickListener(editNoteBookBackImgView,editNoteBookSaveImgView)

        //设置hit的颜色
        // 新建一个可以添加属性的文本对象
        val ss = SpannableString(resources.getString(R.string.string_title))
        // 新建一个属性对象,设置文字的大小
        val ass = AbsoluteSizeSpan(23, true)
        // 附加属性到文本
        ss.setSpan(ass, 0, ss.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        editNoteBookTitleEdit?.setHintTextColor(resources.getColor(R.color.color_note_title_color))
        editNoteBookTitleEdit?.hint = SpannedString(ss)


        // 新建一个可以添加属性的文本对象
        val ss2 = SpannableString(resources.getString(R.string.string_start_write))
        // 新建一个属性对象,设置文字的大小
        val ass2 = AbsoluteSizeSpan(20, true)
        // 附加属性到文本
        ss2.setSpan(ass2, 0, ss.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        editNoteBookEditText?.setHintTextColor(resources.getColor(R.color.color_note_title_color))
        editNoteBookEditText?.hint = SpannedString(ss2)


    }

    override fun initData() {
        editNoteBookTitleTv?.text = resources.getString(R.string.string_add)
        editNoteBookEditText?.addTextChangedListener(textWatcher)

        editNoteBookTimeTv?.text = BikeUtils.formatKeyboardTime(System.currentTimeMillis(),this)

        //时间戳
        timeStr = intent.getStringExtra("timeKey")
        timeStr?.let { queryNoteBookData(it) }

    }



    //查询对应的数据，根据时间戳查询 yyyy-MM-dd HH:mm:ss格式
    private fun queryNoteBookData(timeStr : String){
        val dataBean = DbManager.getInstance().queryNoteBookByTime(timeStr)
        if(dataBean != null){
            editNoteBookTitleTv?.text = resources.getString(R.string.string_edit)
            editNoteBookTitleTv?.text =resources.getString(R.string.string_edit)
            editNoteBookTitleEdit?.setText(dataBean.noteTitle.toString())
            editNoteBookTimeTv?.text = BikeUtils.formatKeyboardTime(dataBean.noteTimeLong,this)
            editNoteBookEditText?.setText(dataBean.noteContent)
        }

    }


    private val textWatcher : TextWatcher = object : TextWatcher{


        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

        }

        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

        }

        override fun afterTextChanged(p0: Editable?) {
            val str = p0.toString()
            if(BikeUtils.isEmpty(str)){
                editNoteBookNumberTv?.text = "0 "+resources.getString(R.string.string_word)
                return
            }
            val length = str.length
            editNoteBookNumberTv?.text = length.toString()+" "+resources.getString(R.string.string_word)
        }

    }


    override fun onClick(view: View?) {
        super.onClick(view)
        val id = view?.id

        when(id){
            //返回
            R.id.editNoteBookBackImgView->{
                finish()
            }
            //保存
            R.id.editNoteBookSaveImgView->{
                saveOrUpdateData()
            }
        }
    }



    //保存或修改数据
    private fun saveOrUpdateData(){
        //标题
        val inputTitle = editNoteBookTitleEdit?.text.toString()
        //内容
        val inputContent = editNoteBookEditText?.text.toString()

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