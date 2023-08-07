package com.app.freya

import android.webkit.WebView
import com.app.freya.action.AppActivity

class ShowWebViewActivity : AppActivity() {


  private var webView : WebView ?= null


  override fun getLayoutId(): Int {
   return R.layout.activity_show_webview_layout
  }

  override fun initView() {
   webView = findViewById(R.id.webView)

   setWebSetting()
  }

  override fun initData() {
     val url = intent.getStringExtra("url")
   val title = intent.getStringExtra("title")

   setTitle(title)

   if (url != null) {
    webView?.loadUrl(url)
   }
  }

  private fun setWebSetting(){
   val webSettings = webView?.settings
   webSettings?.javaScriptEnabled = true
   webSettings?.setSupportZoom(false)
   webSettings?.displayZoomControls = false
  }
 }