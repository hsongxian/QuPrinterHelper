package com.qupai.lib_printer.dnp_dsrx1

import com.blankj.utilcode.util.GsonUtils
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.StringUtils
import com.blankj.utilcode.util.Utils
import com.qupai.lib_printer.QuPrinter
import com.qupai.lib_printer.R
import com.qupai.lib_printer.dnp_dsrx1.PrintOptions.EResolution
import com.qupai.lib_printer.dnp_dsrx1.common.Common
import com.qupai.lib_printer.entity.PrintBean
import com.qupai.lib_printer.response.ResponsePrinter
import com.qupai.lib_printer.type.PrinterType
import com.qupai.util.UsbUtils
import jp.co.dnp.photoprintlib.DNPPhotoPrint
import java.util.Collections

object PrinterDnp {
    var dnpPhotoPrint: DNPPhotoPrint = DNPPhotoPrint(Utils.getApp())
    var printers: MutableMap<String, NewPrinterDnpQueue> = HashMap()
    var printerList: LinkedHashMap<Int, PrintManager.EPrinter> = Common.getPrinterList()

    //打印数据 线程安全 启动打印机任务后 实时监听是否存在数据，存在就打印 打印后移除
    var printDataRx1 =  Collections.synchronizedList(ArrayList<PrintBean>())
    var printData620 =  Collections.synchronizedList(ArrayList<PrintBean>())
    var printData410 =  Collections.synchronizedList(ArrayList<PrintBean>())

    fun initDnpPrint(printerType: String,mEResolution: EResolution = EResolution.RESO300) {
        if( printerList==null || printerList.size<=0){
            printerList= Common.getPrinterList()
        }
        if(printerList.size>0){
            LogUtils.e("读取到连接DNP设备列表$printerList" ,"初始化类型 $printerType")
            var index = 0
            for(item in printerList){
                val convertPrinterType = convertPrinterType(item.value.displayName)
                if(index==0)QuPrinter.changePrinterType(convertPrinterType)   //重新赋值默认值
                if(printers.contains(convertPrinterType)){
                    LogUtils.e("$convertPrinterType 打印机已经初始化过")
                }else{
                    printers[convertPrinterType] = NewPrinterDnpQueue(convertPrinterType,mEResolution)  //初始化的时候启动打印数据监听任务
                }
                index++
            }
        }else{
            LogUtils.e("DNP SDK 未读取到打印机列表 初始化队列任务失败！注意将拒绝所有打印任务，请检查打印机连接")
        }

    }

    private fun convertPrinterType(pt: String): String {
        return when (pt) {
            PrintManager.EPrinter.DS620_DEF.displayName -> {  //打印类型格式识别转换
                PrinterType.PRINTER_DNP_DS620
            }
            PrintManager.EPrinter.CY_DEF.displayName -> {
                PrinterType.PRINTER_DNP_DSRX1
            }
            PrintManager.EPrinter.QW410_DEF.displayName -> {
                PrinterType.PRINTER_DNP_QW410
            }
            else -> {
                PrinterType.PRINTER_DNP_DSRX1
            }
        }
    }

    /**
     * 刷新打印机设备列表
     * 读取设备列表方法必须等待所有打印机空闲状态才能调用，否则会造成打印机断开的异常
     * 重启usb 延迟1s后读取 dnp设备列表
     */
    fun updatePrinterList(printerType: String){
        //多打印机的情况下 其中一台打印机状态非空闲不能调用此方法 会造成sdk异常
        var  isRunningTask = false
        for(printer in printers){
            //排除当前需要重连的打印机
            if(printer.key!=printerType && !printer.value.getDNPStatus().contains("空闲")){
                isRunningTask = true
            }
        }
        if(isRunningTask){
            LogUtils.e("当前有其他打印机正在运行 拦截读取sdk打印机列表调用")
            return
        }
        UsbUtils.resetUsbOtg()
        Thread.sleep(1000)
        printerList= Common.getPrinterList()
        LogUtils.e("设备检修中。。。 读取到连接DNP设备列表 $printerList ")
    }

    fun doCheckStatus(printerType: String): String {
        var result = ""
        result = if (printerList.size == 0) {
            "No connection DNP打印机列表为空"
        } else {
            if (checkNoPrinter(printerType)) return "status empty"
            printers[printerType]!!.getDNPStatus()
        }
        return result
    }

    fun getSerialNum(printerType: String): String {
        if (checkNoPrinter(printerType)) return "0000000000"
        return printers[printerType]!!.getSerialNum()
    }

    var i = 0
    fun doPrintWithDnp(
        printLists: ArrayList<PrintBean>,
        printerType: String,
    ) {
        //DNP在任务已启动的过程中，不可以连续传递打印任务，会造成打印机系统崩溃
        if(checkNoPrinter(printerType)){
            QuPrinter.sendPrintResultMsg(
                StringUtils.getString(R.string.printer_connect_fail),
                ResponsePrinter.PRINTER_ERROR,
                printerType)
            QuPrinter.sendSinglePrintResultMsg(
                StringUtils.getString(R.string.printer_connect_fail),
                ResponsePrinter.PRINTER_ERROR,
                printerType,
                "")
            return
        }
        //插入新的打印数据
        for(item in printLists){
            when(printerType){
                PrinterType.PRINTER_DNP_DSRX1->{
                    printDataRx1.add(item)
                    LogUtils.e("添加rx1 打印数据",GsonUtils.toJson(item))
                }
                PrinterType.PRINTER_DNP_QW410->{
                    printData410.add(item)
                    LogUtils.e("添加410 打印数据",GsonUtils.toJson(item))
                }
                PrinterType.PRINTER_DNP_DS620->{
                    printData620.add(item)
                    LogUtils.e("添加620 打印数据",GsonUtils.toJson(item))
                }
            }
        }
    }

    private fun checkNoPrinter(printerType: String): Boolean {
        if (!printers.contains(printerType)) {
            LogUtils.e(
                "打印机类型 $printerType 不存在"
                , "读取到连接DNP设备列表 $printerList"
                ,"初始化的设备列表 ${printers.keys}")
            return true
        }
        return false
    }



    /**
     * Set over coat matte
     * 设置磨砂效果
     */
    fun setOverCoatMatte(printerType: String) {
        if (checkNoPrinter(printerType)) return
        printers[printerType]!!.setOverCoatMatte()
    }

    /**
     * Set over coat matte
     * 设置光面效果
     */
    fun setOverCoatGlossy(printerType: String) {
        if (checkNoPrinter(printerType)) return
        printers[printerType]!!.setOverCoatGlossy()
    }

    fun getMediaCount(printerType: String): Int {
        if (checkNoPrinter(printerType)) return 0
        return printers[printerType]!!.getMediaCount()
    }

    fun cancelAllPrint(){ //todo 这里取消所有可能会导致 双DNP打印机同时打印时候造成另外一个打印终止
        for(item in printers){
            item.value.cancelPrint()
        }
    }
    fun cancelPrint(printerType: String){
        //无需cancel操作
    }

    fun getPrintList(printerType: String): Int {
        if (checkNoPrinter(printerType)) return 0
        return return printers[printerType]!!.getPrintListSize()
    }

}