package com.qupai.lib_printer.renwoyin.listener

import com.qupai.lib_printer.renwoyin.bean.SalesNetwork


interface GetSalesNetworkListListener {
    fun onResult(result_code:Int,result_message:String,sales_network_list:List<SalesNetwork>){

    }
}