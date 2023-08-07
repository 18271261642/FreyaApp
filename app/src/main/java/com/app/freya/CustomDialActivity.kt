package com.app.freya

import android.Manifest
import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.*
import android.provider.MediaStore
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.FileProvider
import com.app.freya.action.AppActivity
import com.app.freya.ble.ConnStatus
import com.app.freya.dialog.ShowProgressDialog
import com.app.freya.img.CameraActivity
import com.app.freya.img.CameraActivity.OnCameraListener
import com.app.freya.img.ImageSelectActivity
import com.app.freya.utils.*
import com.blala.blalable.Utils
import com.blala.blalable.keyboard.DialCustomBean
import com.blala.blalable.keyboard.KeyBoardConstant
import com.blala.blalable.listener.OnCommBackDataListener
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.Target
import com.hjq.bar.OnTitleBarListener
import com.hjq.bar.TitleBar
import com.hjq.permissions.OnPermissionCallback
import com.hjq.permissions.XXPermissions
import com.hjq.shape.layout.ShapeConstraintLayout
import com.hjq.shape.view.ShapeTextView
import com.hjq.toast.ToastUtils
import com.yalantis.ucrop.UCrop
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.File

/**
 * 自定义表盘页面
 * Created by Admin
 *Date 2023/1/31
 */
class CustomDialActivity : AppActivity() {

    private val tags = "CustomDialActivity"

    private var customDialTitleBar: TitleBar? = null

    private var gifLogTv: TextView? = null

    //选择图片的按钮
    private var customSelectImgView: ImageView? = null

    //展示选择的图片
    private var customShowImgView: ImageView? = null

    //设置保存
    private var customSetDialTv: ShapeTextView? = null

    //相机
    private var cusDialCameraLayout: ShapeConstraintLayout? = null

    //相册
    private var cusDialAlbumLayout: ShapeConstraintLayout? = null

    private var dialHomeCustomSpeedTv: ShapeTextView? = null


    //对象
    private var dialBean = DialCustomBean()

//    private var logTv : TextView ?= null

    //拍照的url
    private var imageUri: Uri? = null

    private var lenght = 0


    private val stringBuilder = StringBuilder()


    //裁剪图片
    private var cropImgPath: String? = null
    private var resultCropUri: Uri? = null

    private var saveCropPath : String ?= null


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

//                getDeviceStatus(tempArray,false)
            }

            if (msg.what == 0x08) {
                val log = msg.obj as String
                gifLogTv?.text = log

            }

        }
    }


    override fun getLayoutId(): Int {
        return R.layout.activity_custom_dial_layout
    }

    override fun initView() {
        dialHomeCustomSpeedTv = findViewById(R.id.dialHomeCustomSpeedTv)
        customDialTitleBar = findViewById(R.id.customDialTitleBar)
        gifLogTv = findViewById(R.id.gifLogTv)
        cusDialAlbumLayout = findViewById(R.id.cusDialAlbumLayout)
        cusDialCameraLayout = findViewById(R.id.cusDialCameraLayout)
        customSelectImgView = findViewById(R.id.customSelectImgView)
        customShowImgView = findViewById(R.id.customShowImgView)
        customSetDialTv = findViewById(R.id.customSetDialTv)
        //  logTv = findViewById(R.id.logTv)

        setOnClickListener(
            customSelectImgView,
            customSetDialTv,
            cusDialAlbumLayout,
            cusDialCameraLayout,
            dialHomeCustomSpeedTv
        )

        findViewById<TextView>(R.id.tmpTv1).setOnClickListener {

            val array = byteArrayOf(0x09, 0x01, 0x00)
            val resultArray = Utils.getFullPackage(array)
//            BaseApplication.getBaseApplication().bleOperate.writeCommonByte(resultArray,object : WriteBackDataListener{
//                override fun backWriteData(data: ByteArray?) {
//                    Timber.e("-------result="+Utils.formatBtArrayToString(data))
//                }
//
//            })
        }

        customShowImgView?.setOnClickListener {

        }

        customDialTitleBar?.setOnTitleBarListener(object : OnTitleBarListener {
            override fun onLeftClick(view: View?) {
                finish()
            }

            override fun onTitleClick(view: View?) {
                BaseApplication.getBaseApplication().logStr = getLogTxt()
                startActivity(LogActivity::class.java)
            }

            override fun onRightClick(view: View?) {

            }

        })
    }

    override fun initData() {
        XXPermissions.with(this).permission(
            arrayOf(
                Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
        ).request { permissions, all -> }



        clearLog()
//        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
//            XXPermissions.with(this).permission(arrayOf(Manifest.permission.READ_MEDIA_IMAGES)).request { permissions, all -> }
//        }

//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
//            XXPermissions.with(this).permission(Manifest.permission.MANAGE_EXTERNAL_STORAGE).request{ per, all->}
//        }


//         cropImgPath = Environment.getExternalStorageDirectory().path + "/Download"
        cropImgPath = this.getExternalFilesDir(Environment.DIRECTORY_DCIM)?.absolutePath

        Timber.e("-----path=" + cropImgPath)
    }


    //从raw目录下获取dial
    private fun getDialForRaw() {
        val inputStream = resources.openRawResource(R.raw.gif_dial)
        val array = inputStream.readBytes()
        //setDialToDevice(array)

    }


    override fun onClick(view: View?) {
        super.onClick(view)
        val id = view?.id

        when (id) {
            //选择图片
            R.id.cusDialAlbumLayout -> {

//                getDialForRaw()
                showSelectDialog()
            }

            //保存
            R.id.customSetDialTv -> {
                //setDialToDevice(byteArrayOf(0x00))
            }

            //相机
            R.id.cusDialCameraLayout -> {
                checkCamera()
            }

            R.id.dialHomeCustomSpeedTv -> {
                val intent = Intent(this@CustomDialActivity, CustomSpeedActivity::class.java)
                // intent.putExtra("file_url",localUrl)
                startActivityForResult(intent, 1001)
                //  startActivity(CustomSpeedActivity::class.java)
            }
        }
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

        // 点击拍照
        CameraActivity.start(this, object : OnCameraListener {
            override fun onSelected(file: File) {
                Timber.e("--------xxxx=" + file.path)
                setSelectImg(file.path, 0)
//                // 当前选中图片的数量必须小于最大选中数
//                if (mSelectImage.size < mMaxSelect) {
//                    mSelectImage.add(file.path)
//                }
            }

            override fun onError(details: String) {
                toast(details)
            }
        })
    }


    private fun getDeviceStatus(imgByteArray: ByteArray, isGIf: Boolean) {
        BaseApplication.getBaseApplication().bleOperate.getKeyBoardStatus(object :
            OnCommBackDataListener {
            override fun onIntDataBack(value: IntArray?) {
                val code = value?.get(0)
                if (code == 88) {
                    //startDialToDevice(imgByteArray,isGIf)
                }
            }

            override fun onStrDataBack(vararg value: String?) {

            }

        })
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
        stringBuilder.append("send 3.11.3 protocol:$str" + "\n" + "fileSize=" + grbByte.size)
        Timber.e("-------表盘指令=" + str)
        showLogTv()


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

            stringBuilder.append("设备端返回指定非固化表盘概要信息状态指令: " + Utils.formatBtArrayToString(data) + "\n")
            showLogTv()

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
                showLogTv()

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
                        showLogTv()

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
        showLogTv()

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


    private fun toStartWriteDialFlash() {

        val start = Utils.toByteArrayLength(16777215, 4)
        val end = Utils.toByteArrayLength(16777215, 4)

        val startByte = byteArrayOf(
            0x00, 0xff.toByte(), 0xff.toByte(),
            0xff.toByte()
        )


        val resultArray = getDialContent(startByte, startByte, grbByte, 1000 + 701, -100, 0)
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


    private fun showLogTv() {
        // logTv?.text = stringBuilder.toString()
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
        ).request { permissions, all ->
            if (all) {
                choosePick()
            }
        }

    }


    //选择图片
    private fun choosePick() {

        ImageSelectActivity.start(this@CustomDialActivity
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

            val intent = Intent(this@CustomDialActivity, CustomSpeedActivity::class.java)
            intent.putExtra("file_url", localUrl)
            startActivityForResult(intent, 1001)
            return
        }

        val uri: Uri
        uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            FileProvider.getUriForFile(this, "com.app.smartkeyboard.provider", File(localUrl))
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


//        startPhotoZoom(uri, code)
    }


    /**
     * 调用系统裁剪
     *
     */
    private fun startPhotoZoom(uri: Uri, code: Int) {
//        cropImgPath = Environment.getExternalStorageDirectory().path + "/Download"
        cropImgPath = this.getExternalFilesDir(Environment.DIRECTORY_DCIM)?.absolutePath

        try {
            val intent = Intent("com.android.camera.action.CROP")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                //添加这一句表示对目标应用临时授权该Uri所代表的文件
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            intent.flags = Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            val date = System.currentTimeMillis().toString()
            cropImgPath = "$cropImgPath/$date.jpg"
            Timber.e("--cropPath=$cropImgPath")
            val cutFile = File(cropImgPath)
            var cRui = Uri.fromFile(cutFile)
            Timber.e("----000--cRui=$cRui")
            if (Build.VERSION.SDK_INT >= 24) {
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                if (context == null) {
                    Toast.makeText(this, "getContext = null", Toast.LENGTH_SHORT).show()
                    return
                }
                cRui = FileProvider.getUriForFile(
                    context,
                    "com.app.smartkeyboard.provider",
                    cutFile
                )
                Timber.e("----11--cRui=$cRui")
                this.resultCropUri = cRui
            } else {
                cRui = Uri.fromFile(cutFile)
                this.resultCropUri = cRui
                Timber.e("----22--cRui=$cRui")
            }

            //所有版本这里都这样调用
            intent.putExtra(MediaStore.EXTRA_OUTPUT, cRui)
            //输入图片路径
            intent.setDataAndType(uri, "image/*")
            intent.putExtra("crop", "true")
            intent.putExtra("aspectX", 2)
            intent.putExtra("aspectY", 1)
            intent.putExtra("outputX", 320)
            intent.putExtra("outputY", 172)
            intent.putExtra("scale", false)
            intent.putExtra("scaleUpIfNeeded", true)
            intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString())
            intent.putExtra("return-data", false)

            Timber.e("------Build.VERSION.SDK_INT=" + Build.VERSION.SDK_INT)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                grantPermissionFix(intent, cRui)
            }
            // grantPermissionFix(intent, cRui);

            //重要！！！添加权限，不然裁剪完后报 “保存时发生错误，保存失败” （我的小米10.0系统是这样）
            val resInfoList =
                packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)
            for (resolveInfo in resInfoList) {
                //我用的小米手机 packageName 得到的是：com.miui.gallery
                val packageName = resolveInfo.activityInfo.packageName
                grantUriPermission(
                    packageName,
                    cRui,
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
                //注意不是 getPackageName()！！ getPackageName()得到的是app的包名
//            grantUriPermission(getPackageName(), cropUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
            }
            val componentName = intent.resolveActivity(
                packageManager
            )

            startActivityForResult(intent, code)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Timber.e("-----onActivityResult=" + requestCode + " " + resultCode)
        if (resultCode == RESULT_OK) {

            if(requestCode == UCrop.REQUEST_CROP){
                //裁剪后的图片地址

                val cropFile = File(saveCropPath)
                if(cropFile != null){
                    val b = BitmapFactory.decodeFile(cropFile.path)

                    //计算偏移量
                    val x: Int =  b.width / 2 - 160
                    val y: Int = b.height / 2 - 81
                    val resultBitmap = Bitmap.createBitmap(b, x, y, 320, 172)

                    ImageUtils.saveMyBitmap(resultBitmap,saveCropPath)

                    Glide.with(this@CustomDialActivity).load(saveCropPath).into(customShowImgView!!)

                    Timber.e("-------裁剪后的图片=" + (File(saveCropPath)).path)
                    val url = File(saveCropPath).path
                    dialBean.imgUrl = url


                    setDialToDevice(byteArrayOf(0x00))

                }

            }



            if(requestCode == 1002){
                val uri = data?.data
                Timber.e("--ri="+uri.toString())

                if (uri != null) {
                    startPhotoZoom(uri,0)
                }
                if(uri != null){
                    val path = ImageUtils.getPathToUri(this,uri)
                    Timber.e("---path="+path)
                }
            }


            if ((requestCode == 0x01 || requestCode == 0x00) && resultCode == RESULT_OK) {
                if (data == null) return
                // 得到图片的全路径
                val cropUri = data.data
                Timber.e("--------后的图片=" + (cropUri == null) + " " + (resultCropUri == null))

//                Glide.with(this@CustomDialActivity).load(cropUri).into(customShowImgView!!)
//
//                Timber.e("-------裁剪后的图片=" + (File(cropImgPath)).path)
//                val url = File(cropImgPath).path
//                dialBean.imgUrl = url
//
//
//                setDialToDevice(byteArrayOf(0x00))
            }

            if (requestCode == 1001) {
                val url = data?.getStringExtra("url")
                Timber.e("------url=" + url)
                if (url != null) {
                    dealWidthGif(url)
                }

            }
        }


    }


    private fun keyValue(
        startKey: ByteArray,
        endKey: ByteArray,
        sendData: ByteArray
    ): String {
        val length = Utils.getHexString(Utils.toByteArray(lenght))
        return "880000" + length + "000805010009" +  //索引,长度
                Utils.getHexString(startKey) +  //起始位
                Utils.getHexString(endKey) +  //结束位
                "0202FFFF" +  //含crc效验包,索引2,俩个字节的长度
                Utils.getHexString(sendData) //+
    }


    private fun getDialContent(
        startKey: ByteArray,
        endKey: ByteArray,
        count: ByteArray,
        type: Int,
        position: Int,
        dialId: Int
    ): MutableList<List<ByteArray>> {

        lenght = count.size + 17
        var mList: MutableList<List<ByteArray>> = mutableListOf()
        var arraySize: Int = count.size / 4096

        //  var arraySize: Int = count.size / 4096
        val list: MutableList<ByteArray> = mutableListOf()
        if (count.size % 4096 > 0) {
            arraySize += 1
        }

        Timber.e("-------总的4096个包:" + arraySize)

        for (i in 0 until arraySize) {
            val srcStart = i * 4096
            var array = ByteArray(4096)
            if (count.size - srcStart < 4096) {
                array = ByteArray(count.size - srcStart)
                System.arraycopy(count, srcStart, array, 0, count.size - srcStart)
            } else
                System.arraycopy(count, srcStart, array, 0, array.size)
            list.add(array)
        }


        list.forEachIndexed { index, childrArry ->
            val ll: MutableList<ByteArray> = mutableListOf()
            var arraySize2: Int = childrArry.size / 243
            if (childrArry.size % 243 > 0) {
                arraySize2 += 1
            }

//            if (index == 0) {
//                TLog.error("arraySize2==" + arraySize2)
//                TLog.error("count.size==" + count.size)
//                TLog.error("childrArry.size==" + childrArry.size)
//            }

            for (i in 0 until arraySize2) {
                var array = ByteArray(243)
                if (i == 0 && index == 0) { //只有第一位的第一个需要
                    array = ByteArray(218)
                    System.arraycopy(childrArry, 0, array, 0, array.size)
//                    array = Utils.hexStringToByte(keyValue(startKey, endKey, array,array.size))
                    array = Utils.hexStringToByte(keyValue(startKey, endKey, array))
                    Timber.e("arrayi == 0 && index == 0==" + Utils.getHexString(array))
                } else if (i == (arraySize2 - 1)) {
                    var srcStart = i * 243
                    if (index == 0)
                        srcStart -= 25
//                    TLog.error("srcStart++"+srcStart)
//                    TLog.error("childrArry.size++"+childrArry.size)
                    val num = childrArry.size - (srcStart)
                    array = ByteArray(num)
//                    TLog.error("array.size++"+array.size)
                    System.arraycopy(childrArry, srcStart, array, 0, array.size)
                } else {
                    var srcStart = i * 243
                    if (index == 0) {
                        srcStart -= 25
//                        TLog.error("srcStart=="+srcStart)
//                        TLog.error("array=="+ByteUtil.getHexString(array))
//                        TLog.error("array=="+ array.size)
//                        TLog.error("srcStart==" +  srcStart)
                        //                       TLog.error("array==" +  array.size)
                    }

                    System.arraycopy(childrArry, srcStart, array, 0, array.size)
                    //  if(index==0)
//                    TLog.error("arrayi == ${i}=="+ByteUtil.getHexString(array))
                }
                val arrayXOR = Utils.byteMerger(array, Utils.byteXOR(array))
                if (i == 0 && index == 0) {
                    Timber.e("------第一=" + Utils.formatBtArrayToString(arrayXOR))
                }
                ll.add(arrayXOR)
            }
            mList.add(ll)
        }

        Timber.e("---------第一包=" + Utils.formatBtArrayToString(mList.get(0).get(0)))

        return mList
    }


    override fun onDestroy() {
        super.onDestroy()
        BaseApplication.getBaseApplication().connStatus = ConnStatus.CONNECTED
    }

    private fun grantPermissionFix(intent: Intent, uri: Uri) {
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
        val resolveInfos: List<ResolveInfo> =
            packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)
        for (resolveInfo in resolveInfos) {
            val packageName: String = resolveInfo.activityInfo.packageName
            grantUriPermission(
                packageName,
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            )
            intent.action = null
            intent.component =
                ComponentName(resolveInfo.activityInfo.packageName, resolveInfo.activityInfo.name)
            break
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

        gifLogTv?.text = ""

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

    private var progressDialog: ShowProgressDialog? = null

    //显示弹窗
    fun showProgressDialog(msg: String) {
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
    fun cancelProgressDialog() {
        if (progressDialog != null) {
            progressDialog?.dismiss()
        }
    }

    private fun clearLog() {
        stringBuilder.delete(0, stringBuilder.length)
    }

    private fun getLogTxt(): String {
        return stringBuilder.toString()
    }
}