package com.qupai.lib_printer.fagao_p510

import android.content.Context
import android.graphics.Bitmap
import android.hardware.usb.UsbDevice
import android.text.TextUtils
import com.blankj.utilcode.util.ImageUtils
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.StringUtils.getString
import com.blankj.utilcode.util.Utils
import com.idp.jsmartcomm2.JSmartComm2
import com.idp.jsmartcomm2.refInt
import com.idp.jsmartcomm2.refLong
import com.idp.jsmartcomm2.refString
import com.qupai.lib_printer.QuPrinter
import com.qupai.lib_printer.R
import com.qupai.lib_printer.entity.EventPrintResult
import com.qupai.lib_printer.entity.PrintBean
import com.qupai.lib_printer.response.IPrintResultListener
import com.qupai.lib_printer.response.ResponsePrinter
import org.greenrobot.eventbus.EventBus
import java.io.File
import java.util.concurrent.Executors

object PrintFaGaoP510 {

    private var m_usbPrinter: USBPrinterReceiver? = null


    private val smart = JSmartComm2()
    private var smartUsbDevice: UsbDevice? = null
    private var smartFileDescriptor = -1
    private var smartIsOpen = false
    private const val SUCCESS = 0
    private const val FAIL = -1
    const val SMART_ORIENTATION_PORTRAIT = 1
    const val SMART_ORIENTATION_LANDSCAPE = 2

    const val DCL_PAGE_MODE_STANDARD = "MODE_STANDARD"
    const val DCL_PAGE_MODE_PARTIAL = "MODE_PARTIAL"

    const val DCL_PAGE_SIDE_FRONT = "SIDE_FRONT"
    const val DCL_PAGE_SIDE_BACK = "SIDE_BACK"

    const val DCL_PAGE_EJECT_YES = "EJECT_YES"
    const val DCL_PAGE_EJECT_NO = "EJECT_NO"


    const val SMART_SURFACE_COLOR = 0
    const val SMART_SURFACE_RESIN = 1
    const val SMART_SURFACE_FLUORESCENT = 2
    const val SMART_SURFACE_OVERLAY = 3

    // 智能图像比例最佳
    const val SMART_IMAGE_SCALE_BEST = 0

    // 智能图像缩放宽度
    const val SMART_IMAGE_SCALE_WIDTH = 1

    // 智能图像缩放高度
    const val SMART_IMAGE_SCALE_HEIGHT = 2

    // 智能图像缩放填充
    const val SMART_IMAGE_SCALE_FULL = 3


    const val DCL_PAGE_ROTATE180_YES = "ROTATE180_YES"
    const val DCL_PAGE_ROTATE180_NO = "ROTATE180_NO"

    const val DCL_PAGE_RESOLUTION_300 = "RESOLUTION_300"
    const val DCL_PAGE_RESOLUTION_600 = "RESOLUTION_600"
    const val DCL_PAGE_RESOLUTION_1200 = "RESOLUTION_1200"

    private var printListsReal: ArrayList<PrintBean> = ArrayList()
    private var printerType = ""

    private var ribbonSize = -1
    private var serialNum = ""
    private var printVersion = ""

    private val statusList = mutableListOf<Long>()


    fun initPrint() {
        if (m_usbPrinter != null && !isOpen()) {
            LogUtils.i("已经初始化过")
            return
        }
        m_usbPrinter = USBPrinterReceiver()
        val context = Utils.getApp()
        m_usbPrinter!!.RegisterIntentFilterForPermission(context)
        m_usbPrinter!!.FindPrinter(context)
        m_usbPrinter!!.RegisterIntentFilterForAttach(context)
        getStatus()
        if (statusList.contains(PrintFaGaoCode.S51PS_S_SBSMODE)) {
            smart.SBSEnd()
        }
        getStatus()
    }

    fun releasePrint() {
        val context = Utils.getApp()
        context.unregisterReceiver(m_usbPrinter)
    }

    fun LibUsbInit(context: Context?, usbDevice: UsbDevice, fileDescriptor: Int) {
        val r: Int = smart.OpenDeviceFd(fileDescriptor)
        smartUsbDevice = usbDevice
        smartFileDescriptor = fileDescriptor
        smartIsOpen = r == 0
        LogUtils.i("com.qupai.lib_printer.renwoyin.bean.Printer:${usbDevice.productName}  OpenDevice() = $r    smartIsOpen:$smartIsOpen")
        getStatus()
    }

    fun LibUsbFini() {
        var retVal = 0
        var msg = ""
        retVal = smart.CloseDevice()
        msg += if (retVal == SUCCESS) "CloseDevice : $retVal" else "CloseDevice Failed"
        LogUtils.i("$msg")
        smartIsOpen = false
        LogUtils.i("com.qupai.lib_printer.renwoyin.bean.Printer is detached")

        ribbonSize = -1
    }

    fun isOpen() = smartIsOpen

    fun doCheckStatus(): String {
        val status = getStatus()
        if (TextUtils.isEmpty(status))
            return "no connection"
        return status
    }

    fun getSerialNum(): String {
        if (TextUtils.isEmpty(serialNum))
            getStatus()
        return serialNum
    }

    fun getRibbonSize(): Int {
        var retVal = 0
        LogUtils.i("isOpen:${isOpen()}")
        if (isOpen()) {
            val ribbon = refInt(0)
            retVal = smart.GetRibbonType(ribbon)
            if (retVal == SUCCESS) {
                LogUtils.i("ribbon:${ribbon.value}")
            } else {
                LogUtils.e("GetRibbonType Failed")
            }
            retVal = smart.GetRibbonRemain(ribbon)
            if (retVal == SUCCESS) {
                ribbonSize = ribbon.value
                LogUtils.i("ribbon:${ribbon.value}")
            } else {
                ribbonSize = -1
                LogUtils.e("GetRibbonRemain Failed")
            }

        } else {
        }
        return ribbonSize
    }

    fun getStatus(): String {
        var retVal = 0
        var msg = ""
        statusList.clear()
        LogUtils.i("isOpen:${isOpen()}")
        if (isOpen()) {
            val ribbon = refInt(0)
            val serial = refString("")
            val version = refString("")
            val status = refLong(0)
            retVal = smart.GetSerial(serial)
            if (retVal == SUCCESS) {
                serialNum = serial.value
                LogUtils.i("Serial:${serial.value}")
            } else {
                serialNum = ""
                LogUtils.e("GetSerial Failed")
            }

            retVal = smart.GetVersion(version)
            if (retVal == SUCCESS) {
                printVersion = version.value
                LogUtils.i("version:${version.value}")
            } else {
                printVersion = ""
                LogUtils.e("GetVersion Failed")
            }
            retVal = smart.GetStatus(status)
            if (retVal == SUCCESS) {
                val statusHex = padHexString(java.lang.Long.toHexString(status.value))
                LogUtils.i("status:0x$statusHex")
//                val statusList = mutableListOf<Long>()
                for (i in statusHex.indices) {
                    val c = statusHex[i]
                    if (c != '0') {
                        val status = "${get0(i)}${c}${get0(statusHex.length - i - 1)}"
                        statusList.add(status.toLong(16))
                        LogUtils.i("i:$i , status:0x$status")
                    }
                }
                if (statusList.isEmpty()) {
                    // 无异常
                    LogUtils.i("打印机正常 不支持双面打印")
                    msg = Utils.getApp().getString(R.string.ok)
                } else {
                    if (
                        statusList.size == 2
                        && statusList.contains(PrintFaGaoCode.S51PS_S_CONNFLIPPER)
                        && statusList.contains(PrintFaGaoCode.S51PS_S_FLIPPERTOP)
                    ) {
                        LogUtils.i("打印机正常 支持双面打印")
                        msg = Utils.getApp().getString(R.string.ok)
                    } else {
                        Utils.getApp().getString(R.string.error, statusHex)
                    }

                    if (statusList.contains(PrintFaGaoCode.S51PS_S_CARDEMPTY)) {
                        LogUtils.i("卡盒无卡")
                    }
                    if (statusList.contains(PrintFaGaoCode.S51PS_S_SBSMODE)) {
                        LogUtils.i("当前是SBS模式")
                    }
                }

            } else {
                LogUtils.e("GetStatus Failed")
            }
            retVal = smart.GetRibbonType(ribbon)
            if (retVal == SUCCESS) {
                LogUtils.i("ribbon:${ribbon.value}")
            } else {
                LogUtils.e("GetRibbonType Failed")
            }
            retVal = smart.GetRibbonRemain(ribbon)
            if (retVal == SUCCESS) {
                ribbonSize = ribbon.value
                LogUtils.i("ribbon:${ribbon.value}")
            } else {
                ribbonSize = -1
                LogUtils.e("GetRibbonRemain Failed")
            }

        } else {
        }
        return msg
    }

    private fun get0(size: Int): String? {
        val sb = java.lang.StringBuilder()
        for (i in 0 until size) {
            sb.append("0")
        }
        return sb.toString()
    }

    private fun padHexString(hexString: String): String {
        // 检查输入的字符串是否是有效的十六进制字符串
        try {
            hexString.toLong(16)
        } catch (e: NumberFormatException) {
            LogUtils.e("解析错误：$hexString")
            return hexString
        }

        // 计算需要补零的个数
        val paddingLength = 16 - hexString.length

        // 如果需要补零，则在字符串前面添加对应个数的零
        return if (paddingLength > 0) {
            val paddedHexString = StringBuilder()
            for (i in 0 until paddingLength) {
                paddedHexString.append("0")
            }
            paddedHexString.append(hexString)
            paddedHexString.toString()
        } else {
            hexString
        }
    }


    fun doPrint(
        printLists: ArrayList<PrintBean>,
        printerType: String,
    ) {
        this.printerType = printerType
        if (isOpen()) {
            Executors.newSingleThreadExecutor().execute {
                setSuitPrintListLooper(printLists)
                LogUtils.i("打印张数：${printListsReal.size}")
                var ret = SUCCESS
                printListsReal.forEachIndexed { index, printBean ->
                    ret = printCard(printBean)
                    if (ret == FAIL) {
                        LogUtils.e("print index:$index")
                        return@forEachIndexed
                    }
                }
                if (ret == SUCCESS) {
                    onPrintResult(getString(R.string.print_success), ResponsePrinter.PRINTER_OK)
                } else {
//                    onPrintResult(
//                        "${getString(R.string.printer_connect_fail)}",
//                        ResponsePrinter.PRINTER_ERROR
//                    )
                }
            }
        } else {
            val msg = "no connection"
            onPrintResult(msg, ResponsePrinter.PRINTER_ERROR)
        }

    }

    private fun setSuitPrintListLooper(printLists: ArrayList<PrintBean>) {
        printListsReal.clear()
        for (printBean in printLists) {
            val path = getRightImage(printBean.printPhotoPath, 90)
            val backPath = if (TextUtils.isEmpty(printBean.backPrintPhotoPath)) {
                ""
            } else {
                getRightImage(printBean.backPrintPhotoPath, 270)
            }
            for (i in 0 until printBean.printNum) {
                printListsReal.add(
                    PrintBean(
                        printPhotoPath = path,
                        backPrintPhotoPath = backPath,
                        printNum = 1,
                        isCut = false
                    )
                )
            }
        }
    }

    private fun getRightImage(imagePath: String, rotate: Int): String {
        var result = ""
        var bitmap = ImageUtils.getBitmap(imagePath)
        if (bitmap.width < bitmap.height) {
            bitmap = ImageUtils.rotate(
                bitmap,
                rotate,
                (bitmap.width / 2).toFloat(),
                (bitmap.height / 2).toFloat()
            )
        } else {
//            return imagePath
        }
        result = ImageUtils.save2Album(bitmap, Bitmap.CompressFormat.JPEG)!!.absolutePath

        val oldFilePath = result
        val oldFile = File(oldFilePath)

        // 新文件路径
        val newFilePath = result.replace(".JPG", ".jpg")
        val newFile = File(newFilePath)
        // 执行重命名操作
        val isRenamed = oldFile.renameTo(newFile)
        if (isRenamed) {
            return newFile.absolutePath
        } else {
            LogUtils.e("重名错误")
        }
        return result
    }




    private fun onPrintResult(msg: String, statusRet: Int) {
        getStatus()
        QuPrinter.sendPrintResultMsg(msg, statusRet, printerType)
    }


    private fun printCard(printBean: PrintBean): Int {
        var isFail = false
        var retVal = 0
        val path = printBean.printPhotoPath
        val backPath = printBean.backPrintPhotoPath

        LogUtils.i("path exists:${File(path).exists()}")
        if (!TextUtils.isEmpty(backPath)) {
            LogUtils.i("backPath exists:${File(backPath).exists()}")
        }

        if (!isOpen()) {
            onPrintResult("no connection", ResponsePrinter.PRINTER_ERROR)
            return FAIL
        }
        if (TextUtils.isEmpty(backPath)) {

            // 单面打印

            retVal = smart.DclInitSurface(SMART_ORIENTATION_LANDSCAPE);
            LogUtils.i("DclInitSurface $retVal")

            retVal = smart.DclSetPage(DCL_PAGE_MODE_PARTIAL);
            LogUtils.i("DclSetPage DCL_PAGE_MODE_PARTIAL $retVal")

            retVal = smart.DclDrawImage(
                SMART_SURFACE_COLOR,
                0,
                0,
                1012,
                636,
                255,
                255,
                255,
                SMART_IMAGE_SCALE_FULL,
                path
            );
            LogUtils.i("DclDrawImage $retVal")

            retVal = smart.Print("");
            LogUtils.i("Print $retVal")
        } else {

            // 打印页面1
            retVal = smart.DclInitSurface(SMART_ORIENTATION_LANDSCAPE);
            LogUtils.i("DclInitSurface $retVal")

            retVal = smart.DclSetPage(DCL_PAGE_MODE_PARTIAL);
            LogUtils.i("DclSetPage DCL_PAGE_MODE_PARTIAL $retVal")

            retVal = smart.DclSetPage(DCL_PAGE_SIDE_FRONT);
            LogUtils.i("DclSetPage DCL_PAGE_SIDE_FRONT $retVal")

            retVal = smart.DclSetPage(DCL_PAGE_EJECT_NO);
            LogUtils.i("DclSetPage DCL_PAGE_SIDE_FRONT $retVal")


            retVal = smart.DclDrawImage(
                SMART_SURFACE_COLOR,
                0,
                0,
                1012,
                636,
                255,
                255,
                255,
                SMART_IMAGE_SCALE_FULL,
                path
            );
            LogUtils.i("DclDrawImage1 $retVal")
            if (retVal == FAIL) {
                onPrintResult("DclDrawImage1 Failed", ResponsePrinter.PRINTER_ERROR)
                isFail = true
            }

            retVal = smart.Print("");
            LogUtils.i("Print1 $retVal")
            if (retVal == FAIL) {
                onPrintResult("Print1 Failed", ResponsePrinter.PRINTER_ERROR)
                isFail = true
            }

            // 打印页面2
            retVal = smart.DclInitSurface(SMART_ORIENTATION_LANDSCAPE);
            LogUtils.i("DclInitSurface $retVal")

            retVal = smart.DclSetPage(DCL_PAGE_MODE_PARTIAL);
            LogUtils.i("DclSetPage DCL_PAGE_MODE_PARTIAL $retVal")

            retVal = smart.DclSetPage(DCL_PAGE_SIDE_BACK);
            LogUtils.i("DclSetPage DCL_PAGE_SIDE_FRONT $retVal")

//                retVal = smart.DclSetPage(DCL_PAGE_EJECT_NO);
//                LogUtils.i("DclSetPage DCL_PAGE_SIDE_FRONT $retVal")

            retVal = smart.DclDrawImage(
                SMART_SURFACE_COLOR,
                0,
                0,
                1012,
                636,
                255,
                255,
                255,
                SMART_IMAGE_SCALE_FULL,
                backPath
            );
            LogUtils.i("DclDrawImage2 $retVal")
            if (retVal == FAIL) {
                onPrintResult("DclDrawImage2 Failed", ResponsePrinter.PRINTER_ERROR)
                isFail = true
            }

            retVal = smart.Print("");
            LogUtils.i("Print2 $retVal")
            if (retVal == FAIL) {
                onPrintResult("Print2 Failed", ResponsePrinter.PRINTER_ERROR)
                isFail = true
            }
        }


        /*

                // sbs start
                retVal = smart.SBSStart()
                LogUtils.i("SBSStart : $retVal")
                if (retVal == FAIL) {
                    onPrintResult("SBSStart Fail", ResponsePrinter.PRINTER_ERROR)
                    SBSEnd()
                    return FAIL
                }

                retVal = smart.CardIn()
                LogUtils.i("CardIn : $retVal")
                if (retVal == FAIL) {
                    onPrintResult("CardIn Fail", ResponsePrinter.PRINTER_ERROR)
                    SBSEnd()
                    return FAIL
                }


                retVal = smart.DclInitSurface(SMART_ORIENTATION_LANDSCAPE)
                LogUtils.i("DclInitSurface SMART_ORIENTATION_LANDSCAPE : $retVal")

                retVal = smart.DclSetPage(DCL_PAGE_MODE_PARTIAL)
                LogUtils.i("DclInitSurface DCL_PAGE_MODE_PARTIAL : $retVal")


                retVal = smart.DclSetPage(DCL_PAGE_ROTATE180_YES)
                LogUtils.i("DclInitSurface DCL_PAGE_ROTATE180_YES : $retVal")

                retVal = smart.DclSetPage(DCL_PAGE_RESOLUTION_600)
                LogUtils.i("DclInitSurface DCL_PAGE_RESOLUTION_600 : $retVal")

                retVal = smart.DclDrawImage(
                    SMART_SURFACE_COLOR,
                    0,
                    0,
                    1012,
                    636,
                    255,
                    255,
                    255,
                    SMART_IMAGE_SCALE_FULL,
                    path
                )
                LogUtils.i("DclDrawImage : $retVal")


                retVal = smart.Print("") // color profile for RIBBON_TYPE_PREMIUM
                LogUtils.i("Print : $retVal")

                retVal = smart.DoPrint()
                LogUtils.i("DoPrint : $retVal")


                if (retVal == SUCCESS) {
                    retVal = smart.CardOutBack()
                    LogUtils.i("CardOutBack : $retVal")
                } else {
                    retVal = smart.CardOut()
                    LogUtils.i("CardOut : $retVal")
                }

                retVal = SBSEnd()
                LogUtils.i("SBSEnd : $retVal")
        */

        return if (isFail) FAIL else SUCCESS
    }

    private fun SBSEnd(): Int {
        val retVal = smart.SBSEnd()
        LogUtils.i("SBSEnd : $retVal")
        if (retVal == FAIL) {
            return FAIL
        }
        return retVal
    }
}