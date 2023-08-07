package com.app.freya.utils

import android.graphics.Bitmap
import android.widget.ImageView
import com.app.freya.R
import com.app.freya.listeners.OnGetImgWidthListener
import com.blala.blalable.Utils
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import timber.log.Timber

import java.io.File
import java.io.FileOutputStream
import java.io.IOException

object ImgUtil {


     fun getDialContent(
        startKey: ByteArray,
        endKey: ByteArray,
        count: ByteArray,
        type: Int,
        position: Int,
        dialId: Int
    ): MutableList<List<ByteArray>> {

        val lenght = count.size + 17
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
                    array = Utils.hexStringToByte(keyValue(startKey, endKey, array,lenght))
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


    private fun keyValue(
        startKey: ByteArray,
        endKey: ByteArray,
        sendData: ByteArray,lenght : Int
    ): String {
        val length = Utils.getHexString(Utils.toByteArray(lenght))
        return "880000" + length + "000805010009" +  //索引,长度
                Utils.getHexString(startKey) +  //起始位
                Utils.getHexString(endKey) +  //结束位
                "0202FFFF" +  //含crc效验包,索引2,俩个字节的长度
                Utils.getHexString(sendData) //+
    }



//    fun loadImage(
//        context: Context?,
//        url: String?
//    ) {
//        ThreadUtils.submit {
//            var bitmap: Bitmap? = null
//            val file =
//                XingLianApplication.getXingLianApplication()
//                    .getExternalFilesDir(null)?.absolutePath //public绝对路径
//            val appDir = File(file, "iChat")
//            if (!appDir.exists()) {
//                appDir.mkdirs()
//            }
//            val pathName = "avatar.jpg"
//            val currentFile = File(appDir, pathName)
//            try {
//                bitmap = Glide.with(context!!)
//                    .asBitmap()
//                    .load(url)
//                    .diskCacheStrategy(DiskCacheStrategy.NONE)
//                    .into(
//                        Target.SIZE_ORIGINAL,
//                        Target.SIZE_ORIGINAL
//                    ).get()
//                if (bitmap != null) {
//                    saveImageToGallery(bitmap, currentFile)
//                }
//            } catch (e: Exception) {
//                e.printStackTrace()
//            }
//        }
//        object : Thread() {
//            override fun run() {
//
//            }
//        }.start()
//    }

    /**
     * 保存图片到相册
     *
     * @param bmp
     */
    fun saveImageToGallery(bmp: Bitmap, file: File?) {
        var fos: FileOutputStream? = null
        try {
            fos = FileOutputStream(file)
            bmp.compress(Bitmap.CompressFormat.JPEG, 100, fos)
            fos.flush()
//            TLog.error("保存完成" + file.toString())
//            Hawk.put(Config.database.IMG_HEAD, file.toString())
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            try {
                fos?.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    fun loadCircle(ivHead: ImageView, url: Any) {
        Glide.with(ivHead.context).load(url).circleCrop()
            //.skipMemoryCache(true)
//            .override(  Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)//加载原始图大小
            //  .format(DecodeFormat.PREFER_RGB_565)//设置通道减少内存
            // .diskCacheStrategy(DiskCacheStrategy.NONE)
            .into(ivHead)
    }

//
//    fun loadCircle(ivHead: ImageView, url: Any,isMain : Boolean) {
//        Glide.with(ivHead.context).load(url).circleCrop()
//            .placeholder(if(isMain) R.mipmap.icon_head_man else R.mipmap.icon_head_woman)
//            //.skipMemoryCache(true)
////            .override(  Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)//加载原始图大小
//            //  .format(DecodeFormat.PREFER_RGB_565)//设置通道减少内存
//            // .diskCacheStrategy(DiskCacheStrategy.NONE)
//            .into(ivHead)
//    }


    fun loadHead(ivHead: ImageView, url: Any) {
        Glide.with(ivHead.context).load(url)
            .circleCrop()
            .placeholder(R.mipmap.ic_launcher)
            .dontAnimate()
            .into(ivHead)
    }


    fun loadHead(ivHead: ImageView, url: Any,onGetImgWidthListener: OnGetImgWidthListener) {
        Glide.with(ivHead.context).asBitmap().load(url).into(object  : SimpleTarget<Bitmap>(){
            override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                val width = resource.width
                val height  = resource.height
                onGetImgWidthListener.backImgWidthAndHeight(width,height)

            }

        });
//            .circleCrop()
//            .placeholder(R.mipmap.icon_head)
//            .dontAnimate()
//            .into(ivHead)
    }



//    fun loadMeImgDialCircle(ivHead: ImageView, url: Any) {
//        Glide.with(ivHead.context).load(url).circleCrop()
//            .into(ivHead)
//    }
//
//    fun loadHomeCard(ivHead: ImageView, url: Any) {
//        Glide.with(ivHead.context).load(url)
//            .placeholder(R.mipmap.ic_launcher)
//            .dontAnimate()
//            .into(ivHead)
//    }
//
//    fun loadRound(ivHead: ImageView, url: Any, round: Int = 10) {
//        Glide.with(ivHead.context).load(url).apply(roundOptions(round))
//            .placeholder(R.mipmap.ic_launcher).dontAnimate().into(ivHead)
//    }
//
//    fun loadMapImg(ivHead: ImageView, url: Any, round: Int = 10) {
//        Glide.with(ivHead.context).load(url).apply(roundOptions(round))
//            .skipMemoryCache(true)
//            .format(DecodeFormat.PREFER_RGB_565)//设置通道减少内存
//            .diskCacheStrategy(DiskCacheStrategy.NONE)
//            .placeholder(R.mipmap.ic_launcher).dontAnimate().into(ivHead)
//    }


    private fun roundOptions(size: Int = 10) = RequestOptions.bitmapTransform(RoundedCorners(size))
}