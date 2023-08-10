package com.app.freya.second

import android.Manifest
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.DisplayMetrics
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.core.content.FileProvider
import com.app.freya.BaseApplication
import com.app.freya.R
import com.app.freya.action.AppActivity
import com.app.freya.ble.ConnStatus
import com.app.freya.dialog.DeleteDeviceDialog
import com.app.freya.dialog.ShowProgressDialog
import com.app.freya.img.CameraActivity
import com.app.freya.img.ImageSelectActivity
import com.app.freya.utils.BitmapAndRgbByteUtil
import com.app.freya.utils.CalculateUtils
import com.app.freya.utils.ImageUtils
import com.app.freya.utils.ImgUtil
import com.app.freya.utils.MmkvUtils
import com.app.freya.utils.ThreadUtils
import com.blala.blalable.Utils
import com.blala.blalable.keyboard.DialCustomBean
import com.blala.blalable.keyboard.KeyBoardConstant
import com.blala.blalable.listener.OnCommBackDataListener
import com.bonlala.widget.layout.SettingBar
import com.bumptech.glide.Glide
import com.bumptech.glide.load.MultiTransformation
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.target.Target
import com.hjq.permissions.OnPermissionCallback
import com.hjq.permissions.XXPermissions
import com.hjq.shape.layout.ShapeConstraintLayout
import com.hjq.toast.ToastUtils
import com.yalantis.ucrop.UCrop
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.File

class SecondGifHomeActivity : AppActivity() {






    //自定义
    private var secondGifCusLayout : LinearLayout ?= null
    //自动锁定
    private var secondGifDormancyLayout : ShapeConstraintLayout ?= null
    private var secondGifDormancyBar : SettingBar?= null
    //自定义速度
    private var secondCusSpeedLayout : ShapeConstraintLayout ?= null
    private var secondCusSpeedSettingBar : SettingBar ?= null

    //自定义的图片
    private var secondCusGifImageView : ImageView ?= null
    private var secondDefaultAnimationImgView : ImageView ?= null

    //裁剪图片
    private var cropImgPath: String? = null
    private var resultCropUri: Uri? = null

    private var saveCropPath : String ?= null

    //对象
    private var dialBean = DialCustomBean()


    private val handlers: Handler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            if (msg.what == 0x00) {
                cancelProgressDialog()
                val array = msg.obj as ByteArray
                val path = getExternalFilesDir(null)?.path

                // FileU.getFile(array,path,"gif.bin")
                setDialToDevice(array)

            }

            if (msg.what == 0x01) {
                cancelProgressDialog()
                val tempArray = msg.obj as ByteArray
                startDialToDevice(tempArray, false)
            }

            if (msg.what == 0x08) {
                val log = msg.obj as String
               // gifLogTv?.text = log

            }

        }
    }

    override fun getLayoutId(): Int {
        return R.layout.activity_second_gif_home_layout
    }

    override fun initView() {
        secondDefaultAnimationImgView = findViewById(R.id.secondDefaultAnimationImgView)
        secondGifCusLayout = findViewById(R.id.secondGifCusLayout)
        secondGifDormancyLayout = findViewById(R.id.secondGifDormancyLayout)
        secondGifDormancyBar = findViewById(R.id.secondGifDormancyBar)
        secondCusSpeedLayout = findViewById(R.id.secondCusSpeedLayout)
        secondCusSpeedSettingBar = findViewById(R.id.secondCusSpeedSettingBar)
        secondCusGifImageView = findViewById(R.id.secondCusGifImageView)


        secondGifCusLayout?.setOnClickListener(this)
        secondGifDormancyLayout?.setOnClickListener(this)
        secondCusSpeedLayout?.setOnClickListener(this)
        secondDefaultAnimationImgView?.setOnClickListener(this)

        //默认动画
        findViewById<LinearLayout>(R.id.secondDefaultAnimationLayout).setOnClickListener {

        }
//        val bitmap = BitmapFactory.decodeResource(resources,R.drawable.gif_preview)
//        val circularBitmapDrawable: RoundedBitmapDrawable =
//            RoundedBitmapDrawableFactory.create(context.resources, bitmap)
//        circularBitmapDrawable.isCircular = true
//        secondDefaultAnimationImgView?.setImageDrawable(circularBitmapDrawable)
        Glide.with(context).load(R.drawable.gif_preview).transform(MultiTransformation(CenterCrop(), CircleCrop())).skipMemoryCache(false).diskCacheStrategy(DiskCacheStrategy.ALL).placeholder(R.drawable.gif_preview).into(secondDefaultAnimationImgView!!)

    }

    override fun initData() {
        cropImgPath = this.getExternalFilesDir(Environment.DIRECTORY_DCIM)?.absolutePath


    }


    override fun onClick(view: View?) {
        super.onClick(view)

        val id = view?.id

        when (id){
            R.id.secondDefaultAnimationImgView->{   //默认动画
                if(BaseApplication.getBaseApplication().connStatus != ConnStatus.CONNECTED){
                    return
                }
                BaseApplication.getBaseApplication().bleOperate.setLocalKeyBoardDial()
            }
            R.id.secondGifCusLayout->{  //自定义
                showPhotoDialog()
            }
            R.id.secondGifDormancyLayout->{ //自动锁定
                startActivity(SecondAutoLockActivity::class.java)
            }
            R.id.secondCusSpeedLayout->{    //自定义速度
                val intent = Intent(this@SecondGifHomeActivity, SecondGifSpeedActivity::class.java)
                startActivityForResult(intent, 1001)
            }
        }
    }


    override fun onResume() {
        super.onResume()
        val time = MmkvUtils.getAutoLock()
        if(time != 0){
            secondGifDormancyBar?.rightText = if(time<60) time.toString()+resources.getString(R.string.string_second_time) else ((time/60).toString()+resources.getString(R.string.string_minute_time))
        }

    }


    //自定义弹窗
    private fun showPhotoDialog(){
        val dialog = DeleteDeviceDialog(this, com.bonlala.base.R.style.BaseDialogTheme)
        dialog.show()
        dialog.setTitleTxt(resources.getString(R.string.string_select_pick))
        dialog.setConfirmAndCancelTxt(resources.getString(R.string.string_take_photo),resources.getString(R.string.string_album))
        dialog.setConfirmBgColor(Color.parseColor("#292E3C"))
        dialog.setOnCommClickListener { position ->
            dialog.dismiss()
            if (position == 0x01) {   //相机
                checkCamera()
            }
            if (position == 0x00) {   //相册
                showSelectDialog()
            }
        }
        val window = dialog.window
        val windowLayout = window?.attributes
        val metrics2: DisplayMetrics = resources.displayMetrics
        val widthW: Int = metrics2.widthPixels

        windowLayout?.width = widthW
        windowLayout?.gravity = Gravity.BOTTOM
        window?.attributes = windowLayout
    }


    //选择图片，展示弹窗
    private fun showSelectDialog() {

        if (XXPermissions.isGranted(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE))) {
            choosePick()
            return
        }
        XXPermissions.with(this).permission(
            arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        ).request(object : OnPermissionCallback {
            override fun onGranted(permissions: MutableList<String>, all: Boolean) {
                if (all) {
                    choosePick()
                }
            }
        })
    }


    //判断是否有相机权限
    private fun checkCamera() {
        if (XXPermissions.isGranted(this, Manifest.permission.CAMERA)) {
            openCamera()

        } else {
            XXPermissions.with(this).permission(
                arrayOf(
                    Manifest.permission.CAMERA,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
            ).request(object : OnPermissionCallback {
                override fun onGranted(permissions: MutableList<String>, all: Boolean) {
                    if (all) {
                        openCamera()
                    }
                }

            })
        }
    }

    //相机拍照
    private fun openCamera() {
        // 点击拍照
        CameraActivity.start(this, object : CameraActivity.OnCameraListener {
            override fun onSelected(file: File) {
                Timber.e("--------xxxx=" + file.path)
                setSelectImg(file.path, 0)
            }

            override fun onError(details: String) {
                toast(details)
            }
        })
    }


    //选择图片
    private fun choosePick() {

        ImageSelectActivity.start(this@SecondGifHomeActivity
        ) { data -> setSelectImg(data.get(0), 0) }

    }


    private fun setSelectImg(localUrl: String, code: Int) {
        Timber.e("--------选择图片=$localUrl")
        if (localUrl.contains(".gif")) {
            // dealWidthGif(localUrl)

            val gifList = ImageUtils.getGifDataBitmap(File(localUrl))
            if (gifList.size < 1) {
                ToastUtils.show(resources.getString(R.string.string_gig_small))
                return
            }

            val intent = Intent(this@SecondGifHomeActivity, SecondGifSpeedActivity::class.java)
            intent.putExtra("file_url", localUrl)
            startActivityForResult(intent, 1001)
            return
        }

        val uri: Uri
        uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            FileProvider.getUriForFile(this, "$packageName.provider", File(localUrl))
        } else {
            Uri.fromFile(File(localUrl))
        }
        Timber.e("-----uri=$uri")

        val date = System.currentTimeMillis()/1000
        val path = "$cropImgPath/$date.jpg"
        this.saveCropPath = path
        val cropFile = File(path)
        val destinationUri = Uri.fromFile(cropFile)
        val uOPtions = UCrop.Options()
        uOPtions.withAspectRatio(16F,9F)
        uOPtions.withMaxResultSize(340,192)

        uOPtions.setFreeStyleCropEnabled(false)
        uOPtions.setHideBottomControls(true)
        UCrop.of(uri, destinationUri)
            .withOptions(uOPtions)
            .start(this)

    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == UCrop.REQUEST_CROP){
            //裁剪后的图片地址
            val cropFile = File(saveCropPath)
            if(cropFile != null){
                val b = BitmapFactory.decodeFile(cropFile.path)
                //计算偏移量
                val x: Int =  b.width / 2 - 160
                val y: Int = b.height / 2 - 81
                val resultBitmap = Bitmap.createBitmap(b, x, y, 320, 172)

                Glide.with(this).load(resultBitmap).into(secondCusGifImageView!!)
                ImageUtils.saveMyBitmap(resultBitmap,saveCropPath)

                Timber.e("-------裁剪后的图片=" + (File(saveCropPath)).path)
                val url = File(saveCropPath).path
                dialBean.imgUrl = url

                setDialToDevice(byteArrayOf(0x00))
            }

        }


        if (requestCode == 1001) {
            val url = data?.getStringExtra("url")
            Timber.e("------url=" + url)
            if (url != null) {
                dealWidthGif(url)
            }

        }
    }



    val dByteStr = StringBuilder()

    val cByteStr = StringBuilder()
    //处理gif的图片
    private fun dealWidthGif(gifPath: String) {
        if (BaseApplication.getBaseApplication().connStatus != ConnStatus.CONNECTED) {
            hideDialog()
            ToastUtils.show(resources.getString(R.string.string_device_not_connect))
            return
        }
        val gifList = ImageUtils.getGifDataBitmap(File(gifPath))
        Timber.e("-------gifList=" + gifList.size)
        if (gifList.size == 0) {
            ToastUtils.show(resources.getString(R.string.string_gig_small))
            return
        }
        //将图片转换成byte集合,得到gif D的数据
        dByteStr.delete(0, dByteStr.length)
        cByteStr.delete(0, cByteStr.length)

       // gifLogTv?.text = ""

        var arraySize = 0
        showProgressDialog("Loading...")

        GlobalScope.launch {
            for (i in 0 until gifList.size) {
                val beforeSize = arraySize

                val tempArray = Utils.intToByteArray(beforeSize)
                val tempStr = Utils.getHexString(tempArray)
                cByteStr.append(tempStr)

                val bitmap = gifList[i]
                val bitArray = BitmapAndRgbByteUtil.bitmap2RGBData(bitmap)
                arraySize += bitArray.size
                dByteStr.append(Utils.getHexString(bitArray))


            }

            Timber.e("-----111--c的内容=" + cByteStr)
            //得到D的数组
            val resultDArray = Utils.hexStringToByte(dByteStr.toString())
            //得到C的数组
            val resultCArray = Utils.hexStringToByte(cByteStr.toString())
            //得到B的数组
            val gifSpeed = MmkvUtils.getGifSpeed()
            val resultBArray = KeyBoardConstant.dealWidthBData(gifList.size, gifSpeed)

            val resultAllArray = KeyBoardConstant.getGifAArrayData(
                gifList.size,
                resultBArray,
                resultCArray,
                resultDArray
            )

            // val logStr = KeyBoardConstant.getStringBuffer()

            //Timber.e("-------结果="+resultDArray.size)

            val msg = handlers.obtainMessage()
            msg.what = 0x00
            msg.obj = resultAllArray
            handlers.sendMessageDelayed(msg, 500)

        }

        //得到C的内容
        Timber.e("----222---c的内容=" + cByteStr)

    }


    var grbByte = byteArrayOf()

    private fun setDialToDevice(byteArray: ByteArray) {
        if (BaseApplication.getBaseApplication().connStatus == ConnStatus.NOT_CONNECTED) {
            ToastUtils.show(resources.getString(R.string.string_device_not_connect))
            hideDialog()
            return
        }

        val isSynGif = byteArray.isNotEmpty() && byteArray.size > 10

        showProgressDialog(resources.getString(R.string.string_sync_ing))
        BaseApplication.getBaseApplication().connStatus = ConnStatus.IS_SYNC_DIAL
        //stringBuilder.delete(0,stringBuilder.length)
        //showLogTv()

        if (isSynGif) {
            startDialToDevice(byteArray, true)
            return
        }

        ThreadUtils.submit {
            val bitmap = Glide.with(this)
                .asBitmap()
                .load(dialBean.imgUrl)
                .into(
                    Target.SIZE_ORIGINAL,
                    Target.SIZE_ORIGINAL
                ).get()

            val tempBitmap = BitmapAndRgbByteUtil.compressImage(bitmap)
            Timber.e("--------bitmap大小=" + tempBitmap.byteCount + " " + bitmap.byteCount)
            val tempArray = BitmapAndRgbByteUtil.bitmap2RGBData(tempBitmap)
            val msg = handlers.obtainMessage()
            msg.what = 0x01
            msg.obj = tempArray
            handlers.sendMessageDelayed(msg, 100)
            Timber.e("------大小=" + grbByte.size)
            //   ImgUtil.loadMeImgDialCircle(imgRecall, bitmap)
        }

    }


    private var progressDialog: ShowProgressDialog? = null

    //显示弹窗
    private fun showProgressDialog(msg: String) {
        if (progressDialog == null) {
            progressDialog = ShowProgressDialog(this, com.bonlala.base.R.style.BaseDialogTheme)
        }
        if (progressDialog?.isShowing == false) {
            progressDialog?.show()
        }
        progressDialog?.setCancelable(false)
        progressDialog?.setShowMsg(msg)
    }


    //隐藏弹窗
    private fun cancelProgressDialog() {
        if (progressDialog != null) {
            progressDialog?.dismiss()
        }
    }



    private fun startDialToDevice(imgByteArray: ByteArray, isGIf: Boolean) {

        showProgressDialog("Loading...")
        grbByte = imgByteArray
        Timber.e("--------大小=" + grbByte.size)
        val uiFeature = 65533
        dialBean.uiFeature = uiFeature.toLong()
        dialBean.binSize = grbByte.size.toLong()
        dialBean.name = "12"
        dialBean.type = if (isGIf) 2 else 1

        val resultArray = KeyBoardConstant.getDialByte(dialBean)
        val str = Utils.formatBtArrayToString(resultArray)
        //stringBuilder.append("send 3.11.3 protocol:$str" + "\n" + "fileSize=" + grbByte.size)
        Timber.e("-------表盘指令=" + str)
        //showLogTv()


        BaseApplication.getBaseApplication().bleOperate.startFirstDial(
            resultArray
        ) { data -> //880000000000030f0904 02
            /**
             * 0x01：传入非法值。例如 0x00000000
            0x02：等待 APP 端发送表盘 FLASH 数据
            0x03：设备已经有存储这个表盘，设备端调用并显示
            0x04：设备存储空间不够，需要 APP 端调用 3.11.5 处理
            0x05：其他高优先级数据在处理
             */
            /**
             * 0x01：传入非法值。例如 0x00000000
            0x02：等待 APP 端发送表盘 FLASH 数据
            0x03：设备已经有存储这个表盘，设备端调用并显示
            0x04：设备存储空间不够，需要 APP 端调用 3.11.5 处理
            0x05：其他高优先级数据在处理
             */

//            stringBuilder.append("设备端返回指定非固化表盘概要信息状态指令: " + Utils.formatBtArrayToString(data) + "\n")
//            showLogTv()

            if (data.size == 11 && data[8].toInt() == 9 && data[9].toInt() == 4) {

                val codeStatus = data[10].toInt()
                if (codeStatus == 1) {
                    cancelProgressDialog()
                    ToastUtils.show(resources.getString(R.string.string_invalid_value))
                    return@startFirstDial
                }
                //设备存储空间不够
                if (codeStatus == 4) {
                    BaseApplication.getBaseApplication().connStatus = ConnStatus.CONNECTED

                }

                if (codeStatus == 5) {
                    cancelProgressDialog()
                    ToastUtils.show(resources.getString(R.string.string_device_busy))
                    BaseApplication.getBaseApplication().connStatus = ConnStatus.CONNECTED
                    return@startFirstDial
                }

                val array = KeyBoardConstant.getDialStartArray()
                // stringBuilder.append("3.10.3 APP 端设擦写设备端指定的 FLASH 数据块" + Utils.formatBtArrayToString(array)+"\n")
               // showLogTv()

                BaseApplication.getBaseApplication().bleOperate.setIndexDialFlash(array) { data ->
                    Timber.e("-----大塔=" + Utils.formatBtArrayToString(data))
                    //880000000000030f090402
                    //88 00 00 00 00 00 03 0e 08 04 02
                    if (data.size == 11 && data[0].toInt() == -120 && data[8].toInt() == 8 && data[9].toInt() == 4 && data[10].toInt() == 2) {

                        /**
                         * 0x01：不支持擦写 FLASH 数据
                         * 0x02：已擦写相应的 FLASH 数据块
                         */

                        //880000000000030e 08 04 02
                        /**
                         * 0x01：不支持擦写 FLASH 数据
                         * 0x02：已擦写相应的 FLASH 数据块
                         */
                        // stringBuilder.append("3.10.4 设备端返回已擦写 FLASH 数据块的状态" + Utils.formatBtArrayToString(data)+"\n")

                        // stringBuilder.append("开始发送flash数据" +"\n")
                       // showLogTv()

                        count = 5
                        //获取下装填，状态是3就继续进行
                        getDeviceStatus()

                    }

                }
            }
        }
    }

    //次数
    var count = 5
    private fun getDeviceStatus() {
        BaseApplication.getBaseApplication().bleOperate.setClearListener()
        BaseApplication.getBaseApplication().bleOperate.getKeyBoardStatus(object :
            OnCommBackDataListener {
            override fun onIntDataBack(value: IntArray?) {
                val code = value?.get(0)
                Timber.e("-------code=$code" + " " + count)
                if (code == 3) {
                    count = 5
                    toStartWriteDialFlash()
                } else {
                    if (count in 1..6) {
                        handlers.postDelayed(Runnable {
                            count--
                            getDeviceStatus()
                        }, 100)
                    } else {
                        cancelProgressDialog()
                        ToastUtils.show("设备正忙!")
                        count = 5
                    }
                }
            }

            override fun onStrDataBack(vararg value: String?) {

            }

        })
    }


    private fun toStartWriteDialFlash() {

        val start = Utils.toByteArrayLength(16777215, 4)
        val end = Utils.toByteArrayLength(16777215, 4)

        val startByte = byteArrayOf(
            0x00, 0xff.toByte(), 0xff.toByte(),
            0xff.toByte()
        )


        val resultArray = ImgUtil.getDialContent(startByte, startByte, grbByte, 1000 + 701, -100, 0)
        Timber.e("-------reaulstArray=" + resultArray.size + " " + resultArray[0].size)

        //计算总的包数
        var allPackSize = resultArray.size
        Timber.e("------总的包数=" + allPackSize)
        //记录发送的包数
        var sendPackSize = 0

        BaseApplication.getBaseApplication().bleOperate.writeDialFlash(
            resultArray
        ) { statusCode ->
            sendPackSize++


            //计算百分比
            var percentValue =
                CalculateUtils.div(sendPackSize.toDouble(), allPackSize.toDouble(), 2)
            val showPercent = CalculateUtils.mul(percentValue, 100.0).toInt()
            //gifLogTv?.text = sendPackSize.toString()+"/"+allPackSize+" "+showPercent
            showProgressDialog(resources.getString(R.string.string_sync_ing) + (if(showPercent>=100) 100 else showPercent ) + "%")

            /**
             * 0x01：更新失败
             * 0x02：更新成功
             * 0x03：第 1 个 4K 数据块异常（含 APP 端发擦写和实际写入的数据地址不一致），APP 需要重走流程
             * 0x04：非第 1 个 4K 数据块异常，需要重新发送当前 4K 数据块
             * 0x05：4K 数据块正常，发送下一个 4K 数据
             * 0x06：异常退出（含超时，或若干次 4K 数据错误，设备端处理）
             */

            /**
             * 0x01：更新失败
             * 0x02：更新成功
             * 0x03：第 1 个 4K 数据块异常（含 APP 端发擦写和实际写入的数据地址不一致），APP 需要重走流程
             * 0x04：非第 1 个 4K 数据块异常，需要重新发送当前 4K 数据块
             * 0x05：4K 数据块正常，发送下一个 4K 数据
             * 0x06：异常退出（含超时，或若干次 4K 数据错误，设备端处理）
             */
            if (statusCode == 1) {
                cancelProgressDialog()
                ToastUtils.show(resources.getString(R.string.string_update_failed))
                BaseApplication.getBaseApplication().connStatus = ConnStatus.CONNECTED
            }
            if (statusCode == 2) {
                cancelProgressDialog()
                ToastUtils.show(resources.getString(R.string.string_update_success))
                BaseApplication.getBaseApplication().connStatus = ConnStatus.CONNECTED
            }
            if (statusCode == 6) {
                cancelProgressDialog()
                ToastUtils.show(resources.getString(R.string.string_error_exit))
                BaseApplication.getBaseApplication().connStatus = ConnStatus.CONNECTED
            }
        }
    }


}