package com.app.freya.viewmodel

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import com.app.freya.BaseApplication
import com.app.freya.utils.GsonUtils
import com.hjq.http.EasyHttp
import com.hjq.http.listener.OnHttpListener
import org.json.JSONObject
import java.lang.Exception

class SecondHomeViewModel : ViewModel() {

    //获取所有的设备类型列表
    fun getAllSupportDeviceType(lifecycleOwner: LifecycleOwner){
        EasyHttp.get(lifecycleOwner).api("productNumberList").request(object : OnHttpListener<String>{
            override fun onHttpSuccess(result: String?) {
                val jsonObject = JSONObject(result)
                if(jsonObject.getInt("code")==200){
                    val data = jsonObject.getString("data")
                    val list = GsonUtils.getGsonObject<List<String>>(data)
                    list?.forEach {
                        BaseApplication.supportDeviceTypeMap[it] = it
                    }

                }

            }

            override fun onHttpFail(e: Exception?) {

            }

        })
    }



}