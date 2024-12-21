package com.qupai.util

import android.graphics.Bitmap
import android.os.Environment
import com.blankj.utilcode.util.Utils
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException


object BMPUtils {
    fun save2Bmp(bitmap: Bitmap): String {
        var filePath = ""
        val w = bitmap.width
        val h = bitmap.height
        val pixels = IntArray(w * h)
        bitmap.getPixels(pixels, 0, w, 0, 0, w, h)
        val rgb = addBMP_RGB_888(pixels, w, h)
        val header = addBMPImageHeader(rgb.size)
        val infos = addBMPImageInfosHeader(w, h)
        val buffer = ByteArray(54 + rgb.size)
        System.arraycopy(header, 0, buffer, 0, header.size)
        System.arraycopy(infos, 0, buffer, 14, infos.size)
        System.arraycopy(rgb, 0, buffer, 54, rgb.size)
        try {
            val rootPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).toString() + File.separator + Utils.getApp().packageName
            val file = File(rootPath)
            if (file.exists() && file.isDirectory) {

            } else {
                file.mkdirs()
            }
            filePath = rootPath + File.separator + System.currentTimeMillis() + ".bmp"
            val fos = FileOutputStream(filePath)
            fos.write(buffer)
            //            ZipUtil.zip(EvidenceApplication.getContext().getExternalFilesDir("finger") + "/finger.bmp", EvidenceApplication.getContext().getExternalFilesDir("finger") + "/finger.zip");
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return filePath
    }

    /**
     * 文件头
     */
    private fun addBMPImageHeader(size: Int): ByteArray {
        val buffer = ByteArray(14)
        buffer[0] = 0x42
        buffer[1] = 0x4D
        buffer[2] = (size shr 0).toByte()
        buffer[3] = (size shr 8).toByte()
        buffer[4] = (size shr 16).toByte()
        buffer[5] = (size shr 24).toByte()
        buffer[6] = 0x00
        buffer[7] = 0x00
        buffer[8] = 0x00
        buffer[9] = 0x00
        buffer[10] = 0x36
        buffer[11] = 0x00
        buffer[12] = 0x00
        buffer[13] = 0x00
        return buffer
    }

    /**
     * 文件信息头
     *
     * @param w
     * @param h
     * @return
     */
    private fun addBMPImageInfosHeader(w: Int, h: Int): ByteArray {
        val buffer = ByteArray(40)
        buffer[0] = 0x28
        buffer[1] = 0x00
        buffer[2] = 0x00
        buffer[3] = 0x00
        buffer[4] = (w shr 0).toByte()
        buffer[5] = (w shr 8).toByte()
        buffer[6] = (w shr 16).toByte()
        buffer[7] = (w shr 24).toByte()
        buffer[8] = (h shr 0).toByte()
        buffer[9] = (h shr 8).toByte()
        buffer[10] = (h shr 16).toByte()
        buffer[11] = (h shr 24).toByte()
        buffer[12] = 0x01
        buffer[13] = 0x00
        buffer[14] = 0x18
        buffer[15] = 0x00
        buffer[16] = 0x00
        buffer[17] = 0x00
        buffer[18] = 0x00
        buffer[19] = 0x00
        buffer[20] = 0x00
        buffer[21] = 0x00
        buffer[22] = 0x00
        buffer[23] = 0x00
        buffer[24] = 0xE0.toByte()
        buffer[25] = 0x01
        buffer[26] = 0x00
        buffer[27] = 0x00
        buffer[28] = 0x02
        buffer[29] = 0x03
        buffer[30] = 0x00
        buffer[31] = 0x00
        buffer[32] = 0x00
        buffer[33] = 0x00
        buffer[34] = 0x00
        buffer[35] = 0x00
        buffer[36] = 0x00
        buffer[37] = 0x00
        buffer[38] = 0x00
        buffer[39] = 0x00
        return buffer
    }

    private fun addBMP_RGB_888(b: IntArray, w: Int, h: Int): ByteArray {
        val len = b.size
        println(b.size)
        val buffer = ByteArray(w * h * 3)
        var offset = 0
        var i = len - 1
        while (i >= w) {

            //DIB文件格式最后一行为第一行，每行按从左到右顺序
            val end = i
            val start = i - w + 1
            for (j in start..end) {
                buffer[offset] = (b[j] shr 0).toByte()
                buffer[offset + 1] = (b[j] shr 8).toByte()
                buffer[offset + 2] = (b[j] shr 16).toByte()
                offset += 3
            }
            i -= w
        }
        return buffer
    }

    fun saveBitmapAsBMP(bitmap: Bitmap) {
        val width = bitmap.width
        val height = bitmap.height
        val pixels = IntArray(width * height)
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)

        // BMP文件头
        val bmpSize = width * height * 3 // 每个像素3个字节
        val bmpFileHeaderSize = 14
        val bmpInfoHeaderSize = 40
        val fileSize = bmpFileHeaderSize + bmpInfoHeaderSize + bmpSize
        val fileHeader = byteArrayOf(
            0x42, 0x4D, fileSize.toByte(), (fileSize shr 8).toByte(), (fileSize shr 16).toByte(), (fileSize shr 24).toByte(),  // 文件大小
            0, 0,  // 保留字段
            0, 0, (bmpFileHeaderSize + bmpInfoHeaderSize).toByte(), 0, 0, 0 // 文件头+信息头大小
        )
        val infoHeader = byteArrayOf(
            bmpInfoHeaderSize.toByte(), 0, 0, 0, width.toByte(), 0, 0, 0, height.toByte(), 0, 0, 0,  // 图像高度
            1, 0,  // 颜色平面
            24, 0,  // 每个像素位数
            0, 0, 0, 0, bmpSize.toByte(), (bmpSize shr 8).toByte(), (bmpSize shr 16).toByte(), (bmpSize shr 24).toByte(),  // 图像大小
            0, 0, 0, 0,  // 水平分辨率
            0, 0, 0, 0,  // 垂直分辨率
            0, 0, 0, 0,  // 颜色索引数
            0, 0, 0, 0 // 重要颜色索引数
        )
        var filePath = ""
        try {
            val rootPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).toString() + File.separator + Utils.getApp().packageName
            filePath = rootPath + File.separator + System.currentTimeMillis() + ".bmp"
            val fos = FileOutputStream(filePath)
//            fos.write(buffer)


//            val file: File = File(getExternalFilesDir(null), "output.bmp")
//            val fos = FileOutputStream(file)
            fos.write(fileHeader)
            fos.write(infoHeader)
            for (i in height - 1 downTo 0) {
                for (j in 0 until width) {
                    val pixel = pixels[i * width + j]
                    fos.write(pixel shr 0 and 0xFF)
                    fos.write(pixel shr 8 and 0xFF)
                    fos.write(pixel shr 16 and 0xFF)
                }
            }
            fos.close()
//            Toast.makeText(this, "BMP file saved to " + file.absolutePath, Toast.LENGTH_SHORT).show()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

}