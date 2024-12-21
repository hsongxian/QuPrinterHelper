package com.qupai.lib_printer.winBoxPrint

import com.blankj.utilcode.util.LogUtils
import com.drake.net.Get
import com.drake.net.Post
import com.drake.net.utils.scopeNet
import com.qupai.lib_printer.QuPrinter
import com.qupai.lib_printer.entity.PrintBean
import com.qupai.lib_printer.response.ResponsePrinter
import com.qupai.lib_printer.type.PrinterStatus
import com.qupai.lib_printer.type.PrinterType
import com.qupai.lib_printer.type.WinBoxPrintMode
import com.qupai.util.GlobalThreadPools
import com.qupai.util.GsonUtils
import java.io.File
import java.util.Collections

/**
 * win盒子打印机
 */
object WinBoxPrinter {

    private lateinit var host: String
    var printData = Collections.synchronizedList(ArrayList<PrintBean>())

    /**
     *
     */
    fun init(host: String) {
        this.host = host
        startPrintTask()
    }

    private var runDateListenerTask = false   //监听

    /**
     * 停止监听
     */
    private fun stopCheckPrintDataTask() {
        runDateListenerTask = false
    }

    private var isStartPrint = false    //是否开启打印
    private fun startPrintTask() {
        LogUtils.e("启动打印任务监听器---- win盒子  runDateListenerTask=$runDateListenerTask")
        if (runDateListenerTask) return
        runDateListenerTask = true
        LogUtils.e("启动打印任务监听器完成----win盒子")
        GlobalThreadPools.getInstance().execute {
            while (runDateListenerTask) {
                if (printData.size == 0) {  //开启过打印 发送成功
                    if (isStartPrint) {
                        isStartPrint=false
                        QuPrinter.sendPrintResultMsg(
                            "winBox打印成功：",
                            ResponsePrinter.PRINTER_OK,
                            PrinterType.PRINTER_WIN_BOX
                        )
                    } else {
                        //当前无数据打印
                    }
                } else {
                    when (getFirstPrintData().printStatus) {
                        0 -> {
                            //未打印的
                            print(getFirstPrintData().apply {
                                printStatus=1
                            })
                        }
                        1 -> {
                            //打印中
                        }
                        2 -> {
                            //发生异常的任务就移除--
                            LogUtils.e("打印任务异常,移除此条任务",GsonUtils.serializedToJson(getFirstPrintData()))
                            removeFirstData()
                        }
                        3 -> {
                            //打印完成
                            removeFirstData()
                        }
                        else -> {

                        }
                    }
                }
                Thread.sleep(2000)
            }
        }
    }

    private fun removeFirstData() {
        printData.removeFirst()
    }

    private fun getFirstPrintData(): PrintBean {
        return printData.first()
    }

    /**
     * 参数1：接口状态码 0成功 其他失败
     * 参数2：打印机状态 IDLE
     * 参数3：状态描述 空闲状态
     *
     * IDLE：空闲状态
     * PRINTING：正在打印
     * PAPER_END：纸张用尽
     * RIBBON_END：色带用尽
     * COOLING：打印头冷却中
     * MOTCOOLING：主电机冷却中
     */
    fun getPrinterStatus(onGetPrinterStatusListener: ((Int, String, String) -> Unit)?) {
        scopeNet {
            LogUtils.e("读取打印机状态")
            var getStatusResult = Get<String>("${host}/api/Image/status", this) {
            }.await()
            LogUtils.e("winBox读取打印机状态返回：$getStatusResult")
            val code = GsonUtils.getIntFromJSON(getStatusResult, "code")
            val data = GsonUtils.getStringFromJSON(getStatusResult, "data")
            val printStatus = GsonUtils.getStringFromJSON(data, "status")
            var status = when (printStatus) {
                "IDLE" -> "空闲状态"
                "PRINTING" -> "正在打印"
                "PAPER_END" -> "打印纸已用完"
                "RIBBON_END" -> "色带已用完"
                "COOLING" -> "打印头冷却中"
                "MOTCOOLING" -> "主电机冷却中"
                else -> printStatus
            }
            onGetPrinterStatusListener?.invoke(code, printStatus, status)
        }.catch {
            LogUtils.e("获取winBox打印机状态异常", it)
            onGetPrinterStatusListener?.invoke(
                -1,
                PrinterStatus.UN_KNOW_STATUS,
                "获取winBox打印机状态异常"
            )
        }
    }

    /**
     * 获取相纸余量
     * 参数1：接口状态码 0成功 其他失败
     * 参数2：相纸余量
     */
    fun getPrinterPaper(onGetPaperListener: ((Int, Int) -> Unit)?) {
        scopeNet {
            LogUtils.e("读取打印机状态")
            var getStatusResult = Get<String>("${host}/api/Image/paper", this) {
            }.await()
            LogUtils.e("winBox读取打印机相纸余量：$getStatusResult")
            val code = GsonUtils.getIntFromJSON(getStatusResult, "code")
            val data = GsonUtils.getStringFromJSON(getStatusResult, "data")
            val paperCount = GsonUtils.getIntFromJSON(data, "paperCount")
            onGetPaperListener?.invoke(code, paperCount)
        }.catch {
            LogUtils.e("获取winBox打印机相纸余量异常", it)
            onGetPaperListener?.invoke(-1, -1)
        }
    }

    /**
     * 执行打印
     * 参数1：接口状态码 0成功 其他失败
     * 参数2：打印结果状态描述
     */
    fun doPrint(printList: ArrayList<PrintBean>) {
        //由于打印机盒子的队列问题，这里需要将每张拆解打印
        for (item in printList) {
            //单张打印多分拆分成多个打印
            for (b in 0 until item.printNum) {
                printData.add(PrintBean().apply {
                    printPhotoPath = item.printPhotoPath
                    isCut = item.isCut
                    printNum = 1
                    winBoxPrintMode = item.winBoxPrintMode
                })
            }
        }
//        for(item in printList){
//            print(item)
//        }
    }

    private fun print(
        item: PrintBean,
    ) {
        isStartPrint = true
        scopeNet {
            LogUtils.e("发送大照片 winBox 打印任务 printPath:${item.printPhotoPath}")
            var result = Post<String>("${host}/api/Image/do-print", this) {
                param("Image", File(item.printPhotoPath))
                param("Copies", item.printNum)
                param("Mode", item.winBoxPrintMode)
                param("TwoInchCut", item.isCut)
                if (item.winBoxPrintMode == WinBoxPrintMode.MODE_PANORAMIC) {
                    param("Size", "6_8")
                } else {
                    param("Size", "6_4")
                }
            }.await()
            LogUtils.e("winBox打印结果：$result")
            val code = GsonUtils.getIntFromJSON(result, "code")
            val data = GsonUtils.getStringFromJSON(result, "data")
            val printStatus = GsonUtils.getStringFromJSON(data, "printStatus")
            if (code == 0) {
                getFirstPrintData().printStatus = 3
            } else {
                getFirstPrintData().printStatus = 2
            }

        }.catch {
            LogUtils.e("winBox打印请求异常", it)
            getFirstPrintData().printStatus = 2
        }
    }
//    private fun print(
//        item: PrintBean,
//    ) {
//        isStartPrint=true
//        scopeNet {
//            LogUtils.e("发送大照片 winBox 打印任务 printPath:${item.printPhotoPath}")
//            var result = Post<String>("${host}/api/Image/do-print", this) {
//                param("Image", File(item.printPhotoPath))
//                param("Copies", item.printNum)
//                param("Mode", item.winBoxPrintMode)
//                param("TwoInchCut", item.isCut)
//                if (item.winBoxPrintMode == WinBoxPrintMode.MODE_PANORAMIC) {
//                    param("Size", "6_8")
//                } else {
//                    param("Size", "6_4")
//                }
//            }.await()
//            LogUtils.e("winBox打印结果：$result")
//            val code = GsonUtils.getIntFromJSON(result, "code")
//            val data = GsonUtils.getStringFromJSON(result, "data")
//            val printStatus = GsonUtils.getStringFromJSON(data, "printStatus")
//            if(code==0){
//                QuPrinter.sendPrintResultMsg("winBox打印成功:$printStatus",ResponsePrinter.PRINTER_OK,PrinterType.PRINTER_WIN_BOX)
//            }else{
//                QuPrinter.sendPrintResultMsg("winBox打印失败$printStatus",ResponsePrinter.PRINTER_ERROR,PrinterType.PRINTER_WIN_BOX)
//            }
//
//        }.catch {
//            LogUtils.e("winBox打印请求异常", it)
//            QuPrinter.sendPrintResultMsg("winBox打印失败，发生异常",ResponsePrinter.PRINTER_ERROR,PrinterType.PRINTER_WIN_BOX)
//        }
//    }
}