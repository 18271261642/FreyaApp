package com.app.freya.second

import android.view.View
import com.app.freya.R
import com.app.freya.action.TitleBarFragment
import com.app.freya.utils.BikeUtils
import com.app.freya.utils.MmkvUtils
import com.app.freya.widget.CheckButtonView


/**
 * 设备设置页面
 */
class MenuSettingFragment : TitleBarFragment<SecondHomeActivity>() {

    private var settingNoteLayout : CheckButtonView ?= null
    private var settingAlarmLayout : CheckButtonView ?= null
    //上传动画
    private var secondUploadGifView : CheckButtonView ?= null

    companion object{

        fun getInstance():MenuSettingFragment{
            return MenuSettingFragment()
        }
    }

    override fun getLayoutId(): Int {
       return R.layout.fragment_menu_setting_layout
    }

    override fun initView() {
        settingNoteLayout = findViewById(R.id.settingNoteLayout)
        settingAlarmLayout = findViewById(R.id.settingAlarmLayout)
        secondUploadGifView = findViewById(R.id.secondUploadGifView)


        settingNoteLayout?.setOnClickListener(this)
        settingAlarmLayout?.setOnClickListener(this)
        secondUploadGifView?.setOnClickListener(this)
    }

    override fun initData() {

    }


    override fun onClick(view: View?) {
        super.onClick(view)
        val id = view?.id

        when(id){
            R.id.settingNoteLayout->{
                if(BikeUtils.isEmpty(getMac())){
                    return
                }
                startActivity(NotePadActivity::class.java)
            }
            R.id.settingAlarmLayout->{
                if(BikeUtils.isEmpty(getMac())){
                    return
                }
                startActivity(AlarmListActivity::class.java)
            }
            R.id.secondUploadGifView->{ //上传动画
                if(BikeUtils.isEmpty(getMac())){
                   return
                }
                startActivity(SecondGifHomeActivity::class.java)
            }
        }
    }
    private fun getMac() : String{
        return MmkvUtils.getConnDeviceMac()
    }

}