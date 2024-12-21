package com.qupai.lib_printer

import RenWoYinPrinter
import android.app.Application
import com.blankj.utilcode.util.LogUtils
import com.qupai.lib_printer.citizen_cy.PrinterCitizen
import com.qupai.lib_printer.dnp_dsrx1.PrintOptions.EResolution
import com.qupai.lib_printer.dnp_dsrx1.PrinterDnp
import com.qupai.lib_printer.entity.EventPrintResult
import com.qupai.lib_printer.entity.EventSinglePrintResult
import com.qupai.lib_printer.entity.PrintBean
import com.qupai.lib_printer.fagao_p510.PrintFaGaoP510
import com.qupai.lib_printer.hiti_u826.PrinterHitiQueue826
import com.qupai.lib_printer.renwoyin.listener.InitTokenListener
import com.qupai.lib_printer.response.InitResult
import com.qupai.lib_printer.response.ResponsePrinter
import com.qupai.lib_printer.type.DNPMixColorsUtil
import com.qupai.lib_printer.type.PrinterStatus
import com.qupai.lib_printer.type.PrinterType
import com.qupai.lib_printer.winBoxPrint.WinBoxPrinter
import org.greenrobot.eventbus.EventBus

object QuPrinter {
    private var checkPrinterDevices: java.util.ArrayList<String> = ArrayList()
    private var mixColorsValue: String = "RT5606"
    private var dnpEResolution: EResolution = EResolution.RESO300
    var printerType: String = PrinterType.PRINTER_DNP_DSRX1
    var initState = 0   //初始化结果1成功 0失败

    fun changePrinterType(printerType:String){
        LogUtils.e("改变默认打印机类型：$printerType")
        this.printerType = printerType
    }

    fun autoInit(application: Application,initResult: InitResult) {
        autoInit(application,PrinterConfig(),initResult)
    }

    /**
     * 同时初始化多台打印机
     */
    fun autoInit(application: Application,printerConfig:PrinterConfig,initResult: InitResult) {
        LogUtils.e("初始化打印机库 Version："+ BuildConfig.VERSION,"dnpMixColorsValue = ${printerConfig.mixColorsValue}",
            "dnpEResolution=${printerConfig.dnpEResolution}")
        this.mixColorsValue = printerConfig.mixColorsValue
        this.dnpEResolution = printerConfig.dnpEResolution
        checkPrinterDevices.clear()
        if(printerConfig.isUseConfigPrinter==0){
            //允许usb识别
            checkPrinterDevices.addAll(PrinterDeviceHelper.checkPrinterDevices(application))
            LogUtils.e("主板识别的打印机列表:$checkPrinterDevices --- 手动指定的打印机列表:${printerConfig.printerList}")
        }else{
            LogUtils.e("只使用手动指定的打印机列表:${printerConfig.printerList}")
        }
        checkPrinterDevices.addAll(printerConfig.printerList)   //添加手动传递的

        checkPrinterDevices.distinct() //去重
        if(checkPrinterDevices.size==0){
            LogUtils.e("主板未识别到打印机，且未手动指定打印机类型，初始化失败")
            initState=0
            initResult.onInitResult(0, "")
            return
        }
        printerType = checkPrinterDevices[0]   //只记录第一个 只有一个的时候才可以判断当前的打印机类型
        LogUtils.e("设置默认打印机类型：$printerType --$checkPrinterDevices")
        var isUseDNP = false
        for(printerType in checkPrinterDevices){
            when (printerType) {
                PrinterType.PRINTER_CITIZEN_CY -> {
                    PrinterCitizen.getPrintManager()
                    PrinterCitizen.initCitizen()
                }

                PrinterType.PRINTER_DNP_DSRX1 -> {
                    isUseDNP=true
                }

                PrinterType.PRINTER_DNP_DS620 -> {
                    isUseDNP=true
                }

                PrinterType.PRINTER_DNP_QW410 -> {
                    isUseDNP=true
                }

                PrinterType.PRINTER_HITI_525L -> {
                    PrinterHitiQueue826.initHiti()
                }

                PrinterType.PRINTER_HITI_U826 -> {
                    PrinterHitiQueue826.initHiti()
                }

                PrinterType.PRINTER_FAGAO_P510 -> {
                    PrintFaGaoP510.initPrint()
                }
                PrinterType.PRINTER_WIN_BOX -> {
                    WinBoxPrinter.init(printerConfig.winBoxHost)
                }
                PrinterType.PRINTER_RENWOYIN -> {    //报纸打印机 任我印厂商
                    RenWoYinPrinter.init(printerConfig.renwoyin_app_key,printerConfig.renwoyin_app_secret,printerConfig.renwoyin_host,object :InitTokenListener{
                        override fun onResult(isSuccess: Boolean) {

                        }
                    })
                }
            }
        }

        //由于连接多DNP的时候 USB识别可能会不准确，这里使用DNP的sdk读取的打印机列表来做最终的初始化
        if(isUseDNP){
            //DNP单独
            PrinterDnp.initDnpPrint(printerType, dnpEResolution)
        }

        initState=1
        initResult.onInitResult(1, checkPrinterDevices.toString())

    }



    fun doPrint(
        printLists: ArrayList<PrintBean>
    ){
        //不指定打印机 默认使用第一个
        doPrint(printerType,printLists)
    }
    fun doPrint(
        printerType: String=QuPrinter.printerType,
        printLists: ArrayList<PrintBean>
    ) {
        if(checkPrinterDevices.size==0){
            LogUtils.e("无打印机连接拦截打印")
            sendPrintResultMsg("打印失败,无打印机连接，请插入打印机并且重启程序",ResponsePrinter.PRINTER_ERROR,printerType)
            return
        }
        if(!checkPrinterDevices.contains(printerType)){
            LogUtils.e("传入打印机类型【$printerType】未找到，拦截打印")
            sendPrintResultMsg("打印失败,传入打印机类型未找到",ResponsePrinter.PRINTER_ERROR,printerType)
            return
        }
//        GlobalThreadPools.getInstance().execute {
        LogUtils.e("调用子线程执行打印任务- printerType=$printerType")
        when (printerType) {
            PrinterType.PRINTER_CITIZEN_CY -> {
                PrinterCitizen.doPrint(printLists, printerType)
            }

            PrinterType.PRINTER_DNP_DSRX1, PrinterType.PRINTER_DNP_DS620, PrinterType.PRINTER_DNP_QW410 -> {
                PrinterDnp.doPrintWithDnp(printLists, printerType)
            }

            PrinterType.PRINTER_HITI_525L -> {
                PrinterHitiQueue826.doPrintWithHiti(printLists, printerType,null)
            }

            PrinterType.PRINTER_HITI_U826 -> {
                PrinterHitiQueue826.doPrintWithHiti(printLists, printerType,null)
            }

            PrinterType.PRINTER_FAGAO_P510 -> {
                PrintFaGaoP510.doPrint(printLists, printerType)
            }
            PrinterType.PRINTER_RENWOYIN -> {
//                    RenWoYinPrinter.createPrinterOrder(prin)
            }
            PrinterType.PRINTER_WIN_BOX -> {
                WinBoxPrinter.doPrint(printLists)
            }
        }
//        }
    }

    fun isConnect(printerType: String):Boolean{
        return checkPrinterDevices.contains(printerType)
    }

    /**
     * Set over coat matte
     * 设置磨砂效果
     */
    fun setOverCoatMatte(printerType: String=QuPrinter.printerType) {
        LogUtils.e("设置磨砂效果- printerType=$printerType")
        PrinterDnp.setOverCoatMatte(printerType)
    }

    /**
     * Set over coat matte
     * 设置光面效果
     */
    fun setOverCoatGlossy(printerType: String=QuPrinter.printerType) {
        LogUtils.e("设置光面效果- printerType=$printerType")
        PrinterDnp.setOverCoatGlossy(printerType)
    }

    /**
     * Get print list size
     *
     * @return 打印队列数量
     */
    fun getPrintListSize(printerType: String=QuPrinter.printerType): Int {
        LogUtils.e("获取打印队列数量- printerType=$printerType")
        return PrinterDnp.getPrintList(printerType)
    }




    /**
     * Get media count
     *
     * @return 相纸余量
     */
    fun getMediaCount(printerType: String=QuPrinter.printerType,onGetMediaCountListener: ((Int) -> Unit)?=null): Int {
        if(checkPrinterDevices.size==0){
            LogUtils.e("获取相纸余量- printerType=$printerType 无打印机连接,获取相纸余量失败 返回-1")
            return -1
        }
        if(!checkPrinterDevices.contains(printerType)){
            LogUtils.e("获取相纸余量- printerType=$printerType 传入打印机类型【$printerType】未找到，获取相纸余量失败 返回-1")
            return -1
        }
        var mediaCountNumber: Int = -1
        when (printerType) {
            PrinterType.PRINTER_CITIZEN_CY -> {
                PrinterCitizen.initCitizen()
                mediaCountNumber = PrinterCitizen.getPrintManager().printRemainQuantityInt
                onGetMediaCountListener?.invoke(mediaCountNumber)
                printMediaCount(mediaCountNumber)
            }

            PrinterType.PRINTER_DNP_DSRX1, PrinterType.PRINTER_DNP_DS620, PrinterType.PRINTER_DNP_QW410 -> {
                mediaCountNumber = PrinterDnp.getMediaCount(printerType)
                onGetMediaCountListener?.invoke(mediaCountNumber)
                printMediaCount(mediaCountNumber)
            }

            PrinterType.PRINTER_HITI_525L -> {
                mediaCountNumber = PrinterHitiQueue826.getRibbonInfo()
                onGetMediaCountListener?.invoke(mediaCountNumber)
                printMediaCount(mediaCountNumber)
            }

            PrinterType.PRINTER_HITI_U826 -> {
                mediaCountNumber = PrinterHitiQueue826.getRibbonInfo()
                onGetMediaCountListener?.invoke(mediaCountNumber)
                printMediaCount(mediaCountNumber)
            }

            PrinterType.PRINTER_FAGAO_P510 -> {
                mediaCountNumber = PrintFaGaoP510.getRibbonSize()
                onGetMediaCountListener?.invoke(mediaCountNumber)
                printMediaCount(mediaCountNumber)
            }
            PrinterType.PRINTER_RENWOYIN -> {

            }

            PrinterType.PRINTER_WIN_BOX -> {
                WinBoxPrinter.getPrinterPaper(object:(Int,Int)->Unit{
                    override fun invoke(p1: Int, p2: Int) {
                        if(p1==0){
                            onGetMediaCountListener?.invoke(p2)
                            printMediaCount(p1)
                        }else{
                            onGetMediaCountListener?.invoke(-1)
                            printMediaCount(-1)
                        }
                    }
                })
            }
        }
        return mediaCountNumber
    }

    private fun printMediaCount(mediaCountNumber:Int){
        LogUtils.e("获取相纸余量: printerType=$printerType mediaCountNumber=$mediaCountNumber")
    }

    /**
     * 获取打印机状态
     */
    fun getPrinterStatus(printerType: String=QuPrinter.printerType,getStatusListener: ((String) -> Unit)?=null):String{
        if(checkPrinterDevices.size==0){
            LogUtils.e("获取打印机状态 printerType=$printerType 无打印机连接,获取状态失败")
            return PrinterStatus.UN_KNOW_STATUS
        }
        if(!checkPrinterDevices.contains(printerType)){
            LogUtils.e("获取打印机状态- printerType=$printerType 传入打印机类型【$printerType】未找到，获取状态失败")
            return PrinterStatus.UN_KNOW_STATUS
        }
        var status = PrinterStatus.UN_KNOW_STATUS
        when (printerType) {
            PrinterType.PRINTER_CITIZEN_CY -> {
            }

            PrinterType.PRINTER_DNP_DSRX1, PrinterType.PRINTER_DNP_DS620, PrinterType.PRINTER_DNP_QW410 -> {
            }

            PrinterType.PRINTER_HITI_525L -> {
            }

            PrinterType.PRINTER_HITI_U826 -> {
            }

            PrinterType.PRINTER_FAGAO_P510 -> {
            }

            PrinterType.PRINTER_RENWOYIN -> {

            }
            PrinterType.PRINTER_WIN_BOX -> {
                WinBoxPrinter.getPrinterStatus(object:(Int,String,String)->Unit{
                    override fun invoke(p1: Int, p2: String, p3: String) {
                        getStatusListener?.invoke(p2)
                    }
                })
            }
        }
        LogUtils.e("获取打印机状态- printerType=$printerType status=$status")
        return status
    }


    /**
     * Cancel print
     * 取消打印
     * todo 取消任务应该根据队列是否完成 来取消任务，无需上层应用者手动调用取消
     */
    fun cancelPrint(printerType: String=QuPrinter.printerType) {
        LogUtils.e("取消打印- printerType=${printerType}")
        kotlin.runCatching {
            when (printerType) {
                PrinterType.PRINTER_CITIZEN_CY -> {
                    PrinterCitizen.stopPrintService()
                }

                PrinterType.PRINTER_DNP_DSRX1, PrinterType.PRINTER_DNP_DS620, PrinterType.PRINTER_DNP_QW410 -> {
                    PrinterDnp.cancelPrint(printerType)
                }

                PrinterType.PRINTER_HITI_525L -> {
                    PrinterHitiQueue826.releaseService()
                }

                PrinterType.PRINTER_HITI_U826 -> {
                    PrinterHitiQueue826.releaseService()
                }

                PrinterType.PRINTER_FAGAO_P510 -> {
                    PrintFaGaoP510.releasePrint()
                }
            }
        }.onFailure {
            LogUtils.i(it)
        }
    }


    /**
     * Get print serial
     *
     * @return 打印机序列号
     */
    fun getPrintSerial(printerType: String = QuPrinter.printerType): String {
        if(!checkPrinterDevices.contains(printerType)){
            LogUtils.e("获取打印机序列号失败- printerType=${printerType} 打印机未连接或者未初始化")
            return "0"
        }
        var result = ""
        when (printerType) {
            PrinterType.PRINTER_CITIZEN_CY -> {
                result = if (PrinterCitizen.getPrintManager().printSerialNO.length > 12) PrinterCitizen.getPrintManager().printSerialNO.substring(
                    0,
                    12
                ) else PrinterCitizen.getPrintManager().printSerialNO
            }

            PrinterType.PRINTER_DNP_DSRX1, PrinterType.PRINTER_DNP_DS620, PrinterType.PRINTER_DNP_QW410 -> {
                result = PrinterDnp.getSerialNum(printerType)
            }

            PrinterType.PRINTER_HITI_525L -> {
                PrinterHitiQueue826.initHiti()
                result = PrinterHitiQueue826.getSerialNum()
            }

            PrinterType.PRINTER_HITI_U826 -> {
                PrinterHitiQueue826.initHiti()
                result = PrinterHitiQueue826.getSerialNum()
            }

            PrinterType.PRINTER_FAGAO_P510 -> {
                PrintFaGaoP510.initPrint()
                result = PrintFaGaoP510.getSerialNum()
            }
        }
        LogUtils.e("获取打印机序列号完成- printerType=${printerType} serialNum=$result")
        return result
    }

    /**
     * Init dnp mix colors
     * 初始化调色
     *
     * @param mixColors
     */
    fun initDnpMixColors(mixColors: String) {
        mixColorsValue = mixColors
    }

    fun initDnpEResolution(mEResolution: EResolution) {
        dnpEResolution = mEResolution
    }
    fun getDnpEResolution():EResolution {
        return dnpEResolution
    }

    fun getDnpMixColorsRaw(): Int {
        if(DNPMixColorsUtil.dnpColors.containsKey(mixColorsValue)){
            return DNPMixColorsUtil.dnpColors[mixColorsValue]!!
        }
        LogUtils.e("DNP 颜色【$mixColorsValue】 不存在！！ 使用默认 RT5606")
        return DNPMixColorsUtil.dnpColors["RT5606"]!!
    }

    fun getDnpMixColors(): String {
        if(DNPMixColorsUtil.dnpColors.containsKey(mixColorsValue)){
            return mixColorsValue
        }
        LogUtils.e("DNP 颜色【$mixColorsValue】 不存在！！ 使用默认 RT5606")
        return "RT5606"
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

    fun isCitizen(): Boolean {
        return printerType == PrinterType.PRINTER_CITIZEN_CY
    }

    fun isHITI_525L(): Boolean {
        return printerType == PrinterType.PRINTER_HITI_525L
    }

    fun isHITI_U826(): Boolean {
        return printerType == PrinterType.PRINTER_HITI_U826
    }

    fun isFAGAO_P510(): Boolean {
        return printerType == PrinterType.PRINTER_FAGAO_P510
    }

    fun sendPrintResultMsg( msg: String,  statusRet: Int,  printerType: String){
        LogUtils.e("发送打印结果：msg=$msg statusRet-0成功-1失败 status=$statusRet printerType=$printerType")
        EventBus.getDefault().post(
            EventPrintResult(
                msg,
                statusRet,
                printerType
            )
        )
    }

    fun sendSinglePrintResultMsg( msg: String,  statusRet: Int,  printerType: String,printPath: String){
        LogUtils.e("发送单次打印结果通知：msg=$msg statusRet-0成功-1失败 status=$statusRet printerType=$printerType printPath=$printPath")
        EventBus.getDefault().post(
            EventSinglePrintResult(
                msg,
                statusRet,
                printerType,
                printPath
            )
        )
    }

}