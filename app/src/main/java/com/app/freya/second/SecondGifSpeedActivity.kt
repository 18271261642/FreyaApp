package com.app.freya.second

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import com.app.freya.R
import com.app.freya.action.AppActivity
import com.app.freya.gif.GifMaker
import com.app.freya.utils.ImageUtils
import com.app.freya.utils.MmkvUtils
import com.hjq.shape.view.ShapeTextView
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import pl.droidsonroids.gif.GifDrawable
import timber.log.Timber
import java.io.Closeable
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream

class SecondGifSpeedActivity : AppActivity() {

    //图片
    private var secondGifSpeedImageView: ImageView? = null

    //当前速度
    private var secondGifSpeedTv: TextView? = null

    private var secondSpeedSeekBar: SeekBar? = null

    var gifPath: String? = null

    //传递过来的文件地址
    private var dialFileUrl: String? = null

    var gifMaker: GifMaker? = null


    private val handlers: Handler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            if (msg.what == 0x00) {
//                val previewFile = File(gifPath + "/previews.gif")
//                Timber.e("-----路径="+previewFile.path)
//                Glide.with(this@CustomSpeedActivity).asGif().load(previewFile).into(previewGifImageView!!)

                val previewFile = File(gifPath + "/previews.gif")
                dialFileUrl = previewFile.path
                Timber.e("-----previewFile=" + previewFile.path)
                val gifDrawable = GifDrawable(previewFile)
                // gifDrawable.stop()
                val m = resources.displayMetrics
                val width = m.widthPixels
                val height = m.heightPixels
                Timber.e("-----widht=" + width)
                secondGifSpeedImageView?.minimumWidth = 800
                secondGifSpeedImageView?.minimumHeight = 300
                secondGifSpeedImageView?.setImageDrawable(gifDrawable)
//                gifDrawable.start()
                Timber.e("-------次数=" + gifDrawable.loopCount)
            }

            if (msg.what == 0x01) {
                val progress = msg.obj as Int
                saveBitmap(progress)
            }

            if (msg.what == 0x99) {
                val previewFile = File(gifPath + "/previews.gif")
                createGif(previewFile.path)
            }
        }
    }


    override fun getLayoutId(): Int {
        return R.layout.activity_second_gif_speed_layout
    }

    override fun initView() {
        secondGifSpeedImageView = findViewById(R.id.secondGifSpeedImageView)
        secondGifSpeedTv = findViewById(R.id.secondGifSpeedTv)
        secondSpeedSeekBar = findViewById(R.id.secondSpeedSeekBar)

        //确定
        findViewById<ShapeTextView>(R.id.secondGifConfirmTv).setOnClickListener {
            val intent = Intent()
            intent.putExtra("url",dialFileUrl)
            this.setResult(Activity.RESULT_OK,intent)
            finish()
        }
        secondSpeedSeekBar?.max = 10
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            secondSpeedSeekBar?.min = 1
        }

        secondSpeedSeekBar?.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {

            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {

            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                Timber.e("-----onStartTrackingTouch---=" + seekBar?.progress)
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                Timber.e("-----onStopTrackingTouch---=" + seekBar?.progress)
                val progress = seekBar?.progress
                if (progress != null) {
                    MmkvUtils.saveGifSpeed(progress)
                }
                secondGifSpeedTv?.text = progress.toString()


                if (progress != null) {
                    changeGifSpeed(progress)
                }
            }

        })
    }

    override fun initData() {
        gifPath = getExternalFilesDir(null)?.path
        val speed = MmkvUtils.getGifSpeed()
        secondSpeedSeekBar?.max = 10
        secondSpeedSeekBar?.progress = speed
        secondGifSpeedTv?.text = speed.toString()

        val url = intent.getStringExtra("file_url")
        Timber.e("-----url=" + url)
        //先判断一下已经选择过的是否存在，存在就显示
        val historyFile = File(gifPath + "/previews.gif")
        if (url != null) {   //存在
            this.dialFileUrl = url
            //生成gif
            createGif(url)
        } else {  //不存在
            if (historyFile.exists()) {
                this.dialFileUrl = historyFile.path
                createGif(historyFile.path)
            } else {
                copyGifToSd()
            }
        }
    }


    private fun saveBitmap(speed: Int) {
        //val bitmap = BitmapFactory.decodeResource(resources,R.drawable.gif_speed)
        secondGifSpeedTv?.text = speed.toString()
        var drawable: GifDrawable? = null
        val file = File(gifPath + "/previews.gif")
        this.dialFileUrl = file.path
        drawable = GifDrawable(file)

        Timber.e("-----速度=" + speed + " " + speed * 30)
        drawable.setSpeed(speed.toFloat())
        secondGifSpeedImageView?.minimumWidth = 800
        secondGifSpeedImageView?.minimumHeight = 300
        secondGifSpeedImageView?.setImageDrawable(drawable)

        MmkvUtils.saveGifSpeed(speed)

    }

    //生成gif
    private fun createGif(url: String) {
        val gifList = ImageUtils.getGifDataBitmap(File(url))
        val duration = ImageUtils.getGifAnimationDuration(File(url))
        val speed = MmkvUtils.getGifSpeed()
        val realSpeed = 11 - speed
        Timber.e("------duraing=" + duration + " " + speed)
        gifMaker = GifMaker(1)
        gifMaker?.setOnGifListener { current, total ->
            if (current + 1 == total) {
                GlobalScope.launch {
                    // Glide.get(this@CustomSpeedActivity).clearDiskCache()
                    val message = handlers.obtainMessage()
                    message.what = 0x00
                    message.obj = speed
                    handlers.sendMessageDelayed(message, 300)
                    // handlers.sendEmptyMessageDelayed(0x01, 300)
                }
            }
        }
        GlobalScope.launch {
            gifMaker?.makeGif(gifList, gifPath + "/previews.gif", realSpeed * 30)
        }
    }


    private fun changeGifSpeed(speed: Int) {
        Timber.e("------速度+" + speed)
        val pickList = ImageUtils.getGifDataBitmap(File(dialFileUrl))
        val markGif = GifMaker(1)
        val realSpeed = 11 - speed
        markGif.setOnGifListener { current, total ->
            if (current + 1 == total) {
                val message = handlers.obtainMessage()
                message.what = 0x00
                message.obj = speed
                handlers.sendMessageDelayed(message, 300)
            }
        }
        GlobalScope.launch {
            markGif.makeGif(pickList, gifPath + "/previews.gif", realSpeed * 30)
        }

        MmkvUtils.saveGifSpeed(speed)
    }


    //将gif放到sd卡本地
    private fun copyGifToSd() {
        GlobalScope.launch {
            val assets = getAssets()
            val inputStream = assets.open("gif_preview.gif")
            copyFile2Local(inputStream, gifPath + "/previews.gif")
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