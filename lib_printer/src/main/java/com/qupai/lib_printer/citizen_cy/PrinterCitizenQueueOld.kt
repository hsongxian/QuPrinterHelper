//package com.qupai.lib_printer.citizen_cy
//
//import com.blankj.utilcode.util.LogUtils
//import com.blankj.utilcode.util.StringUtils
//import com.blankj.utilcode.util.ThreadUtils
//import com.printer.sdk.PrintType
//import com.printer.sdk.PrintUserOrder
//import com.printer.sdk.PrintUserTask
//import com.qupai.lib_base.utils.UsbUtils
//import com.qupai.lib_printer.R
//import com.qupai.lib_printer.entity.EventPrintResult
//import com.qupai.lib_printer.entity.PrintBean
//import com.qupai.lib_printer.response.ResponsePrinter
//import org.greenrobot.eventbus.EventBus
//
//object PrinterCitizenQueueOld {
//
//    private var checkNumber = 0
//    private var isAddSuccess: Boolean = false
//    private var isStartPrint: Boolean = false
//
//    fun doPrintWithCitizen(printLists: ArrayList<PrintBean>) {
//        if (isStartPrint) {
//            addPrintOrder(printLists)
//        } else {
//            checkNumber = 0
//            checkPrintStatus(printLists)
//        }
//    }
//
//    private fun checkPrintStatus(printLists: ArrayList<PrintBean>) {
//        var printStatus = PrinterCitizen.getPrintManager().printStatus
//        LogUtils.i("打印启动服务:getPrintStatus() = $printStatus")
//        if ("Idling" == printStatus || "Standby Mode" == printStatus) {
//            LogUtils.i("启动服务正常")
//            isStartPrint = true
//            ThreadUtils.runOnUiThreadDelayed({ doPrintWithCitizen(printLists) }, 2000)
//        } else {
//            LogUtils.i("启动服务错误，尝试重新启动")
//            PrinterCitizen.restartPrintServer()
//            if (checkNumber == 3) {
//                UsbUtils.resetUsbOtg()
//            }
//            if (checkNumber == 5) {
//                isStartPrint = false
//                if (StringUtils.isEmpty(printStatus)) {
//                    printStatus = StringUtils.getString(R.string.printer_connect_fail)
//                }
//                EventBus.getDefault().post(EventPrintResult(printStatus, ResponsePrinter.PRINTER_ERROR))
//                return
//            }
//            checkNumber++
//            ThreadUtils.runOnUiThreadDelayed({
//                checkPrintStatus(printLists)
//            }, 6000)
//        }
//    }
//
//    private fun addPrintOrder(printLists: ArrayList<PrintBean>) {
//        isStartPrint = false
//        isAddSuccess = false
//        val printUserOrder = PrintUserOrder()
//        val task: MutableList<PrintUserTask> = java.util.ArrayList()
//        for (printBean in printLists) {
//            val printUserTask = PrintUserTask(printBean.printPhotoPath, printBean.printNum)
//            printUserTask.taskid = System.currentTimeMillis()
//            task.add(printUserTask)
//            printUserOrder.task = task
//            printUserOrder.prns = "ONE"
//            printUserOrder.bdpi = "300x300"
//            printUserOrder.mode = "FIT"
//            if (printBean.isCut) {
//                printUserOrder.cutType = "2寸裁切"
//            }
//            printUserOrder.orderid = System.currentTimeMillis() + 1000
//        }
//        PrinterCitizen.getPrintManager().AddOrderDefIcc(printUserOrder, { msg ->
//            if (msg.ret == PrintType.MSG_OK) {
//                isAddSuccess = true
//                LogUtils.i("添加打印任务成功,MSG_PIC: " + msg.msg)
//                startPrintOrder()
//            } else {
//                ThreadUtils.runOnUiThreadDelayed({
//                    if (checkNumber < 4 && !isAddSuccess) {
//                        addPrintOrder(printLists)
//                        LogUtils.i("添加打印超时并重试")
//                    } else {
//                        EventBus.getDefault().post(EventPrintResult(msg.msg, ResponsePrinter.PRINTER_ERROR))
//                    }
//                }, printLists.size * 3000L)
//                LogUtils.i("添加打印任务失败")
//            }
//            0
//        }, 1)
//    }
//
//    private fun startPrintOrder() {
//        PrinterCitizen.getPrintManager().StartPrn { printMsg ->
//            when (printMsg.ret) {
//                PrintType.MSG_PIC -> LogUtils.i("每打印一张图片之前会返回图片缓存路径,MSG_PIC: " + printMsg.msg)
//                PrintType.MSG_ER -> {
//                    LogUtils.i("打印失败,MSG_ER: " + printMsg.msg)
//                    PrinterCitizen.getPrintManager().DelOrder(printMsg.getmPrintOrder()) {
//                        LogUtils.d("打印失败 , 清除订单")
//                        0
//                    }
//                    EventBus.getDefault().post(EventPrintResult(printMsg.msg, ResponsePrinter.PRINTER_ERROR))
//                }
//                PrintType.MSG_PRN -> LogUtils.i("启动打印,MSG_PRN: " + printMsg.msg)
//                PrintType.MSG_OK -> {
//                    LogUtils.i("打印完成,MSG_OK: " + printMsg.msg)
//                    EventBus.getDefault().post(EventPrintResult("打印成功", ResponsePrinter.PRINTER_OK))
//                }
//                PrintType.MSG_OFT -> LogUtils.i("打印订单,MSG_OFT: " + printMsg.msg)
//                PrintType.MSG_START -> LogUtils.i("MSG_START: " + printMsg.msg)
//                PrintType.MSG_FINISH -> LogUtils.i("MSG_FINISH: " + printMsg.msg)
//                else -> LogUtils.i("开始打印,Default: " + printMsg.msg)
//            }
//            0
//        }
//    }
//}