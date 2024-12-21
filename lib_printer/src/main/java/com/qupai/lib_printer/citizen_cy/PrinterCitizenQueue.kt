package com.qupai.lib_printer.citizen_cy

import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.StringUtils
import com.printer.sdk.PrintType
import com.printer.sdk.PrintUserOrder
import com.printer.sdk.PrintUserTask
import com.qupai.lib_printer.QuPrinter
import com.qupai.lib_printer.R
import com.qupai.lib_printer.entity.PrintBean
import com.qupai.lib_printer.response.ResponsePrinter
import com.qupai.lib_printer.type.PrinterType
import com.qupai.lib_printer.winBoxPrint.WinBoxPrinter
import com.qupai.util.GlobalThreadPools
import com.qupai.util.GsonUtils
import java.util.Collections

object PrinterCitizenQueue {

    private var printData = Collections.synchronizedList(ArrayList<PrintBean>())

    fun init(){
        startPrintTask()
    }

    private var isStartPrint = false    //是否开启打印
    private var runDateListenerTask = false   //监听
    private fun startPrintTask() {
        LogUtils.e("启动打印任务监听器---- 西铁城  runDateListenerTask=${runDateListenerTask}")
        if (runDateListenerTask) return
        runDateListenerTask = true
        LogUtils.e("启动打印任务监听器完成----西铁城")
        GlobalThreadPools.getInstance().execute {
            while (runDateListenerTask) {
                if (printData.size == 0) {  //开启过打印 发送成功
                    if (isStartPrint) {
                        isStartPrint=false
                        QuPrinter.sendPrintResultMsg(
                            "西铁城打印成功：",
                            ResponsePrinter.PRINTER_OK,
                            PrinterType.PRINTER_CITIZEN_CY
                        )
                    } else {
                        //当前无数据打印
                    }
                } else {
                    var printStatus = PrinterCitizen.getPrintManager().printStatusInt  //检查状态
                    if(printStatus==0|| printStatus==900){   //
                        //打印机空闲或者是待机模式 才可以用操作数据
                        when (getFirstPrintData().printStatus) {
                            0 -> {
                                //未打印的
                                val firstPrintData = getFirstPrintData()
                                firstPrintData.printStatus=1
                                addPrintOrder(firstPrintData)
                                isStartPrint=true //添加过打印任务
                            }
                            1 -> { //数据状态打印中不处理

                            }
                            2 -> {
                                //发生异常的任务就移除--
                                LogUtils.e("西铁城- 打印任务异常,移除此条任务",GsonUtils.serializedToJson(getFirstPrintData()))
                                removeFirstData()
                            }
                            3 -> {
                                //打印完成
                                removeFirstData()
                            }
                            else -> {

                            }
                        }
                    }else if(printStatus==1){
                        //打印中不处理
                    }else{
                        //打印机报错
                        var errMsg = "西铁城打印机状态报错 停止打印 移除数据：${getPrinterStatusMsg(printStatus)}"
                        LogUtils.e(errMsg)
                        QuPrinter.sendPrintResultMsg(errMsg, ResponsePrinter.PRINTER_ERROR, PrinterType.PRINTER_CITIZEN_CY)
                        printData.clear()
                        isStartPrint=false
                    }
                }
                Thread.sleep(2000)
            }
        }
    }

    private fun getPrinterStatusMsg(printStatus: Int): String {
        return when (printStatus) {
            0 -> "空闲待机"
            1 -> "打印中"
            500 -> "打印头冷却中"
            510 -> "纸张送纸电机冷却中"
            900 -> "待机模式"
            1000 -> "盖板打开"
            1010 -> "无废纸盒"
            1100 -> "缺纸"
            1200 -> "色带用尽"
            1300 -> "卡纸"
            1400 -> "色带错误(检测到错误,色带断裂)"
            1500 -> "纸张定义错误(设置与打印机设置不同)"
            1600 -> "数据错误(数据不当)"
            2000 -> "打印头电压错误"
            2100 -> "打印头位置错误"
            2200 -> "电源风扇停止"
            2300 -> "切刀错误(切刀卡住等)"
            2400 -> "压纸轮位置错误"
            2500 -> "打印头温度异常"
            2600 -> "介质温度异常"
            2610 -> "纸张送纸电机温度异常"
            2700 -> "色带张力错误"
            2800 -> "RFID模块错误"
            3000 -> "系统错误"
            5017 -> "双面打印单元进纸部分卡纸"
            5019 -> "双面打印单元上进纸区域卡纸"
            5023 -> "双面打印单元外壳区域卡纸"
            5027 -> "双面打印单元出纸槽区域卡纸"
            5049 -> "双面打印单元送纸电机故障"
            5065 -> "双面打印单元翻转块(外壳)内部送纸电机故障"
            5081 -> "双面打印单元压纸故障"
            5097 -> "双面打印单元传送导板故障"
            5113 -> "双面打印单元侧导板故障"
            5129 -> "双面打印单元偏斜校正故障"
            5145 -> "双面打印单元翻转块(外壳)故障"
            5161 -> "双面打印单元进纸托盘故障"
            5177 -> "双面打印单元切断操作故障"
            5193 -> "双面打印单元托盘错误"
            5209 -> "双面打印单元维护盖打开"
            5241 -> "双面打印单元系统错误"
            else -> ""
        }
    }


    private fun removeFirstData() {
        printData.removeFirst()
    }

    private fun getFirstPrintData(): PrintBean {
        return printData.first()
    }

    fun doPrintWithCitizen(printLists: ArrayList<PrintBean>) {
        PrinterCitizen.startPrintService()
        for(item in printLists){
            printData.add(item)
        }
    }

//    private fun checkPrintStatus(printLists: ArrayList<PrintBean>) {
//        var printStatus = PrinterCitizen.getPrintManager().printStatus
//        LogUtils.i("打印启动服务:getPrintStatus() = $printStatus")
//        if (checkPrinter()) {
//            addPrintOrder(printLists)
//        } else {
//            PrinterCitizen.restartPrintServer()
//            ThreadUtils.runOnUiThreadDelayed({
//                if (checkPrinter()) {
//                    addPrintOrder(printLists)
//                } else {
//                    if (StringUtils.isEmpty(printStatus)) {
//                        printStatus = StringUtils.getString(R.string.printer_connect_fail)
//                    }
//                    QuPrinter.sendPrintResultMsg(printStatus, ResponsePrinter.PRINTER_ERROR, printerType)
//                }
//            }, 6000)
//        }
//    }

    private fun addPrintOrder(printBean:PrintBean) {
        LogUtils.e("西铁城打印队列：",GsonUtils.serializedToJson(printBean))
        var printUserOrder = PrintUserOrder()
        val task: MutableList<PrintUserTask> = java.util.ArrayList()
        val printUserTask = PrintUserTask(printBean.printPhotoPath, printBean.printNum)
        printUserTask.taskid = System.currentTimeMillis()
        task.add(printUserTask)
        printUserOrder.task = task
        printUserOrder.prns = "ONE"
        printUserOrder.bdpi = "300x300"
        printUserOrder.mode = "FIT"
        if (printBean.isCut) {
            printUserOrder.cutType = "2寸裁切"
        }
        printUserOrder.orderid = System.currentTimeMillis() + 1000
        LogUtils.e("西铁城打印队列 - printUserOrder：",GsonUtils.serializedToJson(printUserOrder))
        PrinterCitizen.getPrintManager().AddOrderDefIcc(printUserOrder, { msg ->
            when (msg.ret) {
                PrintType.MSG_OK -> {
                    LogUtils.i("西铁城 添加打印任务成功,MSG_PIC: " + msg.msg)
                    startPrintOrder()
                }

                PrintType.MSG_OFT -> {
                    val msgInfo = "添加订单>>>" + "" + msg.arg1 + "/" + msg.arg2 + " src=" + msg.msg
                    LogUtils.i("西铁城 MSG_OFT:$msgInfo")
                }

                PrintType.MSG_ER -> {
                    LogUtils.i("西铁城MSG_ER:${msg.msg}")
                    LogUtils.i("西铁城MSG_ER:${msg.ret}")
                    getFirstPrintData().printStatus = 2
                    QuPrinter.sendPrintResultMsg(StringUtils.getString(R.string.printer_connect_fail), ResponsePrinter.PRINTER_ERROR, PrinterType.PRINTER_CITIZEN_CY)
                }
            }
            0
        }, 1)
    }

    private fun startPrintOrder() {
        PrinterCitizen.getPrintManager().StartPrn { printMsg ->
            when (printMsg.ret) {
                PrintType.MSG_PIC -> LogUtils.i("西铁城 每打印一张图片之前会返回图片缓存路径,MSG_PIC: " + printMsg.msg)
                PrintType.MSG_ER -> {
                    LogUtils.i("西铁城 打印失败,MSG_ER: " + printMsg.msg)
                    getFirstPrintData().printStatus = 2
                    PrinterCitizen.getPrintManager().DelOrder(printMsg.getmPrintOrder()) {
                        LogUtils.d("西铁城 打印失败 , 清除订单")
                        0
                    }
                    QuPrinter.sendPrintResultMsg(printMsg.msg, ResponsePrinter.PRINTER_ERROR, PrinterType.PRINTER_CITIZEN_CY)
                }

                PrintType.MSG_PRN -> LogUtils.i("西铁城 启动打印,MSG_PRN: " + printMsg.msg)
                PrintType.MSG_OK -> {
                    LogUtils.i("西铁城 打印完成,MSG_OK: " + printMsg.msg)
                    getFirstPrintData().printStatus = 3
//                    QuPrinter.sendPrintResultMsg(StringUtils.getString(R.string.print_success), ResponsePrinter.PRINTER_OK, PrinterType.PRINTER_CITIZEN_CY)
                }

                PrintType.MSG_OFT -> LogUtils.i("西铁城 打印订单,MSG_OFT: " + printMsg.msg)
                PrintType.MSG_START -> LogUtils.i("西铁城 MSG_START: " + printMsg.msg)
                PrintType.MSG_FINISH -> LogUtils.i("西铁城 MSG_FINISH: " + printMsg.msg)
                else -> LogUtils.i("西铁城  开始打印,Default: " + printMsg.msg)
            }
            0
        }
    }

    private fun checkPrinter(): Boolean {
        val printStatus = PrinterCitizen.getPrintManager().printStatus
        LogUtils.e("西铁城打印机状态：$printStatus")
        return ("Idling" == printStatus || "Standby Mode" == printStatus)
    }
}