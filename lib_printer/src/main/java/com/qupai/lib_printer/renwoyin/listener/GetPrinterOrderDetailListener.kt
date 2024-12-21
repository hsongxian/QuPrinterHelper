package com.qupai.lib_printer.renwoyin.listener

import com.qupai.lib_printer.renwoyin.bean.PrintOrderDetail


interface GetPrinterOrderDetailListener {
    fun onResult(result_code:Int,result_message:String,printOrderDetail: PrintOrderDetail){

    }
}