package com.app.freya.second

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Intent
import android.view.View
import android.widget.TextView
import com.app.freya.R
import com.app.freya.action.AppActivity
import com.app.freya.ble.NotifyLogActivity
import com.app.freya.utils.MmkvUtils
import com.bonlala.widget.view.SwitchButton
import com.hjq.bar.OnTitleBarListener

import kotlinx.android.synthetic.main.activity_msg_notify_layout.notifyTitleBar
import timber.log.Timber

/**
 * 消息提醒
 */
class NotifyOpenActivity : AppActivity() {


    private var notifyDiscordSwitch : SwitchButton ?= null
    private var notifyQqSwitch : SwitchButton ?= null
    private var notifyWxSwitch : SwitchButton ?= null





    override fun getLayoutId(): Int {
        return R.layout.activity_msg_notify_layout
    }

    override fun initView() {
        notifyDiscordSwitch = findViewById(R.id.notifyDiscordSwitch)
        notifyQqSwitch = findViewById(R.id.notifyQqSwitch)
        notifyWxSwitch = findViewById(R.id.notifyWxSwitch)


        findViewById<TextView>(R.id.notifyTempTv).setOnClickListener {
            startActivity(NotifyLogActivity::class.java)
        }



        notifyWxSwitch?.setOnCheckedChangeListener { button, checked ->
            MmkvUtils.setSaveObjParams(MmkvUtils.WX_KEY,checked)
        }

        notifyQqSwitch?.setOnCheckedChangeListener{button, checked ->
            MmkvUtils.setSaveObjParams(MmkvUtils.QQ_KEY,checked)

        }

        notifyDiscordSwitch?.setOnCheckedChangeListener { button, checked ->
            MmkvUtils.setSaveObjParams(MmkvUtils.DISCORD_KEY,checked)
        }

        notifyTitleBar?.setOnTitleBarListener(object : OnTitleBarListener {
            override fun onLeftClick(view: View?) {
                finish()
            }

            override fun onTitleClick(view: View?) {

            }

            override fun onRightClick(view: View?) {
                Timber.e("----rightClick")
                startToNotificationListenSetting(this@NotifyOpenActivity)
            }

        })
    }

    override fun initData() {
        val wxSwitch = MmkvUtils.getSaveParams(MmkvUtils.WX_KEY,false)
        val qqSwitch = MmkvUtils.getSaveParams(MmkvUtils.QQ_KEY,false)
        val discordSwitch = MmkvUtils.getSaveParams(MmkvUtils.DISCORD_KEY,false)
        notifyWxSwitch?.isChecked = wxSwitch as Boolean
        notifyQqSwitch?.isChecked = qqSwitch as Boolean
        notifyDiscordSwitch?.isChecked = discordSwitch as Boolean

    }


    /**
     * 跳转到通知内容读取权限设置
     *
     * @param context
     */
    private fun startToNotificationListenSetting(context: Activity) {
        try {
            val intent = Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS")
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivityForResult(intent, 0x08)
            //context.startActivity(intent);
        } catch (e: ActivityNotFoundException) {
            try {
                val intent = Intent()
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                val cn = ComponentName(
                    "com.android.settings",
                    "com.android.settings.Settings\$NotificationAccessSettingsActivity"
                )
                intent.component = cn
                intent.putExtra(":settings:show_fragment", "NotificationAccessSettings")
                context.startActivity(intent)
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }
    }
}