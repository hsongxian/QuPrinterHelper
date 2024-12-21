package com.qupai.lib_printer.citizen_cy

import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.StringUtils
import com.blankj.utilcode.util.ThreadUtils
import com.printer.sdk.PrintType
import com.printer.sdk.PrintUserOrder
import com.printer.sdk.PrintUserTask
import com.qupai.lib_printer.QuPrinter
import com.qupai.lib_printer.R
import com.qupai.lib_printer.entity.EventPrintResult
import com.qupai.lib_printer.entity.PrintBean
import com.qupai.lib_printer.response.IPrintResultListener
import com.qupai.lib_printer.response.ResponsePrinter
import org.greenrobot.eventbus.EventBus

object PrinterCitizenQueueOld2 {
    private var printerType = ""
    fun doPrintWithCitizen(printLists: ArrayList<PrintBean>, printerType: String) {
        PrinterCitizen.startPrintService()
        checkPrintStatus(printLists)
        this.printerType = printerType
    }

    private fun checkPrintStatus(printLists: ArrayList<PrintBean>) {
        var printStatus = PrinterCitizen.getPrintManager().printStatus
        LogUtils.i("打印启动服务:getPrintStatus() = $printStatus")
        if (checkPrinter()) {
            addPrintOrder(printLists)
        } else {
            PrinterCitizen.restartPrintServer()
            ThreadUtils.runOnUiThreadDelayed({
                if (checkPrinter()) {
                    addPrintOrder(printLists)
                } else {
                    if (StringUtils.isEmpty(printStatus)) {
                        printStatus = StringUtils.getString(R.string.printer_connect_fail)
                    }
                    QuPrinter.sendPrintResultMsg(printStatus, ResponsePrinter.PRINTER_ERROR, printerType)
                }
            }, 6000)
        }
    }

    private fun addPrintOrder(printLists: ArrayList<PrintBean>) {
        val printUserOrder = PrintUserOrder()
        val task: MutableList<PrintUserTask> = java.util.ArrayList()
        for (printBean in printLists) {
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
        }
        PrinterCitizen.getPrintManager().AddOrderDefIcc(printUserOrder, { msg ->
            when (msg.ret) {
                PrintType.MSG_OK -> {
                    LogUtils.i("添加打印任务成功,MSG_PIC: " + msg.msg)
                    startPrintOrder()
                }

                PrintType.MSG_OFT -> {
                    val msgInfo = "添加订单>>>" + "" + msg.arg1 + "/" + msg.arg2 + " src=" + msg.msg
                    LogUtils.i("MSG_OFT:$msgInfo")
                }

                PrintType.MSG_ER -> {
                    LogUtils.i("MSG_ER:${msg.msg}")
                    LogUtils.i("MSG_ER:${msg.ret}")
                    QuPrinter.sendPrintResultMsg(StringUtils.getString(R.string.printer_connect_fail), ResponsePrinter.PRINTER_ERROR, printerType)
                    PrinterCitizen.stopPrintService()
                }
            }
            0
        }, 1)
    }

    private fun startPrintOrder() {
        PrinterCitizen.getPrintManager().StartPrn { printMsg ->
            when (printMsg.ret) {
                PrintType.MSG_PIC -> LogUtils.i("每打印一张图片之前会返回图片缓存路径,MSG_PIC: " + printMsg.msg)
                PrintType.MSG_ER -> {
                    LogUtils.i("西铁城 打印失败,MSG_ER: " + printMsg.msg)
                    PrinterCitizen.getPrintManager().DelOrder(printMsg.getmPrintOrder()) {
                        LogUtils.d("西铁城 打印失败 , 清除订单")
                        0
                    }
                    QuPrinter.sendPrintResultMsg(printMsg.msg, ResponsePrinter.PRINTER_ERROR, printerType)
                    PrinterCitizen.stopPrintService()
                }

                PrintType.MSG_PRN -> LogUtils.i("启动打印,MSG_PRN: " + printMsg.msg)
                PrintType.MSG_OK -> {
                    LogUtils.i("西铁城 打印完成,MSG_OK: " + printMsg.msg)
                    QuPrinter.sendPrintResultMsg(StringUtils.getString(R.string.print_success), ResponsePrinter.PRINTER_OK, printerType)
                    PrinterCitizen.stopPrintService()
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