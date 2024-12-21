package com.qupai.lib_printer.response

interface InitResult {
    /**
     * initState:1成功 0失败
     */
    fun onInitResult(initSate:Int,printerList: String)
}
