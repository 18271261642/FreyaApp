package com.app.freya.second

import android.util.DisplayMetrics
import android.view.Gravity
import android.widget.ImageView
import com.app.freya.R
import com.app.freya.action.AppActivity
import com.app.freya.dialog.AddAlarmDialog

/**
 * 闹钟页面
 */
class AlarmListActivity : AppActivity(){


    override fun getLayoutId(): Int {
       return R.layout.activity_alarm_list_layout
    }

    override fun initView() {

        //添加闹钟
        findViewById<ImageView>(R.id.addAlarmImg).setOnClickListener {
            showEditDialog()
        }
    }

    override fun initData() {

    }


  //显示弹窗
  private fun showEditDialog(){
      val dialog = AddAlarmDialog(this, com.bonlala.base.R.style.BaseDialogTheme)
      dialog.show()

      val window = dialog.window
      val windowLayout = window?.attributes
      val metrics2: DisplayMetrics = resources.displayMetrics
      val widthW: Int = metrics2.widthPixels
      windowLayout?.height = metrics2.heightPixels
      windowLayout?.width = widthW
      windowLayout?.gravity = Gravity.BOTTOM
      windowLayout?.verticalMargin = 30F
      window?.attributes = windowLayout
  }
}