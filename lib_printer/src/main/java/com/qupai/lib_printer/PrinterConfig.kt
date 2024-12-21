package com.qupai.lib_printer

import com.qupai.lib_printer.dnp_dsrx1.PrintOptions.EResolution

data class PrinterConfig(
    var mixColorsValue: String = "RT5606",
    var dnpEResolution: EResolution = EResolution.RESO300,
    var printerList: ArrayList<String> = ArrayList(),
    var isUseConfigPrinter: Int = 0,    //强制使用配置打印机 不使用usb读取配置 0兼容usb识别 1只使用配置
    var winBoxHost: String = "",
    var renwoyin_app_key: String = "",
    var renwoyin_app_secret: String = "",
    var renwoyin_host: String ="",
)
