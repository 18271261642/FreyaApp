package com.app.freya

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.TextView
import com.app.freya.action.AppActivity
import com.app.freya.gif.GifMaker
import com.app.freya.utils.ImageUtils
import com.app.freya.utils.MmkvUtils
import com.hjq.shape.view.ShapeTextView
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import pl.droidsonroids.gif.GifDrawable
import pl.droidsonroids.gif.GifImageView
import timber.log.Timber
import java.io.Closeable
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream


class CustomSpeedActivity : AppActivity() {


    private var previewGifImageView: ImageView? = null
    private var customSeekBar: SeekBar? = null

    private var seekBarValueTv: TextView? = null

    private var gifImageView : GifImageView ?= null

    //是否是自定义的速度，选择gif后的自定义速度
    private var isCustomSped = false

    private var cusSpeedSaveTv : ShapeTextView ?= null

    //传递过来的文件地址
    private var dialFileUrl : String ?= null


    var gifPath: String? = null

    private val handlers : Handler = object : Handler(Looper.getMainLooper()){
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            if(msg.what == 0x00){
//                val previewFile = File(gifPath + "/previews.gif")
//                Timber.e("-----路径="+previewFile.path)
//                Glide.with(this@CustomSpeedActivity).asGif().load(previewFile).into(previewGifImageView!!)

                val previewFile = File(gifPath + "/previews.gif")
                dialFileUrl = previewFile.path
                Timber.e("-----previewFile="+previewFile.path)
                val gifDrawable = GifDrawable(previewFile)
               // gifDrawable.stop()
                val m = resources.displayMetrics
                val width = m.widthPixels
                val height = m.heightPixels
                Timber.e("-----widht="+width)
                gifImageView?.minimumWidth = 800
                gifImageView?.minimumHeight = 300
                gifImageView?.setImageDrawable(gifDrawable)
//                gifDrawable.start()
                Timber.e("-------次数="+gifDrawable.loopCount)
            }

            if(msg.what == 0x01){
                val progress = msg.obj as Int
                saveBitmap(progress)
            }

            if(msg.what == 0x99){
                val previewFile = File(gifPath + "/previews.gif")
                createGif(previewFile.path)
            }
        }
    }

    override fun getLayoutId(): Int {
        return R.layout.activity_custom_speed_layout
    }

    override fun initView() {
        cusSpeedSaveTv = findViewById(R.id.cusSpeedSaveTv)
        gifImageView = findViewById(R.id.gifImageView)
        seekBarValueTv = findViewById(R.id.seekBarValueTv)
        gifPath = getExternalFilesDir(null)?.path

        previewGifImageView = findViewById(R.id.previewGifImageView)
        customSeekBar = findViewById(R.id.customSeekBar)
        customSeekBar?.max = 10
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            customSeekBar?.min = 1
        }

        cusSpeedSaveTv?.setOnClickListener {

            val intent = Intent()
            intent.putExtra("url",dialFileUrl)
            this.setResult(Activity.RESULT_OK,intent)
            finish()
        }


        customSeekBar?.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {

            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
             
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                Timber.e("-----onStartTrackingTouch---="+seekBar?.progress)
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                Timber.e("-----onStopTrackingTouch---="+seekBar?.progress)
                val progress = seekBar?.progress
                if (progress != null) {
                    MmkvUtils.saveGifSpeed(progress)
                }
                seekBarValueTv?.text = progress.toString()


                if (progress != null) {
                    changeGifSpeed(progress)
                }

            }

        })
    }



    var gifMaker :GifMaker ?= null



    override fun initData() {

      val speed = MmkvUtils.getGifSpeed()
        customSeekBar?.max = 10
        customSeekBar?.progress = speed
        seekBarValueTv?.text = speed.toString()

        val url = intent.getStringExtra("file_url")
        Timber.e("-----url="+url)
        //先判断一下已经选择过的是否存在，存在就显示
        val historyFile = File(gifPath + "/previews.gif")
        if( url != null){   //存在
            this.dialFileUrl = url
            //生成gif
            createGif(url)
        }else{  //不存在
            if(historyFile.exists()){
                this.dialFileUrl = historyFile.path
                createGif(historyFile.path)
            }else{
                copyGifToSd()
            }
        }
    }


    //生成gif
    private fun createGif(url : String){
        val gifList = ImageUtils.getGifDataBitmap(File(url))
        val duration = ImageUtils.getGifAnimationDuration(File(url))
        val speed = MmkvUtils.getGifSpeed()
        val realSpeed = 11-speed
        Timber.e("------duraing="+duration+" "+speed)
        gifMaker = GifMaker(1)
        gifMaker?.setOnGifListener { current, total ->
            if (current + 1 == total) {
                GlobalScope.launch {
                    // Glide.get(this@CustomSpeedActivity).clearDiskCache()
                    val message = handlers.obtainMessage()
                    message.what = 0x00
                    message.obj = speed
                    handlers.sendMessageDelayed(message,300)
                   // handlers.sendEmptyMessageDelayed(0x01, 300)
                }
            }
        }
        GlobalScope.launch {
            gifMaker?.makeGif(gifList, gifPath + "/previews.gif",realSpeed*30)
        }
    }



    private fun changeGifSpeed(speed : Int){
        Timber.e("------速度+"+speed)
        val pickList = ImageUtils.getGifDataBitmap(File(dialFileUrl))
        val markGif = GifMaker(1)
        val realSpeed = 11-speed
        markGif.setOnGifListener { current, total ->
            if(current+1 == total){
                val message = handlers.obtainMessage()
                message.what = 0x00
                message.obj = speed
                handlers.sendMessageDelayed(message,300)
            }
        }
        GlobalScope.launch {
            markGif.makeGif(pickList,gifPath + "/previews.gif",realSpeed*30)
        }

        MmkvUtils.saveGifSpeed(speed)
    }


    private fun saveBitmap(speed : Int) {
        //val bitmap = BitmapFactory.decodeResource(resources,R.drawable.gif_speed)
        seekBarValueTv?.text = speed.toString()
        var drawable : GifDrawable ?= null
        val file = File(gifPath + "/previews.gif")
        this.dialFileUrl = file.path
        drawable = GifDrawable(file)

//        if(isCustomSped){
//            val file = File(gifPath + "/previews.gif")
//            drawable = GifDrawable(file)
//        }else{
//            drawable = GifDrawable(resources,R.drawable.gif_preview)
//        }
        Timber.e("-----速度="+speed+" "+speed*30)
       drawable.setSpeed(speed.toFloat())
        gifImageView?.minimumWidth = 800
        gifImageView?.minimumHeight = 300
        gifImageView?.setImageDrawable(drawable)

        MmkvUtils.saveGifSpeed(speed)

    }


    //将gif放到sd卡本地
    private fun copyGifToSd(){
        GlobalScope.launch {

            val assets = getAssets()
            val inputStream = assets.open("gif_preview.gif")
            copyFile2Local(inputStream,gifPath + "/previews.gif")

        }

    }

    /**
     * 拷贝文件至本地
     *
     * @param srcInputStream 源文件输入流
     * @param destFilePath   目标文件路径
     */
    @Throws(IOException::class)
    private fun copyFile2Local(srcInputStream: InputStream, destFilePath: String) {
        val destFile = File(destFilePath)
//        if (!destFile.isFile || !destFile.canRead()) {
//            return
//        }
//        if (destFile.exists() && destFile.length() == srcInputStream.available().toLong()) {
//            return
//        }
//        try {
//            destFile.createNewFile()
//        } catch (e: IOException) {
//            e.printStackTrace()
//        }
        val fos = FileOutputStream(destFile, false)
        val buffer = ByteArray(1024)
        var len: Int
        while (srcInputStream.read(buffer).also { len = it } > 0) {
            fos.write(buffer, 0, len)
        }
        fos.flush()
        closeStream(fos)
        closeStream(srcInputStream)

        handlers.sendEmptyMessage(0x99)
    }

    /**
     * 关闭输入输出流
     *
     * @param stream 流
     */
    private fun closeStream(stream: Closeable?) {
        try {
            stream?.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}