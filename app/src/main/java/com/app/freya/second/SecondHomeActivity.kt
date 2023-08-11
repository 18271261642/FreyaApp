package com.app.freya.second

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.widget.LinearLayout
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager.widget.ViewPager
import com.app.freya.BaseApplication
import com.app.freya.R
import com.app.freya.action.ActivityManager
import com.app.freya.action.AppActivity
import com.app.freya.action.AppFragment
import com.app.freya.adapter.OnCommItemClickListener
import com.app.freya.ble.ConnStatus
import com.app.freya.dialog.NoticeDialog
import com.app.freya.utils.BikeUtils
import com.app.freya.utils.MmkvUtils
import com.app.freya.utils.NotificationUtils
import com.app.freya.viewmodel.SecondHomeViewModel
import com.app.freya.widget.HomeMenuView
import com.bonlala.base.FragmentPagerAdapter
import com.hjq.shape.layout.ShapeLinearLayout
import com.hjq.toast.ToastUtils
import timber.log.Timber

/**
 * 键盘二代主页，三个底部菜单
 */
class SecondHomeActivity : AppActivity(){


    private var onStateListener : OnCommItemClickListener ?= null


    fun setOnStateListener(li : OnCommItemClickListener){
        this.onStateListener = li
    }



    private var viewModel : SecondHomeViewModel ?= null

    private var scanHolderLayout : LinearLayout ?= null
    private var dataAddLayout : ShapeLinearLayout?= null

    private val INTENT_KEY_IN_FRAGMENT_INDEX = "fragmentIndex"
    private val INTENT_KEY_IN_FRAGMENT_CLASS = "fragmentClass"

    private var mViewPager: ViewPager? = null

    private var mPagerAdapter: FragmentPagerAdapter<AppFragment<*>>? = null

    private var secondHomeMenuView : HomeMenuView ?= null

    override fun getLayoutId(): Int {
        return R.layout.activity_second_home_layout
    }

    override fun initView() {
        dataAddLayout = findViewById(R.id.dataAddLayout)
        mViewPager = findViewById(R.id.vp_home_pager)
        secondHomeMenuView = findViewById(R.id.secondHomeMenuView)
        scanHolderLayout = findViewById(R.id.scanHolderLayout)

        secondHomeMenuView?.setOnItemClick(object :OnCommItemClickListener{
            override fun onItemClick(position: Int) {
                switchFragment(position)
            }

        })

        dataAddLayout?.setOnClickListener { startActivity(SecondScanActivity::class.java) }

    }

    override fun initData() {
        mPagerAdapter = FragmentPagerAdapter(this)
        mPagerAdapter?.addFragment(MenuDataFragment.getInstance())
        mPagerAdapter?.addFragment(MenuSettingFragment.getInstance())
        mPagerAdapter?.addFragment(MenuDeviceFragment.getInstance())
        mViewPager?.adapter = mPagerAdapter
        viewModel = ViewModelProvider(this).get(SecondHomeViewModel::class.java)
        viewModel?.getAllSupportDeviceType(this)
    }


    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        switchFragment(mPagerAdapter!!.getFragmentIndex(getSerializable(INTENT_KEY_IN_FRAGMENT_CLASS)))
    }


    override fun onResume() {
        super.onResume()
        showIsAddDevice()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        // 保存当前 Fragment 索引位置
        outState.putInt(INTENT_KEY_IN_FRAGMENT_INDEX, mViewPager!!.currentItem)
    }


    private fun switchFragment(fragmentIndex: Int) {
        if (fragmentIndex == -1) {
            return
        }

        Timber.e("-------swww=" + fragmentIndex)

        when (fragmentIndex) {
            0, 1, 2 -> {
                mViewPager!!.currentItem = fragmentIndex

            }
            else -> {}
        }
    }

    //是否显示添加设备
     fun showIsAddDevice(){
        //是否有连接过
        val isMac = MmkvUtils.getConnDeviceMac()
        Timber.e("-----isMac="+isMac)
        scanHolderLayout?.visibility = if(BikeUtils.isEmpty(isMac)) View.VISIBLE else View.GONE

        onStateListener?.onItemClick(0x00)
    }


    private var mExitTime: Long = 0

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        // 过滤按键动作
        if (event.keyCode == KeyEvent.KEYCODE_BACK) {
            if (System.currentTimeMillis() - mExitTime > 2000) {
                mExitTime = System.currentTimeMillis()
                ToastUtils.show(resources.getString(R.string.string_double_click_exit))
                return true
            } else {
                BaseApplication.getBaseApplication().bleOperate.disConnYakDevice()
                BaseApplication.getBaseApplication().connStatus = ConnStatus.NOT_CONNECTED
                ActivityManager.getInstance().finishAllActivities()
                finish()
            }
        }
        return super.onKeyDown(keyCode, event)
    }


    //显示打开通知权限弹窗
    private fun showOpenNotifyDialog() {
        //判断通知权限是否打开了
        val isOpen2 = hasNotificationListenPermission(this)
        val isOpen = NotificationUtils.isNotificationEnabled(this)
        Timber.e("--------通知是否打开了=" + isOpen + " " + isOpen2)
        if (!isOpen2) {
            openNoti()
        }
    }


    private fun openNoti() {
        val dialog = NoticeDialog(this, com.bonlala.base.R.style.BaseDialogTheme)
        dialog.show()
        dialog.setCancelable(false)
        dialog.setOnDialogClickListener { position ->
            if (position == 0x00) {
                dialog.dismiss()
                startToNotificationListenSetting(this@SecondHomeActivity)
            }
            if (position == 0x01) {
                dialog.dismiss()
            }
        }
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

    /**
     * 有通知/监听读取权限
     * 迁移到SNNotificationListener依赖库
     *
     * @param context
     * @return
     */
    private fun hasNotificationListenPermission(context: Context): Boolean {
        val packageNames = NotificationManagerCompat.getEnabledListenerPackages(context)
        return !packageNames.isEmpty() && packageNames.contains(context.packageName)
    }
}