package com.qupai.lib_printer.dnp_dsrx1

import com.blankj.utilcode.util.FileUtils
import com.blankj.utilcode.util.ImageUtils
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.StringUtils.getString
import com.blankj.utilcode.util.Utils
import com.qupai.lib_printer.QuPrinter
import com.qupai.lib_printer.R
import com.qupai.lib_printer.dnp_dsrx1.PrintOptions.ECutterMode
import com.qupai.lib_printer.dnp_dsrx1.PrintOptions.EMediaSize
import com.qupai.lib_printer.dnp_dsrx1.PrintOptions.EOverCoat
import com.qupai.lib_printer.dnp_dsrx1.PrintOptions.EResolution
import com.qupai.lib_printer.dnp_dsrx1.PrintOptions.ERetryPrint
import com.qupai.lib_printer.dnp_dsrx1.common.Common
import com.qupai.lib_printer.entity.EventPrintResult
import com.qupai.lib_printer.entity.EventSinglePrintResult
import com.qupai.lib_printer.entity.PrintBean
import com.qupai.lib_printer.response.IPrintResultListener
import com.qupai.lib_printer.response.ResponsePrinter
import com.qupai.lib_printer.type.PrinterType
import com.qupai.util.BMPUtils
import com.qupai.util.GlobalThreadPools
import jp.co.dnp.photoprintlib.DNPPhotoPrint
import org.greenrobot.eventbus.EventBus
import java.io.File
import java.sql.Types

class NewPrinterDnpQueue(var printerType: String = "",var mEResolution:EResolution = EResolution.RESO300) {

    private val printData = PrintData()
    private var mEMediaSize = EMediaSize.CSP_PCx2
    private var mECutterMode = ECutterMode.MODE_2INCHCUT
    private val mERetryPrint = ERetryPrint.FREE
    private var mEOverCoat = EOverCoat.GLOSSY

    private var isStartPrintSuccess = false //有移除过数据 就表示打印成功
    private var startPrintMediaCount = 0   //开始打印相纸余量，发送单次打印任务查询记录一次,用于判断单次打印多张的时候是否全部打印完成

    init {
        //启动打印数据监听任务
        startLooperPrintTask()  //打印任务
    }

    private var runDateListenerTask = false   //监听

    /**
     * 停止监听
     */
    private fun stopCheckPrintDataTask(){
        runDateListenerTask=false
    }

    /**a
     * 启动监听
     */
    private fun startLooperPrintTask() {
        LogUtils.e("启动打印任务监听器---- $printerType  runDateListenerTask=$runDateListenerTask")
        if(runDateListenerTask)return
        runDateListenerTask = true
        LogUtils.e("启动打印任务监听器完成---- $printerType")
        GlobalThreadPools.getInstance().execute {
            while (runDateListenerTask){
                if(isDataNotEmpty()){
                    val dnpStatus = getDNPStatus()
                    if(isConnect()){//当前打印机类型是否连接
                        val firstPrintData = getFirstPrintData()
                        LogUtils.e("$printerType 打印机状态 -- $dnpStatus " +
                                "firstPrintData.printStatus=${firstPrintData.printStatus} " +
                                "firstPrintData.printNum =${firstPrintData.printNum} " +
                                "startPrintMediaCount=$startPrintMediaCount ")
                        if(dnpStatus.contains("正在打印")){
                            //不操作
                        }else if(dnpStatus.contains("空闲") ){ //空闲状态 且当前未标识打印，注意观察 防止调用打印任务后 延迟返回空闲状态
                            if(firstPrintData.printStatus==0||firstPrintData.printStatus==2){  //第一次空闲 或者是故障的数据 发送打印
                                firstPrintData.printStatus = 1
                                //调用打印
                                sendPrint(firstPrintData)
                            }else if(firstPrintData.printStatus==1){ //第2+次空闲 打印完成，这里要判断打印数量是否全部打印 根据打印前后相纸余量判断
                                //打印机空闲状态下 第一条数据状态打印中 说明已经打印完成
                                var currentCount = getMediaCount()
                                var printSuccessCount =  startPrintMediaCount - currentCount
                                LogUtils.e("$printerType currentCount=$currentCount   startPrintMediaCount=$startPrintMediaCount  printSuccessCount=$printSuccessCount ")
                                if(printSuccessCount==firstPrintData.printNum){
                                    //返回单次打印成功
                                    QuPrinter.sendSinglePrintResultMsg( getString(R.string.print_success),
                                        ResponsePrinter.PRINTER_OK,
                                        printerType,
                                        getFirstPrintData().printPhotoPath)
                                    // 状态2 打印完成 移除此条数据
                                    removeFirstData()
                                    isStartPrintSuccess=true
                                }
                            }
                        }else{
                            //打印机报错直接移除
                            QuPrinter.sendSinglePrintResultMsg("${getString(R.string.printer_connect_fail)} -- $dnpStatus",
                                ResponsePrinter.PRINTER_ERROR,
                                printerType,
                                getFirstPrintData().printPhotoPath)
                            // 状态2 打印完成 移除此条数据
                            removeFirstData()
                            LogUtils.e("打印失败： $printerType 打印机打印当前打印任务异常，移除此条打印任务 ${PrinterDnp.printerList.toString()} -- ${convertPrinterType()}")

                            //首条数据打印中 发生了故障 表示打印失败，故障排除后需要再次打印
//                            if(firstPrintData.printStatus==1){
//                                firstPrintData.printStatus = 2
//                            }
//                            stopCheckPrintDataTask()
//                            //打印机报错 发送打印机错误通知 停止监听任务 进入打印机检修模式 待打印机正常后 再重新开启监听任务
//                            printerError(dnpStatus)
                        }
                    }else{
                        LogUtils.e("打印失败： $printerType-》指定的打印机未识别，拦截发送打印任务 ${PrinterDnp.printerList.toString()} -- ${convertPrinterType()}")
                        QuPrinter.sendPrintResultMsg( getString(R.string.printer_connect_fail),
                            ResponsePrinter.PRINTER_ERROR,
                            printerType)

//                        stopCheckPrintDataTask()
//                        printerError(dnpStatus)
                    }
                }else{
                    //这边是判断当前打印队列没有数据 返回结果
                    if(isStartPrintSuccess){ //如果需求想要跳过打印时间 接收到不处理就好
                        isStartPrintSuccess=false   //发送打印结果 后恢复状态
                        LogUtils.e("${printerType}打印成功，发送打印成功通知.....")
                        QuPrinter.sendPrintResultMsg(getString(R.string.print_success),
                            ResponsePrinter.PRINTER_OK,
                            printerType)
                    }
                }
                Thread.sleep(2000)
            }
        }
    }

    /**
     *当前打印机是否连接 设备列表是否存在
     */
    private fun isConnect(): Boolean {
        for(item in PrinterDnp.printerList){
            if(item.value.displayName==convertPrinterType()){
                return true
            }
        }
        return false
    }


    private fun printerError(status:String) {
        //启动修复模式
        repairPrinter()
    }

    private var runRepairPrinterTask = false
    private fun repairPrinter() {
        LogUtils.e("启动检修状态监听 8s每次---- $printerType")
        runRepairPrinterTask=true
        GlobalThreadPools.getInstance().execute {
            while (runRepairPrinterTask){
                val dnpStatus = getDNPStatus()
                LogUtils.e("$printerType 检修打印机当前状态 -- $dnpStatus")
                PrinterDnp.updatePrinterList(printerType)
                if(dnpStatus.contains("正在打印")||dnpStatus.contains("空闲")){
                    //不操作
                    runRepairPrinterTask=false
                    LogUtils.e("$printerType 打印机检修完成 状态恢复正常 停止检修任务 1S 后重启打印任务")
                    Thread.sleep(1000)
                    startLooperPrintTask()
                }else{
                    EventBus.getDefault().post(
                        EventSinglePrintResult(
                            getString(R.string.printer_connect_fail)+" : "+dnpStatus+" : "+printerType,
                            ResponsePrinter.PRINTER_ERROR,
                            printerType,
                            getFirstPrintData().printPhotoPath
                        )
                    )
                }
                Thread.sleep(8000)
            }
        }
    }


    private fun sendPrint(firstPrintData: PrintBean) {
        startPrintMediaCount = getMediaCount()
        LogUtils.i("$printerType 打印前相纸数量：$startPrintMediaCount")
        initPrinterModelConfig(firstPrintData)    //处理打印配置
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
    }

    private fun initPrinterModelConfig(firstPrintData: PrintBean) {
        getRightOrientationImage(firstPrintData) //转换打印机可识别的图片格式
        setEMediaSize(firstPrintData)   //设置打印纸尺寸 裁剪模式
        setPrintDataField(firstPrintData)   //设置打印数据

        LogUtils.i(
            "$printerType 当前数据打印模式配置",
            "mEMediaSize = $mEMediaSize",
            "mECutterMode = $mECutterMode",
        )
    }
    private var scale = 0f
    private fun getRightOrientationImage(firstPrintData: PrintBean): String {
        var result = ""
        var bitmap = ImageUtils.getBitmap(firstPrintData.printPhotoPath)
        LogUtils.e("getRightOrientationImage bitmap.width = ${bitmap.width} bitmap.height=${bitmap.height}")
        //1.根据打印机类型设置版面的方向
        if (isDSRX1() || isDS620()) {
            //rx1和620的打印版面是横版
            if (bitmap.width < bitmap.height) {
                bitmap = ImageUtils.rotate(
                    bitmap,
                    90,
                    (bitmap.width / 2).toFloat(),
                    (bitmap.height / 2).toFloat(),
                    true)
            }
        } else {
            //410的打印版面是竖版
            if (bitmap.width > bitmap.height && bitmap.width.toFloat() / bitmap.height.toFloat() > 1.24) {
                bitmap = ImageUtils.rotate(
                    bitmap,
                    90,
                    (bitmap.width / 2).toFloat(),
                    (bitmap.height / 2).toFloat(),
                    true)
            }
        }

        //2.缩放打印机识别的分辨率 300dpi 600dpi
        bitmap = if (isQW410()) {
            //410不支持600dpi
            scale = bitmap.height.toFloat() / bitmap.width.toFloat()
            if (scale > 1.24) {
                ImageUtils.compressByScale(bitmap, 1260, 1836)
            } else {
                ImageUtils.compressByScale(bitmap, 1264, 1236)
            }
        } else if (isDS620()) {
            scale = bitmap.width.toFloat() / bitmap.height.toFloat()
            when (scale) {
                //8寸
                in 1.3..1.35 -> {
                    //需要旋转一下才能打印正常
                    if(mEResolution==EResolution.RESO600){
                        ImageUtils.rotate(
                            ImageUtils.compressByScale(bitmap, 4872, 1844,true),
                            90,
                            1844f,
                            4872f,
                            true)
                    }else{
                        ImageUtils.rotate(
                            ImageUtils.compressByScale(bitmap, 2436, 1844),
                            90,
                            1844f,
                            2436f,
                            true
                        )
                    }

                }
                //6寸
                in 1.45..1.51 -> {
                    if(mEResolution==EResolution.RESO600){
                        ImageUtils.compressByScale(bitmap, 2480, 1844)//600dpi
                    }else{
                        ImageUtils.compressByScale(bitmap, 1844, 1240)
                    }
                }
                //5*5
                in 0.9..1.1 -> {
                    if(mEResolution==EResolution.RESO600){
                        ImageUtils.compressByScale(bitmap, 1548, 3080)//600dpi
                    }else{
                        ImageUtils.compressByScale(bitmap, 1548, 1548)
                    }
                }
                //默认6寸
                else -> {
                    if(mEResolution==EResolution.RESO600){
                        ImageUtils.compressByScale(bitmap, 2480, 1844)//600dpi
                    }else{
                        ImageUtils.compressByScale(bitmap, 1844, 1240)
                    }

                }
            }
        } else {
            //rx1
            if(mEResolution==EResolution.RESO600){
                ImageUtils.compressByScale(bitmap, 1844, 2480)
            }else{
                ImageUtils.compressByScale(bitmap, 1844, 1240)
            }
        }
        result = BMPUtils.save2Bmp(bitmap)
        firstPrintData.printPhotoPath = result
        return result
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
        printData.imageRGBData1 =Common.readAndConvertImageData(listBean.printPhotoPath, Utils.getApp(), size)
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

    private fun setEMediaSize(firstPrintData: PrintBean) {
        if (isDSRX1()) {
            //RX1打印机
            mEMediaSize = if (firstPrintData.isCut) EMediaSize.CSP_PCx2 else EMediaSize.CSP_PC
            mECutterMode =
                if (firstPrintData.isCut) ECutterMode.MODE_2INCHCUT else ECutterMode.MODE_STANDARD
        } else if (isDS620()) {
            //620 打印机
            when (scale) {
                //8寸
                in 1.3..1.35 -> {
                    mEMediaSize = EMediaSize.CSP_A5
                }
                //6寸
                in 1.45..1.51 -> {
                    mEMediaSize = EMediaSize.CSP_PC_REWIND
                }
                //5*5
                in 0.9..1.1 -> {
                    mEMediaSize = EMediaSize.CSP_5x5
                }
            }
            mECutterMode =
                if (firstPrintData.isCut) ECutterMode.MODE_2INCHCUT else ECutterMode.MODE_STANDARD
        } else {
            //410 打印机
            mEMediaSize = if (scale > 1.24) {
                EMediaSize.CSP_4X6
            } else {
                EMediaSize.CSP_4X4
            }
            mECutterMode =
                if (firstPrintData.isCut) ECutterMode.MODE_2INCHCUT else ECutterMode.MODE_STANDARD
        }
    }

    private fun removeFirstData() {
        FileUtils.delete(getFirstPrintData().printPhotoPath)
        getPrinterList().removeFirst()
    }

    private fun getFirstPrintData():PrintBean {
        return getPrinterList().first()
    }

    private fun isDataNotEmpty(): Boolean {
        return getPrinterList().isNotEmpty()
    }
    private fun getPrinterList(): MutableList<PrintBean> {
        return if(isDSRX1()) {
            PrinterDnp.printDataRx1 as MutableList
        }else if(isDS620()){
            PrinterDnp.printData620 as MutableList
        }else{
            PrinterDnp.printData410 as  MutableList
        }
    }

    fun getPrintListSize(): Int {
        return getPrinterList().size
    }




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

    /**
     * 打印业务逻辑.
     * 1.设置打印数据源-队列形式
     * 2.检查打印机连接-设备列表是否存在，不存在尝试重置USB
     * 3.对图像镜像旋转缩放 处理成符合打印机的尺寸
     * 4.设置打印机的打印纸尺寸，和裁切方式
     * 5.定时查询状态 查询到空闲后执行下一张 第2步，如果打印数量完成 返回打印结果
     *
     *
     */




    /**
     * 打印机的序号
     */
    fun getPortNum(): Int {
        var checkPrinterType = convertPrinterType()
        val printerList = PrinterDnp.printerList
        val valueList: List<PrintManager.EPrinter?> = ArrayList(printerList.values)
        for (index in valueList.indices) {
            if (checkPrinterType == valueList[index]?.displayName) {
                return index
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
        if (portNum == -1) return "-1:无打印机连接"
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


    fun cancelPrint() {
        //无需做动作，
    }

    fun getDNPStatus(): String {
        val dnpInstance: DNPPhotoPrint = PrinterDnp.dnpPhotoPrint
        val keyList: List<Int?> = ArrayList(PrinterDnp.printerList.keys)
        var idx = getPortNum() // ListViewでチェックされているIndexを取得
        if (idx == -1) return "-1:status 无打印机连接"
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
        if (idx == -1) return -1
        return if (printerType == PrinterType.PRINTER_DNP_DS620) {
            PrinterDnp.dnpPhotoPrint.GetMediaCounterH(idx)  //620打印机
        } else if (printerType == PrinterType.PRINTER_DNP_DSRX1) {
            PrinterDnp.dnpPhotoPrint.GetMediaCounter(idx) - 50  //RX1 打印机
        }else{
            PrinterDnp.dnpPhotoPrint.GetMediaCounter(idx)  //410 打印机
        }
    }

//    private fun connectPrinterFail() {
//        EventBus.getDefault().post(
//            EventPrintResult(
//                getString(R.string.printer_connect_fail),
//                ResponsePrinter.PRINTER_ERROR,
//                printerType
//            )
//        )
//        iPrintResultListener?.onPrintResultListener(
//            getString(R.string.printer_connect_fail),
//            ResponsePrinter.PRINTER_ERROR,
//            printerType
//        )
//    }

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