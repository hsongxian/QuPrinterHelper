package com.qupai.lib_printer.response

interface IPrintResultListener {
    fun onPrintResultListener(msg: String, statusRet: Int, printerType: String)
    fun onPrintSingleResultListener(msg: String, statusRet: Int, printerType: String)
}
