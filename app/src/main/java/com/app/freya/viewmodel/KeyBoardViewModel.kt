package com.app.freya.viewmodel


import android.content.Context
import android.os.Build
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.app.freya.BaseApplication
import com.app.freya.bean.AppVersionBean
import com.app.freya.bean.OtaBean
import com.app.freya.utils.BikeUtils
import com.app.freya.utils.GsonUtils
import com.app.freya.utils.MmkvUtils
import com.google.gson.Gson
import com.hjq.http.EasyConfig
import com.hjq.http.EasyHttp
import com.hjq.http.listener.OnHttpListener
import com.tencent.bugly.crashreport.CrashReport
import okhttp3.Call
import org.json.JSONObject
import timber.log.Timber
import java.util.concurrent.TimeUnit


class KeyBoardViewModel : ViewModel() {


    //升级的内容
    var firmwareData = SingleLiveEvent<OtaBean?>()

    //app升级
    var appVersionData = SingleLiveEvent<AppVersionBean?>()

    //log
    var logData = SingleLiveEvent<String>()

    //检查版本
    fun checkVersion(lifecycleOwner: LifecycleOwner, versionCode: Int) {
        val productNumber = MmkvUtils.getSaveProductNumber()
        val url = BaseApplication.BASE_URL + "checkUpdate?firmwareVersionCode=$versionCode&productNumber="+productNumber
        Timber.e("----url="+url)
        val stringRequest =
            StringRequest(url,
                { response ->
                    Timber.e("----response=" + response)
                    val jsonObject = JSONObject(response)
                    if (jsonObject.getInt("code") == 200) {
                        val data = jsonObject.getJSONObject("data")
                        val firmware = data.getString("firmware")

                        if (!BikeUtils.isEmpty(firmware)) {
                            var bean = GsonUtils.getGsonObject<OtaBean>(firmware)
                            if (bean == null) {
                                bean = OtaBean()
                                bean.isError = true
                                bean.setErrorMsg("back null")
                            } else {
                                bean.isError = false
                            }
                            Timber.e("------bean=" + bean.toString())
                            firmwareData.postValue(bean)
                        } else {
                            val bean = OtaBean()
                            bean.isError = true
                            bean.setErrorMsg("back null")
                            firmwareData.postValue(bean)
                        }
                    }
                }
            ) { error ->
                Timber.e("----ErrorListener=" + error?.message)
                val bean = OtaBean()
                bean.isError = true
                bean.setErrorMsg(error?.message + "\n" + error?.printStackTrace())
                firmwareData.postValue(bean)

                val errorStr = "型号:"+Build.MODEL+" android版本："+Build.VERSION.SDK_INT+"\n"+"error="+error?.message
                CrashReport.postCatchedException(CusException(errorStr))

            }
        stringRequest.retryPolicy = DefaultRetryPolicy(
            TimeUnit.SECONDS.toMillis(20)
                .toInt(),  //After the set time elapses the request will timeout
            0,
            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        )
        BaseApplication.getBaseApplication().requestQueue.add(stringRequest)


    }



    //检查版本
    fun checkVersion(lifecycleOwner: LifecycleOwner, versionCode: Int,productNumber : String) {

        val url = BaseApplication.BASE_URL + "checkUpdate?firmwareVersionCode=$versionCode&productNumber="+productNumber
        Timber.e("----url="+url)
        val stringRequest =
            StringRequest(url,
                { response ->
                    Timber.e("----response=" + response)
                    val jsonObject = JSONObject(response)
                    if (jsonObject.getInt("code") == 200) {
                        val data = jsonObject.getJSONObject("data")
                        val firmware = data.getString("firmware")

                        if (!BikeUtils.isEmpty(firmware)) {
                            var bean = GsonUtils.getGsonObject<OtaBean>(firmware)
                            if (bean == null) {
                                bean = OtaBean()
                                bean.isError = true
                                bean.setErrorMsg("back null")
                            } else {
                                bean.isError = false
                            }
                            Timber.e("------bean=" + bean.toString())
                            firmwareData.postValue(bean)
                        } else {
                            val bean = OtaBean()
                            bean.isError = true
                            bean.setErrorMsg("back null")
                            firmwareData.postValue(bean)
                        }
                    }
                }
            ) { error ->
                Timber.e("----ErrorListener=" + error?.message)
                val bean = OtaBean()
                bean.isError = true
                bean.setErrorMsg(error?.message + "\n" + error?.printStackTrace())
                firmwareData.postValue(bean)

                val errorStr = "型号:"+Build.MODEL+" android版本："+Build.VERSION.SDK_INT+"\n"+"error="+error?.message
                CrashReport.postCatchedException(CusException(errorStr))

            }
        stringRequest.retryPolicy = DefaultRetryPolicy(
            TimeUnit.SECONDS.toMillis(20)
                .toInt(),  //After the set time elapses the request will timeout
            0,
            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        )
        BaseApplication.getBaseApplication().requestQueue.add(stringRequest)


    }





    //检查App版本
    fun checkAppVersion(versionCode: Int) {

        val url = BaseApplication.BASE_URL + "checkUpdate?appVersion=$versionCode&appType=1"
        Timber.e("----url="+url)
        val stringRequest =
            StringRequest(url,
                { response ->
                    Timber.e("----response=" + response)
                    val jsonObject = JSONObject(response)
                    if (jsonObject.getInt("code") == 200) {
                        val data = jsonObject.getJSONObject("data")
                        val appVo = data.getString("appVo")

                        if (!BikeUtils.isEmpty(appVo)) {
                            var bean = GsonUtils.getGsonObject<AppVersionBean>(appVo)
                            if (bean == null) {
                                bean = AppVersionBean()
                                bean.isError = true
                                bean.setErrorMsg("back null")
                            } else {
                                bean.isError = false
                            }
                            Timber.e("------bean=" + bean.toString())
                            appVersionData.postValue(bean)
                        } else {
                            val bean = AppVersionBean()
                            bean.isError = true
                            bean.setErrorMsg("back null")
                            appVersionData.postValue(bean)
                        }
                    }
                }
            ) { error ->
                Timber.e("----ErrorListener=" + error?.message)
                val bean = AppVersionBean()
                bean.isError = true
                bean.setErrorMsg(error?.message + "\n" + error?.printStackTrace())
                appVersionData.postValue(bean)

                val errorStr = "型号:"+Build.MODEL+" android版本："+Build.VERSION.SDK_INT+"\n"+"error="+error?.message
                CrashReport.postCatchedException(CusException(errorStr))

            }
        stringRequest.retryPolicy = DefaultRetryPolicy(
            TimeUnit.SECONDS.toMillis(20)
                .toInt(),  //After the set time elapses the request will timeout
            0,
            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        )
        BaseApplication.getBaseApplication().requestQueue.add(stringRequest)


    }





    val stringBuffer = StringBuffer()

    fun checkRequest(lifecycleOwner: LifecycleOwner) {
        stringBuffer.delete(0, stringBuffer.length)

        val vUrl = "http://wuquedistribution.com:12348/recordDebugInfo?productNumber=c003&mac=%E6%B5%8B%E8%AF%95&osType=1&content=%E5%86%85%E5%AE%B9&remark=%E5%A4%87%E6%B3%A8&phoneModel=18888888888"
        val httpsUrl = "https://wuquedistribution.com:12349/recordDebugInfo?productNumber=c003&mac=%E6%B5%8B%E8%AF%95&osType=1&content=%E5%86%85%E5%AE%B9&remark=%E5%A4%87%E6%B3%A8&phoneModel=18888888888"
        //volley
        val stringRequest =
            StringRequest(vUrl,
                { response ->
                    Timber.e("----response=" + response)
                   stringBuffer.append("v http keyboard:$response\n\n")
                    logData.postValue(stringBuffer.toString())
                }
            ) { error ->
                val errorStr = "型号:"+Build.MODEL+" android版本："+Build.VERSION.SDK_INT+"\n"+"error="+error?.message
                Timber.e("----ErrorListener=" + error?.message)
                stringBuffer.append("v keyboard http error:"+errorStr+" " +error?.message+"\n\n")
                logData.postValue(stringBuffer.toString())

            }
        stringRequest.retryPolicy = DefaultRetryPolicy(
            TimeUnit.SECONDS.toMillis(20)
                .toInt(),  //After the set time elapses the request will timeout
            0,
            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        )
        BaseApplication.getBaseApplication().requestQueue.add(stringRequest)



        val httpsRequest =
            StringRequest(httpsUrl,
                { response ->
                    Timber.e("----response=" + response)
                    stringBuffer.append("v keyboard https:$response\n\n")
                    logData.postValue(stringBuffer.toString())
                }
            ) { error ->
                val errorStr = "型号:"+Build.MODEL+" android版本："+Build.VERSION.SDK_INT+"\n"+"error="+error?.message
                Timber.e("----ErrorListener=" + error?.message)
                stringBuffer.append("v keyboard https error:"+errorStr+" " +error?.message+"\n\n")
                logData.postValue(stringBuffer.toString())

            }
        stringRequest.retryPolicy = DefaultRetryPolicy(
            TimeUnit.SECONDS.toMillis(20)
                .toInt(),  //After the set time elapses the request will timeout
            0,
            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        )
        BaseApplication.getBaseApplication().requestQueue.add(httpsRequest)



        val httpUrl = "http://47.106.139.220:8089/find_app_update"

        val stringRequest3: StringRequest = object : StringRequest(
            Method.POST, httpUrl,
            Response.Listener<String> { response ->
                Timber.e("----response=" + response)
                stringBuffer.append("v aiHealth:$response\n\n")
                                      },
            Response.ErrorListener { error ->
                Timber.e("----ErrorListener=" + error?.message)
                stringBuffer.append("v aiHealth error:"+error?.message+"\n\n")
                logData.postValue(stringBuffer.toString())
            }) {
            override fun getParams(): Map<String, String>? {
                //在这里设置须要post的參数
                val map: MutableMap<String, String> = HashMap()
                map["appName"] = "aiHealth"
                map["versionCode"] ="1"
                map["type"] = "1"
                return map
            }
        }

        stringRequest3.retryPolicy = DefaultRetryPolicy(
            TimeUnit.SECONDS.toMillis(20)
                .toInt(),  //After the set time elapses the request will timeout
            0,
            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        )
        BaseApplication.getBaseApplication().requestQueue.add(stringRequest3)





        //EasyHttp
        val config2 = EasyConfig.getInstance()
        config2.setServer("https://wuquedistribution.com:12349/")
        config2.into()
        EasyHttp.get(lifecycleOwner).api("recordDebugInfo?productNumber=c003&mac=测试&osType=1&content=内容&remark=备注&phoneModel=18888888888")
            .request(
                object : OnHttpListener<String> {
                    override fun onHttpSuccess(result: String?) {
                        Timber.e("----key=" + result)
                        stringBuffer.append("e keyboard:$result\n\n")
                    }

                    override fun onHttpFail(e: Exception?) {
                        Timber.e("--onHttpFail--key=" + e?.message)
                        stringBuffer.append("e keyboard fail:" + e?.message + "\n\n")
                    }

                    override fun onHttpEnd(call: Call?) {
                        super.onHttpEnd(call)
                        logData.postValue(stringBuffer.toString())
                    }

                })


        //请求aiHealth
        val config = EasyConfig.getInstance()
        config.setServer("https://wuquedistribution.com:12349/")
        config.into()
        EasyHttp.get(lifecycleOwner).api("recordDebugInfo?productNumber=c003&mac=测试&osType=1&content=内容&remark=备注&phoneModel=18888888888")
            .request(object : OnHttpListener<String> {
                override fun onHttpSuccess(result: String?) {
                    Timber.e("--ai--key=" + result)
                    stringBuffer.append("e aiHealth:$result\n\n")
                }

                override fun onHttpFail(e: java.lang.Exception?) {
                    Timber.e("--ai--onHttpFail=" + e?.message)
                    stringBuffer.append("e aiHealth: fail:" + e?.message+"\n\n")
                }

                override fun onHttpEnd(call: Call?) {
                    super.onHttpEnd(call)
                    logData.postValue(stringBuffer.toString())
                }
            })


    }


    fun setAppData(lifecycleOwner: LifecycleOwner,context: Context){
        val phoneModel = Build.MODEL
        EasyHttp.get(lifecycleOwner).api("recordDebugInfo?productNumber=123&osType=0&phoneModel=$phoneModel")
            .request(object : OnHttpListener<String>{
                override fun onHttpSuccess(result: String?) {
                    stringBuffer.append("上传="+Gson().toJson(result)+"\n")
                }

                override fun onHttpFail(e: java.lang.Exception?) {
                    stringBuffer.append("上传error="+e?.message+"\n")
                }

            })

        val httpUrl = "http://wuquedistribution.com:12348/recordDebugInfo?productNumber=123&osType=0&phoneModel=$phoneModel"

        val httpsRequest =
            StringRequest(httpUrl,
                { response ->
                    Timber.e("----response=" + response)
                    stringBuffer.append("v keyboard https:$response\n\n")
                    logData.postValue(stringBuffer.toString())
                }
            ) { error ->
                val errorStr = "型号:"+Build.MODEL+" android版本："+Build.VERSION.SDK_INT+"\n"+"error="+error?.message
                Timber.e("----ErrorListener=" + error?.message)
                stringBuffer.append("v keyboard https error:"+errorStr+" " +error?.message+"\n\n")
                logData.postValue(stringBuffer.toString())

            }
        httpsRequest.retryPolicy = DefaultRetryPolicy(
            TimeUnit.SECONDS.toMillis(20)
                .toInt(),  //After the set time elapses the request will timeout
            0,
            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        )
        BaseApplication.getBaseApplication().requestQueue.add(httpsRequest)

    }
}