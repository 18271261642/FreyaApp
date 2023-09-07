package com.app.freya.second

import android.content.Intent
import android.net.Uri
import android.widget.TextView
import androidx.activity.viewModels
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.viewModels
import com.app.freya.R
import com.app.freya.action.AppActivity
import com.app.freya.dialog.UpgradeDialogView
import com.app.freya.viewmodel.KeyBoardViewModel
import com.hjq.toast.ToastUtils

class AppVersionActivity : AppActivity() {


    private val viewModel by viewModels<KeyBoardViewModel>()

    private var appVersionDescTv : TextView ?= null
    private var appVersionCurrentTv : TextView ?= null



    override fun getLayoutId(): Int {
      return R.layout.activity_app_version_layout
    }

    override fun initView() {
        appVersionDescTv= findViewById(R.id.appVersionDescTv)
        appVersionCurrentTv = findViewById(R.id.appVersionCurrentTv)

        findViewById<ConstraintLayout>(R.id.appCheckLayout).setOnClickListener {
            val packManager = this.packageManager
            val packInfo = packManager.getPackageInfo(this.packageName,0)
            viewModel.checkAppVersion(packInfo.versionCode)
        }
    }

    override fun initData() {
        val packManager = this.packageManager
        val packInfo = packManager.getPackageInfo(this.packageName,0)

        viewModel.appVersionData.observe(this){
            if(it?.isError == false){
                showAppUpgradeDialog(it.ota)
                appVersionDescTv?.text = packInfo.versionName+"/"+resources.getString(R.string.string_new_app_version)
            }else{
                ToastUtils.show(resources.getString(R.string.string_has_last_version))

                appVersionDescTv?.text = packInfo.versionName+"/"+resources.getString(R.string.string_has_last_version)
            }
        }

        appVersionDescTv?.text = packInfo.versionName
        appVersionCurrentTv?.text = String.format(resources.getString(R.string.string_app_version),packInfo.versionName)


        viewModel.checkAppVersion(packInfo.versionCode)
    }

    private fun showAppUpgradeDialog(url : String){
        val upgradeDialogView = UpgradeDialogView(this, com.bonlala.base.R.style.BaseDialogTheme)
        upgradeDialogView.show()
        upgradeDialogView.setContentTxt(resources.getString(R.string.string_app_new_version))
        upgradeDialogView.setOnDialogClickListener{
            upgradeDialogView.dismiss()
            if(it == 0x01){
                val uri = Uri.parse(url)
                val intent = Intent(Intent.ACTION_VIEW,uri)
                startActivity(intent)
            }
        }

    }
}