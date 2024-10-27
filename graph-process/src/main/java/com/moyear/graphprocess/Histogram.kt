package com.moyear.graphprocess

import android.graphics.Bitmap
import android.graphics.Color

class Histogram {

    companion object {

        /**
         * 计算bitmap整体的直方图
         *  @param channel 颜色通道值，0代表全部通道，1、2、3分别为R、G、B通道，4为Alpha通道
         */
        @JvmStatic
        fun calculateHistogram(bitmap: Bitmap, channel: Int = 0): IntArray? {
            if (channel < 0 || channel > 4) return null

            val histogram = IntArray(256)

            for (x in 0 until bitmap.width) {
                for (y in 0 until bitmap.height) {
                    val pixel = bitmap.getPixel(x, y)

                    val value = when(channel) {
                        1 -> Color.red(pixel)
                        2 -> Color.green(pixel)
                        3 -> Color.blue(pixel)
                        else -> (Color.red(pixel) + Color.green(pixel) + Color.blue(pixel)) / 3
                    }
                    histogram[value]++
                }
            }
            return histogram
        }

//        /**
//         *
//         * @param bitmap
//         * @param channel 颜色通道值，0代表全部通道，1、2、3分别为R、G、B通道，4为Alpha通道
//         */
//        fun calculateHistogram(bitmap: Bitmap, channel: Int): IntArray? {
//            if (channel < 0 || channel > 4) return null
//
//
//            val histogram = IntArray(256)
//
//            return histogram
//
//        }
    }

}

