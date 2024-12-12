package com.capstone.emoticalm

import android.graphics.Bitmap
import android.graphics.Color
import java.nio.ByteBuffer


class YuvToRgbConverter {
    companion object {
        // Convert YUV image data (YUV_420_888) to Bitmap
        fun convertToBitmap(yuvBytes: ByteArray, width: Int, height: Int): Bitmap? {
            val expectedSize = width * height * 3 / 2 // Assuming NV21 format or YUV_420_888
            if (yuvBytes.size != expectedSize) {
                throw IllegalArgumentException("YUV data size is not valid. Expected size: $expectedSize, but got: ${yuvBytes.size}")
            }

            val ySize = width * height
            val uvSize = ySize / 4

            // Byte arrays to hold Y, U, and V values
            val y = ByteArray(ySize)
            val u = ByteArray(uvSize)
            val v = ByteArray(uvSize)

            // Copy Y, U, and V byte arrays from YUV data
            System.arraycopy(yuvBytes, 0, y, 0, ySize)
            System.arraycopy(yuvBytes, ySize, u, 0, uvSize)
            System.arraycopy(yuvBytes, ySize + uvSize, v, 0, uvSize)

            val rgb = ByteArray(3 * ySize)

            // Convert YUV to RGB
            for (i in 0 until ySize) {
                val yValue = y[i].toInt() and 0xFF
                val uValue = u[i / 2].toInt() and 0xFF
                val vValue = v[i / 2].toInt() and 0xFF

                // YUV to RGB conversion formula
                val r = yValue + 1.402 * (vValue - 128)
                val g = yValue - 0.344136 * (uValue - 128) - 0.714136 * (vValue - 128)
                val b = yValue + 1.772 * (uValue - 128)

                // Clamp RGB values to valid range [0, 255]
                val rClamped = r.coerceIn(0.0, 255.0).toInt()
                val gClamped = g.coerceIn(0.0, 255.0).toInt()
                val bClamped = b.coerceIn(0.0, 255.0).toInt()

                // Store RGB in the output array
                rgb[i * 3] = rClamped.toByte()
                rgb[i * 3 + 1] = gClamped.toByte()
                rgb[i * 3 + 2] = bClamped.toByte()
            }

            // Create a Bitmap from the RGB byte array
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)

            // Set the RGB values to the Bitmap
            for (i in 0 until height) {
                for (j in 0 until width) {
                    val pixelIndex = (i * width + j) * 3
                    val r = rgb[pixelIndex].toInt() and 0xFF
                    val g = rgb[pixelIndex + 1].toInt() and 0xFF
                    val b = rgb[pixelIndex + 2].toInt() and 0xFF

                    val color = Color.rgb(r, g, b)
                    bitmap.setPixel(j, i, color)
                }
            }

            return bitmap
        }
    }
}
