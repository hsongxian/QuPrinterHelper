package com.qupai.lib_printer.renwoyin.bean
import com.qupai.lib_printer.renwoyin.const.PrintChannel

class CreateOrderParam {
    var print_channel_id= PrintChannel.OepnApi
    var terminal_code= ""
    var is_send_print_job= true
    var print_order_detail_list:ArrayList<CreatePrintOrderDetail> = ArrayList()
}