package com.app.freya

import android.view.View
import com.app.freya.action.AppActivity
import com.app.freya.ble.ConnStatus
import com.hjq.shape.view.ShapeTextView

/**
 * 表盘主页
 * Created by Admin
 *Date 2023/1/13
 */
class DialHomeActivity : AppActivity() {

    //默认动画
    private var dialHomeDefaultTv : ShapeTextView ?= null
    //自定义
    private var dialHomeCustomizeTv : ShapeTextView ?= null


    override fun getLayoutId(): Int {
        return R.layout.activity_dial_home_layout
    }

    override fun initView() {
        dialHomeDefaultTv = findViewById(R.id.dialHomeDefaultTv)
        dialHomeCustomizeTv = findViewById(R.id.dialHomeCustomizeTv)


        setOnClickListener(dialHomeDefaultTv,dialHomeCustomizeTv)
    }

    override fun initData() {

    }


    override fun onClick(view: View?) {
        super.onClick(view)
        val id = view?.id
        when(id){
            //默认动画
            R.id.dialHomeDefaultTv->{
                if(BaseApplication.getBaseApplication().connStatus != ConnStatus.CONNECTED){
                    return
                }
                BaseApplication.getBaseApplication().bleOperate.setLocalKeyBoardDial()
            }

            //自定义
            R.id.dialHomeCustomizeTv->{
                startActivity(CustomDialActivity::class.java)
            }
        }
    }

}