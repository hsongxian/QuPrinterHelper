package com.qupai.lib_printer.hiti_u826

import android.annotation.SuppressLint
import android.content.res.AssetManager
import android.graphics.Bitmap
import android.util.Log
import com.blankj.utilcode.util.ImageUtils
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.StringUtils
import com.blankj.utilcode.util.ThreadUtils
import com.blankj.utilcode.util.Utils
import com.qupai.lib_printer.R
import com.qupai.lib_printer.entity.EventPrintResult
import com.qupai.lib_printer.entity.PrintBean
import com.qupai.lib_printer.response.IPrintResultListener
import com.qupai.lib_printer.response.ResponsePrinter
import com.uni.usb.jni.JniData
import com.uni.usb.printer.PrinterJob
import com.uni.usb.printer.PrinterStatus
import com.uni.usb.service.Action
import com.uni.usb.service.ErrorCode
import com.uni.usb.service.ServiceConnector
import com.uni.usb.utility.ByteUtility
import com.uni.usb.utility.FileUtility
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.disposables.Disposable
import org.greenrobot.eventbus.EventBus
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.concurrent.TimeUnit

@SuppressLint("StaticFieldLeak")
object PrinterHitiQueue826 {
    @SuppressLint("StaticFieldLeak")
    private var serviceConnector: ServiceConnector? = null

    @SuppressLint("StaticFieldLeak")
    var operation: PrinterOperation? = null

    // print photo select
    private var PAPER_SIZE: Short = 2 // print photo select
    private var PAPER_TYPE: Short = 1
    private var MATTE: Short = 0
    private var PRINT_COUNT: Short = 1
    private var PRINT_MODE: Short = 0
    var m_strTablesRoot = ""
    private var m_strTablesCopyRoot = ""
    private var m_fwversion: String? = null
    private var m_fwpath = ""
    private var m_fwfolderpath = ""
    private var m_fwBootpath = ""
    private var m_fwKernelpath = ""

    private var checkNum = 0
    private var indexPrint = 0
    private var disposable: Disposable? = null
    private var printListsReal: ArrayList<PrintBean> = ArrayList()
    private var printerType = ""
    private var iPrintResultListener: IPrintResultListener? = null

    fun doPrintWithHiti(printLists: ArrayList<PrintBean>, printerType: String, iPrintResultListener: IPrintResultListener?) {
        this.iPrintResultListener = iPrintResultListener
        printListsReal = printLists
        setPrintData()
        this.printerType = printerType
    }

    fun initHiti() {
        //Create and Copy color bin file from asset folder
        m_strTablesCopyRoot = Utils.getApp().getExternalFilesDir(null)!!.absolutePath
        m_strTablesRoot = Utils.getApp().getExternalFilesDir(null)!!.absolutePath + "/Tables"
        LogUtils.e("透明打印机 tables目录："+Utils.getApp().getExternalFilesDir(null)!!.absolutePath)
        if (!FileUtility.FileExist(m_strTablesRoot)) {
            FileUtility.CreateFolder(m_strTablesRoot)
        }

        val strLogDir: String = Utils.getApp().getExternalFilesDir(null)!!.absolutePath + "/Logs"
        if (!FileUtility.FileExist(strLogDir)) {
            FileUtility.CreateFolder(strLogDir)
        }

        copyFileOrDir("Tables")
        InitialP525FW()
        ThreadUtils.runOnUiThreadDelayed({
            serviceConnector = ServiceConnector.register(Utils.getApp(), null)
            operation = PrinterOperation(Utils.getApp(), serviceConnector)
            operation!!.m_strTablesRoot = ""
            val errorCode: ErrorCode? = serviceConnector?.StartService()
            LogUtils.i("errorCode:${errorCode!!.description}")
        }, 2000)
    }

    private fun setPrintData() {
        val errorCode: ErrorCode? = serviceConnector?.StartService()
        LogUtils.i("errorCode:${errorCode!!.description}")
        val status = getStatus()
        LogUtils.i("status:$status")
        if (errorCode == ErrorCode.ERR_CODE_SUCCESS) {
            if (!status.contains("Success")) {
                EventBus.getDefault().post(EventPrintResult(status, ResponsePrinter.PRINTER_ERROR, printerType))
                iPrintResultListener?.onPrintResultListener(status, ResponsePrinter.PRINTER_ERROR, printerType)
                return
            }
            //PAPER_SIZE = 2; break;//"6x4
            //PAPER_SIZE = 4; break;//"6x8
            //PAPER_SIZE = 5; break;//"6x4 2 splits
            //PAPER_SIZE = 6; break;//"6x4 3 splits
            //PAPER_SIZE = 7; break;//6x8 2up/4x6
            //PAPER_SIZE = 8; break;//"6x8 for 6x4 2splits
            //PAPER_SIZE = 9; break;//"6x8 for 6x4 3splits
            PAPER_SIZE = if (printListsReal[indexPrint].isCut) 5 else 2
            when (printListsReal[indexPrint].cutNum) {
                0 -> {
                    PAPER_SIZE = 7
                }

                1 -> {
                    PAPER_SIZE = 8
                }

                2 -> {
                    PAPER_SIZE = 9
                }
            }
            PRINT_COUNT = printListsReal[indexPrint].printNum.toShort()
            printListsReal[indexPrint].printPhotoPath = getRightImage(printListsReal[indexPrint].printPhotoPath)
            operatePrinter(Action.USB_PRINT_PHOTOS)
        } else {
            LogUtils.i("启动服务错误")
            EventBus.getDefault().post(EventPrintResult(errorCode.toString(), ResponsePrinter.PRINTER_ERROR, printerType))
            iPrintResultListener?.onPrintResultListener(errorCode.toString(), ResponsePrinter.PRINTER_ERROR, printerType)
        }
    }

    private fun getRightImage(imagePath: String): String {
        var result = ""
        var bitmap = ImageUtils.getBitmap(imagePath)
        if (bitmap.width < bitmap.height) {
            bitmap = ImageUtils.rotate(bitmap, 90, (bitmap.width / 2).toFloat(), (bitmap.height / 2).toFloat())
        }
        bitmap = ImageUtils.compressByScale(bitmap, 1844, 1240)
        result = ImageUtils.save2Album(bitmap, Bitmap.CompressFormat.PNG)!!.absolutePath
        return result
    }

    private fun operatePrinter(action: Action) {
        ThreadUtils.executeBySingle(object : ThreadUtils.Task<String>() {
            override fun doInBackground(): String {
                var job: PrinterJob? = null
                synchronized("targetJob") {
                    when (action) {
                        Action.USB_CHECK_PRINTER_STATUS -> {
                            operation!!.m_strTablesRoot = m_strTablesRoot
                            job = operation!!.printerStatus
                        }

                        Action.USB_DEVICE_MODEL_NAME -> job = operation!!.modelName
                        Action.USB_DEVICE_SERIAL_NUM -> job = operation!!.serialNumber
                        Action.USB_DEVICE_FW_VERSION -> job = operation!!.firmwareVersion
                        Action.USB_DEVICE_RIBBON_INFO -> job = operation!!.ribbonInfo
                        Action.USB_DEVICE_PRINT_COUNT -> job = operation!!.printCount
                        Action.USB_COMMAND_RESET_PRINTER -> job = operation!!.resetPrinter()
                        Action.USB_COMMAND_RESUME_JOB -> job = operation!!.resumeJob()
                        Action.USB_COMMAND_UPDATE_FW -> {
                        }

                        Action.USB_EJECT_PAPER_JAM -> job = operation!!.ejectPaperJam()
                        Action.USB_COMMAND_CLEAN_PAPER_PATH -> job = operation!!.cleanPaperPath()
                        Action.USB_PRINT_PHOTOS -> {
                            LogUtils.e("PAPER_SIZE = $PAPER_SIZE")
                            job = operation!!.PrintPhotosStart()
                            operation!!.PRINTCOUNT = PRINT_COUNT
                            operation!!.MATTE = MATTE
                            operation!!.PRINTMODE = PRINT_MODE
                            operation!!.PAPERSIZE = PAPER_SIZE
                            operation!!.PAPERTYPE = PAPER_TYPE
                            operation!!.m_strTablesRoot = m_strTablesRoot
                            operation!!.bFinalPage = true
                            job = operation!!.print(printListsReal[indexPrint].printPhotoPath)
                        }

                        Action.USB_PRINT_PHOTOS_END -> job = operation!!.PrintPhotosEnd()
                        else -> {}
                    }
                    if (action != Action.USB_GET_OBJECT_NUMBER && action != Action.USB_GET_OBJECT_INFO && action != Action.USB_GET_OBJECT_HANDLE_ID && action != Action.USB_GET_OBJECT_DATA && action != Action.USB_COMMAND_UPDATE_FW) {
                        job?.let { showResponse(it) }
                    }
                }
                return ""
            }

            override fun onCancel() {

            }

            override fun onFail(t: Throwable?) {
                ThreadUtils.runOnUiThreadDelayed({
                    operatePrinter(Action.USB_PRINT_PHOTOS)
                }, 3000)
            }

            override fun onSuccess(result: String?) {

            }

        })
    }

    fun showResponse(job: PrinterJob) {
        val data = retrieveData(job)
        LogUtils.i("打印结果：$data")
        if (data.contains("0x0 Success")) {
            if (indexPrint == printListsReal.size - 1) {
                EventBus.getDefault().post(EventPrintResult(StringUtils.getString(R.string.print_success), ResponsePrinter.PRINTER_OK, printerType))
                iPrintResultListener?.onPrintResultListener(StringUtils.getString(R.string.print_success), ResponsePrinter.PRINTER_OK, printerType)
            } else {
                indexPrint++
                autoGetConfig()
            }
        } else {
            EventBus.getDefault().post(EventPrintResult(data, ResponsePrinter.PRINTER_ERROR, printerType))
            iPrintResultListener?.onPrintResultListener(data, ResponsePrinter.PRINTER_ERROR, printerType)
        }
    }

    fun getStatus(): String {
        operation!!.m_strTablesRoot = m_strTablesRoot
        val retData = operation!!.printerStatus.retData
        return if (retData == null) {
            "no connection"
        } else {
            val status = retData as PrinterStatus
            status.statusDescription
        }
    }

    fun getSerialNum(): String {
        if(operation==null)return "00"
        operation!!.m_strTablesRoot = m_strTablesRoot
        val retData = operation!!.serialNumber.retData
        return retData?.toString() ?: ""
    }

    fun getRibbonInfo(): Int {
        operation!!.m_strTablesRoot = m_strTablesRoot
        val retData = operation!!.ribbonInfo.retData
        return if (retData == null) {
            -1
        } else {
            val array = retData as JniData.IntArray
            array[1] * 2
        }
    }

    private fun autoGetConfig() {
        Observable.interval(2, 2, TimeUnit.SECONDS).subscribe(object : Observer<Long?> {
            override fun onSubscribe(d: Disposable) {
                disposable = d
            }

            override fun onNext(aLong: Long) {
                val data = getStatus()
                LogUtils.i("status:$data")
                if (data.contains("is printing")) {
                    return
                }
                if (data.contains("Success")) {
                    disposable!!.dispose()
                    ThreadUtils.runOnUiThreadDelayed({
                        setPrintData()
                    }, 6000)
                } else {
                    EventBus.getDefault().post(EventPrintResult(data, ResponsePrinter.PRINTER_ERROR, printerType))
                    iPrintResultListener?.onPrintResultListener(data, ResponsePrinter.PRINTER_ERROR, printerType)
                }
            }

            override fun onError(e: Throwable) {}
            override fun onComplete() {}
        })
    }


    fun retrieveData(job: PrinterJob): String {

        val bu = StringBuilder("\n\n<<<")

        /** get action name, job id */
        /** get action name, job id  */
        bu.append(job.action.name).append(" -ID").append(job.id)
            /** get error code  */
            .append(" : err <0x").append(Integer.toHexString(job.errCode.value)).append(" ")
            .append(job.errCode.description).append(">")

        if (job.retData == null) {
            return bu.toString()
        }

        /** parsing return data */
        when (job.action) {
            Action.USB_PRINT_PHOTOS_START, Action.USB_PRINT_PHOTOS, Action.USB_PRINT_PHOTOS_END, Action.USB_COMMAND_RESET_PRINTER, Action.USB_COMMAND_RESUME_JOB, Action.USB_EJECT_PAPER_JAM -> {}
            Action.USB_CHECK_PRINTER_STATUS -> {
                val status = job.retData as PrinterStatus
                bu.append("\nStatus: 0x").append(Integer.toHexString(status.statusValue)).append(" ")
                    .append(status.statusDescription)
                try {
                    val description = PrinterStatus.getDescription(status.statusValue)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            Action.USB_DEVICE_MODEL_NAME, Action.USB_DEVICE_SERIAL_NUM, Action.USB_DEVICE_FW_VERSION, Action.USB_GET_STORAGE_ID, Action.USB_GET_OBJECT_NUMBER ->
                /* get return data , string */if (job.retData is String) {
                if (job.retData != null) bu.append("\ndata: ").append(job.retData as String)
            }

            Action.USB_GET_OBJECT_INFO -> if (job.errCode == ErrorCode.ERR_CODE_SUCCESS) {
                val data = job.retData as ByteArray
                val date = ByteUtility.getDate(data)
                for (time in date) bu.append(time).append(',')
                val name = ByteUtility.getEncodingName(data)
                bu.append("\ndata: ").append(name.toString())
            }

//            Action.USB_GET_OBJECT_DATA -> {
//                val path: String = GetPhoto(job)
//                if (path != null) bu.append("\npath: ").append(path)
//            }

            Action.USB_DEVICE_RIBBON_INFO, Action.USB_GET_OBJECT_HANDLE_ID, Action.USB_DEVICE_PRINT_COUNT -> if (job.retData is JniData.IntArray) {
                var i = 0
                while (i < (job.retData as JniData.IntArray).size) {
                    bu.append("\ndata[").append(i).append("]: ")
                        .append((job.retData as JniData.IntArray)[i])
                    i++
                }
            }

            else -> {}
        }

        return bu.toString()
    }

    fun releaseService() {
        serviceConnector?.unregister()
    }

    @SuppressLint("SdCardPath")
    private fun copyFileOrDir(path: String) {
        val assetManager: AssetManager = Utils.getApp().assets
        var assets: Array<String>? = null
        try {
            assets = assetManager.list(path)
            if (assets!!.isEmpty()) {
                copyFile(path)
            } else {
                val fullPath = "/data/data/" + Utils.getApp().packageName + "/" + path
                val dir = File(fullPath)
                if (!dir.exists()) dir.mkdir()
                for (i in assets.indices) {
                    copyFileOrDir(path + "/" + assets[i])
                }
            }
        } catch (ex: IOException) {
            Log.e("tag", "I/O Exception", ex)
        }
    }

    private fun copyFile(filename: String) {
        val assetManager: AssetManager = Utils.getApp().assets
        var `in`: InputStream? = null
        var out: OutputStream? = null
        try {
            Log.e("wing", "filename: $filename")
            `in` = assetManager.open(filename)
            val newFileName: String = "$m_strTablesCopyRoot/$filename"
            out = FileOutputStream(newFileName)
            val buffer = ByteArray(1024)
            var read: Int
            while (`in`.read(buffer).also { read = it } != -1) {
                out.write(buffer, 0, read)
            }
            `in`.close()
            `in` = null
            out.flush()
            out.close()
            out = null
        } catch (e: java.lang.Exception) {
            Log.e("tag", e.message!!)
        }
    }

    private fun InitialP525FW() {
        //FW version and path
        m_fwversion = "1.02.0.m"
        m_fwfolderpath = Utils.getApp().getExternalFilesDir(null)!!.absolutePath + "/U826_FW"
        m_fwpath = "$m_fwfolderpath/rootfs.brn"
        m_fwBootpath = "$m_fwfolderpath/boot.brn"
        m_fwKernelpath = "$m_fwfolderpath/kernel.brn"
        if (!FileUtility.FileExist(m_fwfolderpath)) {
            FileUtility.CreateFolder(m_fwfolderpath)
        }
        //Copy asset fw to absolutepath
        val assetManager: AssetManager = Utils.getApp().assets
        var `in`: InputStream? = null
        var out: OutputStream? = null
        if (!FileUtility.FileExist(m_fwpath)) {
            try {
                `in` = assetManager.open("U826_FW/rootfs.brn")
                out = FileOutputStream(m_fwpath)
                val buffer = ByteArray(1024)
                var read: Int
                while (`in`.read(buffer).also { read = it } != -1) {
                    out.write(buffer, 0, read)
                }
                `in`.close()
                `in` = null
                out.flush()
                out.close()
                out = null
            } catch (e: java.lang.Exception) {
                Log.e("tag", e.message!!)
            }
        }
        if (!FileUtility.FileExist(m_fwpath)) {
            m_fwpath = ""
        }
        if (!FileUtility.FileExist(m_fwBootpath)) {
            try {
                `in` = assetManager.open("U826_FW/boot.brn")
                out = FileOutputStream(m_fwBootpath)
                val buffer = ByteArray(1024)
                var read: Int
                while (`in`.read(buffer).also { read = it } != -1) {
                    out.write(buffer, 0, read)
                }
                `in`.close()
                `in` = null
                out.flush()
                out.close()
                out = null
            } catch (e: java.lang.Exception) {
                Log.e("tag", e.message!!)
            }
        }
        if (!FileUtility.FileExist(m_fwBootpath)) {
            m_fwBootpath = ""
        }
        if (!FileUtility.FileExist(m_fwKernelpath)) {
            try {
                `in` = assetManager.open("U826_FW/kernel.brn")
                out = FileOutputStream(m_fwKernelpath)
                val buffer = ByteArray(1024)
                var read: Int
                while (`in`.read(buffer).also { read = it } != -1) {
                    out.write(buffer, 0, read)
                }
                `in`.close()
                `in` = null
                out.flush()
                out.close()
                out = null
            } catch (e: java.lang.Exception) {
                Log.e("tag", e.message!!)
            }
        }
        if (!FileUtility.FileExist(m_fwKernelpath)) {
            m_fwKernelpath = ""
        }
    }
}