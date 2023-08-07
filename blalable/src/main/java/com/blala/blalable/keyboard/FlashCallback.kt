package com.blala.blalable.keyboard

import com.blala.blalable.Utils

/**
 * Created by Admin
 *Date 2023/2/1
 */
open class FlashCallback {


    fun getDialContent(
        startKey: ByteArray,
        endKey: ByteArray,
        count: ByteArray,
        length: Int,
        type: Int,
        position: Int,
        dialId: Int
    ): MutableList<List<ByteArray>> {

        var mList: MutableList<List<ByteArray>> = mutableListOf()
        var arraySize: Int = count.size / 4096

        //  var arraySize: Int = count.size / 4096
        val list: MutableList<ByteArray> = mutableListOf()
        if (count.size % 4096 > 0) {
            arraySize += 1
        }
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
                    array = Utils.hexStringToByte(keyValue(startKey, endKey, array,array.size))

                    //TLog.error("arrayi == 0 && index == 0==" + ByteUtil.getHexString(array))
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
                ll.add(arrayXOR)
            }
            mList.add(ll)
        }

        return mList

    }


    fun keyValue(
        startKey: ByteArray,
        endKey: ByteArray,
        sendData: ByteArray, lengths: Int
    ): String {
        val length = Utils.getHexString(Utils.toByteArray(lengths))
        return "880000" + length + "000805010009" +  //索引,长度
                Utils.getHexString(startKey) +  //起始位
                Utils.getHexString(endKey) +  //结束位
                "0202FFFF" +  //含crc效验包,索引2,俩个字节的长度
                Utils.getHexString(sendData) //+
    }

}