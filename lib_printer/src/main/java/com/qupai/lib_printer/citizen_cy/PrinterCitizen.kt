package com.qupai.lib_printer.citizen_cy

import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.ThreadUtils
import com.blankj.utilcode.util.Utils
import com.printer.sdk.PrintManage
import com.printer.sdk.PrintMsg
import com.printer.sdk.PrintType
import com.qupai.lib_printer.R
import com.qupai.lib_printer.entity.PrintBean

object PrinterCitizen {
    fun getPrintManager(): PrintManage {
        return PrintManage.instance(Utils.getApp())
    }

    fun doCheckCiziten(): String {
        var result = ""
        val printStatus = getPrintManager().printStatus
        result = if ("Idling" == printStatus) {
            Utils.getApp().getString(R.string.ok)
        } else {
            Utils.getApp().getString(R.string.error, printStatus)
        }
        return result
    }


    fun initCitizen() {
        loadIccFile()
        startPrintService()
        PrinterCitizenQueue.init()
    }

    fun doPrint( printLists: ArrayList<PrintBean>,printType: String){
        PrinterCitizenQueue.doPrintWithCitizen(printLists)
    }

    private fun loadIccFile() {
        getPrintManager().LoadIccFile { 0 }
        ThreadUtils.runOnUiThreadDelayed({ getPrintManager().SetPrinterDebug(false) }, 2000)
        ThreadUtils.runOnUiThreadDelayed({ getPrintManager().SetPrinterMedia("6x4") }, 4000)
    }

    fun startPrintService() {
        getPrintManager().StartSvr { msg: PrintMsg ->
            when (msg.ret) {
                PrintType.MSG_ER -> LogUtils.i(
                    """
                       启动打印服务Error: ${msg.msg}
                       ${msg.ret}
                       """.trimIndent()
                )

                PrintType.MSG_OK -> LogUtils.w(
                    """
                       启动打印服务Ok: ${msg.msg}
                       ${msg.ret}
                       """.trimIndent()
                )

                else -> LogUtils.i(
                    """
                       启动打印服务default: ${msg.msg}
                       ${msg.ret}
                       """.trimIndent()
                )
            }
            0
        }
    }

    fun stopPrintService() {
        getPrintManager().StopPrn { info: PrintMsg ->
            when (info.ret) {
                PrintType.MSG_OK -> LogUtils.i(
                    """
                        打印停止: ${info.msg}
                        ${info.ret}
                        """.trimIndent()
                )

                else -> LogUtils.i(
                    """
                        打印停止default: ${info.msg}
                        ${info.ret}
                        """.trimIndent()
                )
            }
            0
        }
        ThreadUtils.runOnUiThreadDelayed({
            runCatching {
                getPrintManager().DelOrder(null) { info: PrintMsg ->
                    when (info.ret) {
                        PrintType.MSG_OK -> LogUtils.i(
                            """
                            清空打印队列: ${info.msg}
                            ${info.ret}
                            """.trimIndent()
                        )

                        else -> LogUtils.i(
                            """
                            清空打印队列default: ${info.msg}
                            ${info.ret}
                            """.trimIndent()
                        )
                    }
                    0
                }
            }.onFailure {
                it.printStackTrace()
            }
        }, 2000)
        ThreadUtils.runOnUiThreadDelayed({
            getPrintManager().StopSvr { info: PrintMsg ->
                when (info.ret) {
                    PrintType.MSG_OK -> LogUtils.i(
                        """
                            停止打印服务: ${info.msg}
                            ${info.ret}
                            """.trimIndent()
                    )

                    else -> LogUtils.i(
                        """
                            停止打印服务default: ${info.msg}
                            ${info.ret}
                            """.trimIndent()
                    )
                }
                0
            }
        }, 4000)
    }

    fun restartPrintServer() {
        stopPrintService()
        ThreadUtils.runOnUiThreadDelayed({
            startPrintService()
        }, 4000)
    }
}