package com.qupai.lib_printer.dnp_dsrx1

import com.blankj.utilcode.util.ImageUtils
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.ObjectUtils
import com.blankj.utilcode.util.StringUtils.getString
import com.blankj.utilcode.util.ThreadUtils
import com.blankj.utilcode.util.ToastUtils
import com.blankj.utilcode.util.Utils
import com.drake.net.time.Interval
import com.qupai.lib_printer.QuPrinter
import com.qupai.lib_printer.R
import com.qupai.lib_printer.dnp_dsrx1.PrintOptions.ECutterMode
import com.qupai.lib_printer.dnp_dsrx1.PrintOptions.EMediaSize
import com.qupai.lib_printer.dnp_dsrx1.PrintOptions.EOverCoat
import com.qupai.lib_printer.dnp_dsrx1.PrintOptions.EResolution
import com.qupai.lib_printer.dnp_dsrx1.PrintOptions.ERetryPrint
import com.qupai.lib_printer.dnp_dsrx1.common.Common
import com.qupai.lib_printer.entity.EventPrintResult
import com.qupai.lib_printer.entity.PrintBean
import com.qupai.lib_printer.response.IPrintResultListener
import com.qupai.lib_printer.response.ResponsePrinter
import com.qupai.lib_printer.type.PrinterType
import com.qupai.util.BMPUtils
import com.qupai.util.UsbUtils
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.disposables.Disposable
import jp.co.dnp.photoprintlib.DNPPhotoPrint
import org.greenrobot.eventbus.EventBus
import java.io.File
import java.sql.Types
import java.util.concurrent.TimeUnit

class PrinterDnpQueue(var printerType:String="") {
    private var checkNum = 0
    private var indexPrint = 0

    private var disposable: Disposable? = null
    private val printData = PrintData()
    private var mEMediaSize = EMediaSize.CSP_PCx2
    private var mEResolution = EResolution.RESO600
    private var mECutterMode = ECutterMode.MODE_2INCHCUT
    private val mERetryPrint = ERetryPrint.FREE
    private var mEOverCoat = EOverCoat.GLOSSY

    private var printListsReal: ArrayList<PrintBean> = ArrayList()
    private var lastMediaCount = -1
//    private var printerType = ""
    private var isPrinterNormal = false
    private var isPrinting = false
    private var iPrintResultListener: IPrintResultListener? = null


    /**
     * Set over coat matte
     * 设置磨砂效果
     */
    fun setOverCoatMatte() {
        this.mEOverCoat = EOverCoat.MATTE1
    }

    /**
     * Set over coat matte
     * 设置光面效果
     */
    fun setOverCoatGlossy() {
        this.mEOverCoat = EOverCoat.GLOSSY
    }

    fun doPrintWithDnp(
        printLists: ArrayList<PrintBean>,
        printerType: String,
        iPrintResultListener: IPrintResultListener
    ) {
        this.iPrintResultListener = iPrintResultListener
        setSuitPrintList(printLists)
        LogUtils.i("$printerType doPrintWithDnp 打印张数：${printListsReal.size}")
        checkNum = 0
        indexPrint = 0
        checkPrint()
        this.printerType = printerType
    }
    private fun checkPrint() {
        lastMediaCount = getMediaCount()
        LogUtils.i("$printerType 打印前相纸数量：$lastMediaCount")
        if (checkNum == 6) {
            connectPrinterFail()
        } else {
            if (PrinterDnp.printerList.size == 0) {
                LogUtils.i("$printerType 打印机连接失败，尝试重新连接")
                checkNum++
                if (checkNum == 3) {
                    UsbUtils.resetUsbOtg()
                }
                ThreadUtils.runOnUiThreadDelayed({ checkPrint() }, 2000)
            } else {
                LogUtils.i("$printerType scale:$scale","printListsReal = ${printListsReal.size}")
                printListsReal[indexPrint].printPhotoPath =
                    getRightOrientationImage(printListsReal[indexPrint].printPhotoPath)
                if (isDSRX1()) {
                    mEMediaSize =
                        if (printListsReal[indexPrint].isCut) EMediaSize.CSP_PCx2 else EMediaSize.CSP_PC
                    mECutterMode =
                        if (printListsReal[indexPrint].isCut) ECutterMode.MODE_2INCHCUT else ECutterMode.MODE_STANDARD
                } else if (isDS620()) {
                    when (scale) {
                        //8寸
                        in 1.3..1.35 -> {
                            LogUtils.i("$printerType CSP_A5")
                            mEMediaSize = EMediaSize.CSP_A5
                        }
                        //6寸
                        in 1.45..1.51 -> {
                            LogUtils.i("$printerType CSP_PC_REWIND")
                            mEMediaSize = EMediaSize.CSP_PC_REWIND
                        }
                        //5*5
                        in 0.9..1.1 -> {
                            LogUtils.i("$printerType CSP_5x5")
                            mEMediaSize = EMediaSize.CSP_5x5
                        }
                    }
//                    mEMediaSize = if (scale > 1.4) {
//                        LogUtils.i("CSP_PC_REWIND")
//                        EMediaSize.CSP_PC_REWIND
//                    } else {
//                        LogUtils.i("CSP_A5")
//                        EMediaSize.CSP_A5
//                    }
                    mECutterMode =
                        if (printListsReal[indexPrint].isCut) ECutterMode.MODE_2INCHCUT else ECutterMode.MODE_STANDARD
                } else {
                    mEMediaSize = if (scale > 1.24) {
                        LogUtils.i("$printerType CSP_4X6")
                        EMediaSize.CSP_4X6
                    } else {
                        LogUtils.i("$printerType CSP_4X4")
                        EMediaSize.CSP_4X4
                    }
                    mECutterMode =
                        if (printListsReal[indexPrint].isCut) ECutterMode.MODE_2INCHCUT else ECutterMode.MODE_STANDARD
                }
                LogUtils.i("$printerType mediaSize:${mEMediaSize.mDisplayName}")
                LogUtils.i("$printerType cutterMode:${mECutterMode.mDisplayName}")
                doPrintWithDNP()
            }
        }
    }
    private fun doPrintWithDNP() {
        try {
            // 設定内容を PrintData のフィールドに格納
            if (!setPrintDataField(printListsReal[indexPrint])) {
                LogUtils.i("$printerType 设置打印参数错误")
                connectPrinterFail()
                return
            }
        } catch (e: NumberFormatException) {
            // 設定値が不正な場合の処理
            LogUtils.i("$printerType 设置打印参数错误")
            connectPrinterFail()
            return
        }
        printData.chkBoxPageLayout = true

        //　Debug用
        printData.displayDebug()
        var printerList = PrinterDnp.printerList
        val keyList: List<Int?> = ArrayList(printerList.keys)
        val valueList: List<PrintManager.EPrinter?> = ArrayList(printerList.values)
        LogUtils.i("$printerType keyList.size:" + keyList.size)
        LogUtils.i("$printerType valueList.size:" + valueList.size)
        var idx = getPortNum() // ListViewでチェックされているIndexを取得
        val solidId = keyList[idx] // 固体識別子を取得
        val ePrinter = valueList[idx] // プリンター情報を取得
        PrintManager.doBasicPrintAsync(Utils.getApp(), idx, printData, ePrinter, solidId!!)
        autoGetConfig()
    }




    fun doPrintWithDnpLooper(
        printLists: ArrayList<PrintBean>,
        printerType: String,
        iPrintResultListener: IPrintResultListener
    ) {
        this.iPrintResultListener = iPrintResultListener
        setSuitPrintListLooper(printLists)
        LogUtils.i("$printerType Looper 打印张数：${printListsReal.size}")
        if (ObjectUtils.isEmpty(disposable) || disposable!!.isDisposed) {
            checkNum = 0
            indexPrint = 0
            checkPrintLooper()
            this.printerType = printerType
        }
    }

    private fun checkPrintLooper() {
        isStartPrint = true
        lastMediaCount = getMediaCount()
        LogUtils.i("$printerType Looper 打印前相纸数量：$lastMediaCount")
        if (checkNum == 6) {
            isPrinterNormal = false
            connectPrinterFail()
        } else {
            if (PrinterDnp.printerList.size == 0) {
                LogUtils.i("$printerType Looper 打印机连接失败，尝试重新连接")
                checkNum++
                if (checkNum == 3) {
                    UsbUtils.resetUsbOtg()
                }
                ThreadUtils.runOnUiThreadDelayed({ checkPrintLooper() }, 2000)
            } else {
                isPrinterNormal = true
                printListsReal[0].printPhotoPath =
                    getRightOrientationImage(printListsReal[0].printPhotoPath)
                LogUtils.i("$printerType Looper scale:$scale")
                if (isDSRX1()) {
                    mEMediaSize =
                        if (printListsReal[0].isCut) EMediaSize.CSP_PCx2 else EMediaSize.CSP_PC
                    mECutterMode =
                        if (printListsReal[0].isCut) ECutterMode.MODE_2INCHCUT else ECutterMode.MODE_STANDARD
                } else if (isDS620()) {
                    when (scale) {
                        //8寸
                        in 1.3..1.35 -> {
                            LogUtils.i("$printerType Looper CSP_A5")
                            mEMediaSize = EMediaSize.CSP_A5
                        }
                        //6寸
                        in 1.45..1.51 -> {
                            LogUtils.i("$printerType Looper CSP_PC_REWIND")
                            mEMediaSize = EMediaSize.CSP_PC_REWIND
                        }
                        //5*5
                        in 0.9..1.1 -> {
                            LogUtils.i("$printerType Looper CSP_5x5")
                            mEMediaSize = EMediaSize.CSP_5x5
                        }
                    }
//                    mEMediaSize = if (scale > 1.4) {
//                        LogUtils.i("CSP_PC_REWIND")
//                        EMediaSize.CSP_PC_REWIND
//                    } else {
//                        LogUtils.i("CSP_A5")
//                        EMediaSize.CSP_A5
//                    }
                    mECutterMode =
                        if (printListsReal[0].isCut) ECutterMode.MODE_2INCHCUT else ECutterMode.MODE_STANDARD
                } else {
                    mEMediaSize = if (scale > 1.24) {
                        LogUtils.i("$printerType Looper CSP_4X6")
                        EMediaSize.CSP_4X6
                    } else {
                        LogUtils.i("$printerType Looper CSP_4X4")
                        EMediaSize.CSP_4X4
                    }
                    mECutterMode =
                        if (printListsReal[0].isCut) ECutterMode.MODE_2INCHCUT else ECutterMode.MODE_STANDARD
                }
                LogUtils.i("$printerType Looper mediaSize:${mEMediaSize.mDisplayName}")
                LogUtils.i("$printerType Looper cutterMode:${mECutterMode.mDisplayName}")
                doPrintWithDNPLooper()
            }
        }
    }

    private var isStartPrint = false
    private fun doPrintWithDNPLooper() {
        try {
            // 設定内容を PrintData のフィールドに格納
            if (!setPrintDataField(printListsReal[0])) {
                LogUtils.i("$printerType Looper 设置打印参数错误")
                printListsReal.removeFirst()
                connectPrinterFail()
                return
            }
        } catch (e: NumberFormatException) {
            // 設定値が不正な場合の処理
            LogUtils.i("$printerType Looper 设置打印参数错误")
            printListsReal.removeFirst()
            connectPrinterFail()
            return
        }
        printData.chkBoxPageLayout = true

        //　Debug用
        printData.displayDebug()
        var printerList = PrinterDnp.printerList
        val keyList: List<Int?> = ArrayList(printerList.keys)
        val valueList: List<PrintManager.EPrinter?> = ArrayList(printerList.values)
        LogUtils.i("$printerType Looper keyList.size:" + keyList.size)
        LogUtils.i("$printerType Looper valueList.size:" + valueList.size)
        var idx = getPortNum() // ListViewでチェックされているIndexを取得
        val solidId = keyList[idx] // 固体識別子を取得
        val ePrinter = valueList[idx] // プリンター情報を取得
        PrintManager.doBasicPrintAsync(Utils.getApp(), idx, printData, ePrinter, solidId!!)
        isFirstTask = true
        ThreadUtils.runOnUiThreadDelayed({
            isStartPrint = false
        }, 2000)
        if (ObjectUtils.isEmpty(disposable) || disposable!!.isDisposed) {
            autoGetConfigLooper()
        }
    }



    private lateinit var interval: Interval


    private fun startDnpPrinterLooper() {
        if (this::interval.isLateinit) {
            return
        }
        interval = Interval(2, TimeUnit.SECONDS)
        interval.subscribe {
            doPrintWithDNPLooper()
        }.start()
    }

    private var lastStatus = ""
    private fun autoGetConfigLooper() {
        Observable.interval(2, 3, TimeUnit.SECONDS).subscribe(object : Observer<Long?> {
            override fun onSubscribe(d: Disposable) {
                disposable = d
            }

            override fun onNext(aLong: Long) {
                //当开始打印的时候（避免出现断开）或者打印队列为0且最后的打印机状态为空闲，不再检测打印机状态
                if (isStartPrint || (printListsReal.size == 0 && lastStatus.contains("空闲"))) {
                    return
                }
                val dnpStatus = getDNPStatus()
                LogUtils.i(dnpStatus)
                //记录最后的状态
                lastStatus = dnpStatus
                if (dnpStatus.contains("正在打印")) {
                    isPrinting = true
                    isFirstTask = false
                } else if (dnpStatus.contains("空闲")) {
                    //如果是第一次添加任务返回空闲状态，则不处理
                    if (!isFirstTask) {
                        LogUtils.i("$printerType 打印后相纸数量：${getMediaCount()}")
                        if (isPrinting) {
                            printListsReal.removeFirst()
                            isPrinting = false
                        }
                        if (printListsReal.size > 0) {
                            ThreadUtils.runOnUiThreadDelayed({
                                checkPrintLooper()
                            }, 1500)
                        }
                    }
                } else {
                    ToastUtils.showLong(dnpStatus)
//                    disposable!!.dispose()
                    QuPrinter.sendPrintResultMsg(getString(R.string.printer_connect_fail)+dnpStatus,
                        ResponsePrinter.PRINTER_ERROR,
                        printerType)
                    iPrintResultListener?.onPrintResultListener(
                        getString(R.string.printer_connect_fail)+dnpStatus,
                        ResponsePrinter.PRINTER_ERROR,
                        printerType
                    )
                }
            }

            override fun onError(e: Throwable) {}
            override fun onComplete() {}
        })
    }

    fun getPrintList(): Int {
        return printListsReal.size
    }

    private fun setSuitPrintListLooper(printLists: ArrayList<PrintBean>) {
        for (printBean in printLists) {
            for (i in 0 until printBean.printNum) {
                printListsReal.add(PrintBean(printBean.printPhotoPath, 1, printBean.isCut))
            }
        }
    }

    private fun setSuitPrintList(printLists: ArrayList<PrintBean>) {
        printListsReal.clear()
        for (printBean in printLists) {
            for (i in 0 until printBean.printNum) {
                printListsReal.add(PrintBean(printBean.printPhotoPath, 1, printBean.isCut))
            }
        }
    }


    private var scale = 0f
    private fun getRightOrientationImage(imagePath: String): String {
        var result = ""
        var bitmap = ImageUtils.getBitmap(imagePath)
        LogUtils.e("getRightOrientationImage bitmap.width = ${bitmap.width} bitmap.height=${bitmap.height}")
        if (isDSRX1() || isDS620()) {
            //rx1和620的打印版面是横版
            if (bitmap.width < bitmap.height) {
                bitmap = ImageUtils.rotate(
                    bitmap,
                    90,
                    (bitmap.width / 2).toFloat(),
                    (bitmap.height / 2).toFloat()
                )
            }
        } else {
            //410的打印版面是竖版
            if (bitmap.width > bitmap.height && bitmap.width.toFloat() / bitmap.height.toFloat() > 1.24) {
                bitmap = ImageUtils.rotate(
                    bitmap,
                    90,
                    (bitmap.width / 2).toFloat(),
                    (bitmap.height / 2).toFloat()
                )
            }
        }
        bitmap = if (isQW410()) {
            //410不支持600dpi
            mEResolution = EResolution.RESO300
            scale = bitmap.height.toFloat() / bitmap.width.toFloat()
            if (scale > 1.24) {
                ImageUtils.compressByScale(bitmap, 1260, 1836)
            } else {
                ImageUtils.compressByScale(bitmap, 1264, 1236)
            }
        } else if (isDS620()) {
            mEResolution = EResolution.RESO300
            scale = bitmap.width.toFloat() / bitmap.height.toFloat()
            when (scale) {
                //8寸
                in 1.3..1.35 -> {
//                    ImageUtils.compressByScale(bitmap, 1844, 4872)//600dpi
                    //需要旋转一下才能打印正常
                    ImageUtils.rotate(ImageUtils.compressByScale(bitmap, 2436, 1844),90,1844f,2436f)

                }
                //6寸
                in 1.45..1.51 -> {
//                    ImageUtils.compressByScale(bitmap, 2480, 1844)//600dpi
                    ImageUtils.compressByScale(bitmap, 1844, 1240)
                }
                //5*5
                in 0.9..1.1 -> {
//                    ImageUtils.compressByScale(bitmap, 1548, 3080)//600dpi
                    ImageUtils.compressByScale(bitmap, 1548, 1548)
                }
                //默认6寸
                else -> {
//                    ImageUtils.compressByScale(bitmap, 2480, 1844)//600dpi
                    ImageUtils.compressByScale(bitmap, 1844, 1240)
                }
            }
//            if (scale > 1.4) {
//                ImageUtils.compressByScale(bitmap, 1844, 1240)
//            } else {
//                ImageUtils.compressByScale(bitmap, 2436, 1844)
//            }
        } else {
            //rx1
//            ImageUtils.compressByScale(bitmap, 1844, 1240)
            ImageUtils.compressByScale(bitmap, 1844, 2480)
        }
        //600dpi
//        bitmap = ImageUtils.rotate(bitmap, 90, (bitmap.width / 2).toFloat(), (bitmap.height / 2).toFloat())
//        bitmap = ImageUtils.compressByScale(bitmap, 1844, 2480)
//        ImageUtils.save2Album(bitmap, Bitmap.CompressFormat.JPEG)
        result = BMPUtils.save2Bmp(bitmap)
        return result
    }



    /**
     * 打印机的序号
     */
    fun getPortNum():Int{
        var checkPrinterType = convertPrinterType()
        val printerList = PrinterDnp.printerList
        val valueList: List<PrintManager.EPrinter?> = ArrayList(printerList.values)
        for (index in valueList.indices) {
            if (checkPrinterType == valueList[index]?.displayName) {
                return  index
            }
        }
        LogUtils.i("$printerType - 获取打印机序号有误 - $printerList ")
        return -1
    }

    private fun convertPrinterType(): String {
        return if (printerType == PrinterType.PRINTER_DNP_DS620) {  //打印类型格式识别转换
            PrintManager.EPrinter.DS620_DEF.displayName
        } else if (printerType == PrinterType.PRINTER_DNP_DSRX1) {
            PrintManager.EPrinter.CY_DEF.displayName
        } else if (printerType == PrinterType.PRINTER_DNP_QW410) {
            PrintManager.EPrinter.QW410_DEF.displayName
        } else {
            printerType
        }
    }

    fun getSerialNum(): String {
        val portNum = getPortNum()
        if(portNum==-1)return "-1:无打印机连接"
        val rBuf = CharArray(Common.RECEIVE_BUF_SIZE)
        val resultInt = PrinterDnp.dnpPhotoPrint.GetSerialNo(portNum, rBuf)
        LogUtils.i("$printerType resultInt:$resultInt")
        LogUtils.i("$printerType serialNumD:${charAryToString(rBuf)}")
        return charAryToString(rBuf)
    }
    /**
     * Char ary to string
     * 字符数组转字符串
     * @param chars 字符数组
     * @return 字符串
     */
    private fun charAryToString(chars: CharArray): String {
        val str: String
        val sb = StringBuilder()
        if (chars[0].code != Types.NULL) {
            /* 文字列を作成 */
//            sb.append(",")
            var i = 0
            while (chars[i].code != Types.NULL) {
                sb.append(chars[i])
                i++
            }
        }
        str = sb.toString()
        return str
    }

    /**
     * 設定内容を PrintData のフィールドに格納
     * 戻り値がfalseの場合以外に、文字列をの数値変換に失敗し NumberFormatException が throw させる場合もある
     *
     * @return true:設定に成功、false:設定に失敗
     */
    private fun setPrintDataField(listBean: PrintBean): Boolean {

        // ファイル名
        printData.imageFileName1 = File(listBean.printPhotoPath).name
        // パス
        printData.imagePath1 = listBean.printPhotoPath
        // Bitmap のサイズを取得
        val size = Common.getBitmapSize(listBean.printPhotoPath)
        // 横幅
        printData.imageWidth1 = size.width()
        // 高さ
        printData.imageHeight1 = size.height()
        // 色変換後のRGBデータを取得する
        printData.imageRGBData1 =
            Common.readAndConvertImageData(listBean.printPhotoPath, Utils.getApp(), size)
        Common.dumpLog("Image : " + printData.imageRGBData1.size)
        printData.mediaSize = mEMediaSize.mValue
        printData.resolution = mEResolution.mValue
//        /* 印刷枚数を設定 */printData.pQTY = listBean.printNum
        /* 印刷枚数を設定 */printData.pQTY = listBean.printNum
        printData.cutterMode = mECutterMode.mValue
        printData.overCoat = mEOverCoat.mValue
        printData.retryPrint = mERetryPrint.mValue
        return true
    }

    //是否第一次添加任务，如果是首次添加并返回空闲状态，则不处理
    private var isFirstTask = true

    //记录获取空闲状态次数
    private var countFree = 0

    private fun autoGetConfig() {
        isFirstTask = true
        Observable.interval(2, 4, TimeUnit.SECONDS).subscribe(object : Observer<Long?> {
            override fun onSubscribe(d: Disposable) {
                disposable = d
            }

            override fun onNext(aLong: Long) {
                val dnpStatus = getDNPStatus()
                LogUtils.i("$printerType--$dnpStatus")
                if (dnpStatus.contains("正在打印")) {
                    isFirstTask = false
                } else if (dnpStatus.contains("空闲")) {
                    //如果是第一次添加任务返回空闲状态，则不处理
                    countFree++
                    if (!isFirstTask || countFree > 10) {
                        LogUtils.i("$printerType 打印后相纸数量：${getMediaCount()}")
                        if (lastMediaCount > getMediaCount() || countFree > 10) {
                            disposable!!.dispose()
                            ThreadUtils.runOnUiThreadDelayed({
                                countFree = 0
                                if (indexPrint == printListsReal.size - 1) {
                                    LogUtils.i("$printerType 发送打印成功通知")
                                    QuPrinter.sendPrintResultMsg(  getString(R.string.print_success),
                                        ResponsePrinter.PRINTER_OK,
                                        printerType)
                                    iPrintResultListener?.onPrintResultListener(
                                        getString(R.string.print_success),
                                        ResponsePrinter.PRINTER_OK,
                                        printerType
                                    )
                                } else {
                                    indexPrint++
                                    checkPrint()
                                }
                            }, 4000)
                        }
                    }
                } else {
                    ToastUtils.showLong(dnpStatus)
                    disposable!!.dispose()
                    EventBus.getDefault().post(
                        EventPrintResult(
                            getString(R.string.printer_connect_fail),
                            ResponsePrinter.PRINTER_ERROR,
                            printerType
                        )
                    )
                    iPrintResultListener?.onPrintResultListener(
                        getString(R.string.printer_connect_fail),
                        ResponsePrinter.PRINTER_ERROR,
                        printerType
                    )
                }
            }

            override fun onError(e: Throwable) {}
            override fun onComplete() {}
        })
    }

    fun cancelPrint() {
        printListsReal.clear()
        disposable?.dispose()
        LogUtils.i("$printerType 取消询问打印状态")
    }

    fun getDNPStatus(): String {
        val dnpInstance: DNPPhotoPrint = PrinterDnp.dnpPhotoPrint
        val keyList: List<Int?> = ArrayList(PrinterDnp.printerList.keys)
        var idx = getPortNum() // ListViewでチェックされているIndexを取得
        if(idx==-1)return "-1:status 无打印机连接"
        val stat = dnpInstance.GetStatus(idx)
        try {
            if (stat and DNPPhotoPrint.GROUP_USUALLY > 0) {
                return when (stat) {
                    DNPPhotoPrint.STATUS_USUALLY_IDLE -> "空闲"
                    DNPPhotoPrint.STATUS_USUALLY_PRINTING -> "正在打印..."
                    DNPPhotoPrint.STATUS_USUALLY_PAPER_END -> "缺纸"
                    DNPPhotoPrint.STATUS_USUALLY_RIBBON_END -> "缺色带"
                    DNPPhotoPrint.STATUS_USUALLY_COOLING -> "冷却"
                    DNPPhotoPrint.STATUS_USUALLY_MOTCOOLING -> "电机冷却"
                    else -> "GROUP_USUALLY: 其它异常 $stat"
                }
            } else {
                if (stat and DNPPhotoPrint.GROUP_SETTING > 0) {
                    return when (stat) {
                        DNPPhotoPrint.STATUS_SETTING_COVER_OPEN -> "盖子打开"
                        DNPPhotoPrint.STATUS_SETTING_PAPER_JAM -> "卡纸"
                        DNPPhotoPrint.STATUS_SETTING_RIBBON_ERR -> "色带异常"
                        DNPPhotoPrint.STATUS_SETTING_PAPER_ERR -> "纸张定义错误"
                        DNPPhotoPrint.STATUS_SETTING_DATA_ERR -> "数据异常"
                        DNPPhotoPrint.STATUS_SETTING_SCRAPBOX_ERR -> "废纸箱异常"
                        else -> "GROUP_SETTING： 废纸箱异常 $stat"
                    }
                } else {
                    if (stat and DNPPhotoPrint.GROUP_HARDWARE > 0) {
                        return when (stat) {
                            DNPPhotoPrint.STATUS_HARDWARE_ERR01 -> "打印头电压异常"
                            DNPPhotoPrint.STATUS_HARDWARE_ERR02 -> "打印头位置错误"
                            DNPPhotoPrint.STATUS_HARDWARE_ERR03 -> "风扇异常"
                            DNPPhotoPrint.STATUS_HARDWARE_ERR04 -> "切刀异常"
                            DNPPhotoPrint.STATUS_HARDWARE_ERR05 -> "压紧轮异常"
                            DNPPhotoPrint.STATUS_HARDWARE_ERR06 -> "打印头温度异常"
                            DNPPhotoPrint.STATUS_HARDWARE_ERR07 -> "介质温度异常"
                            DNPPhotoPrint.STATUS_HARDWARE_ERR08 -> "色带张力异常"
                            DNPPhotoPrint.STATUS_HARDWARE_ERR09 -> "RFID模块错误"
                            DNPPhotoPrint.STATUS_HARDWARE_ERR10 -> "电机温度异常"
                            else -> "GROUP_HARDWARE: 其它异常 $stat"
                        }
                    } else {
                        if (stat and DNPPhotoPrint.GROUP_SYSTEM > 0) {
                            return "GROUP_SYSTEM: 系统错误 $stat"
                        } else {
                            if (stat < 0) {
                                return "断开 $stat"
                            }
                        }
                    }
                }
            }
        } catch (e: java.lang.Exception) {
        }
        return "空闲 $stat"
    }

    fun getMediaCount(): Int {
        var idx = getPortNum()    //打印机列表端口
        if(idx==-1)return -1
        return if (printerType != PrinterType.PRINTER_DNP_DS620) {
            PrinterDnp.dnpPhotoPrint.GetMediaCounter(idx) - 50
        } else {
            PrinterDnp.dnpPhotoPrint.GetMediaCounterH(idx)
        }
    }

    private fun connectPrinterFail() {
        QuPrinter.sendPrintResultMsg( getString(R.string.printer_connect_fail),
            ResponsePrinter.PRINTER_ERROR,
            printerType)
        iPrintResultListener?.onPrintResultListener(
            getString(R.string.printer_connect_fail),
            ResponsePrinter.PRINTER_ERROR,
            printerType
        )
    }

    fun isDSRX1(): Boolean {
        return printerType == PrinterType.PRINTER_DNP_DSRX1
    }

    fun isDS620(): Boolean {
        return printerType == PrinterType.PRINTER_DNP_DS620
    }

    fun isQW410(): Boolean {
        return printerType == PrinterType.PRINTER_DNP_QW410
    }
}