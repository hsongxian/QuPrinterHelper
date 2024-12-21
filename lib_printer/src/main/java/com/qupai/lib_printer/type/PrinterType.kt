package com.qupai.lib_printer.type

object PrinterType {
    const val PRINTER_TYPE = "CITIZEN-CY02"
    const val PRINTER_CITIZEN_CY = "CITIZEN-CY02"   //西铁城打印机
    const val PRINTER_DNP_DSRX1 = "DNP-DSRX1"   //DNP-RX1打印机
    const val PRINTER_DNP_DS620 = "DNP-DS620"   //DNP-620打印机
    const val PRINTER_DNP_QW410 = "DNP-QW410"   //DNP-410打印机
    const val PRINTER_HITI_525L = "HITI-525L"   //呈妍打印机
    const val PRINTER_HITI_U826 = "HITI-U826"   //透明打印机
    const val PRINTER_FAGAO_P510 = "FAGAO-P510" //法高卡片打印机

    /**
     * win盒子打印机，主要用于大照片打印的场景，通过一个win盒子连接DNP-6220打印机完成打印
     * win 盒子需要配置 局域网的ip地址，在初始化sdk的时候指定 winBoxHost
     */
    const val PRINTER_WIN_BOX = "WinBox"

    /**
     * 任我印打印机，主要用于报纸打印机产品
     * 通过任我印的盒子 + 喷墨打印机 实现打印
     * 注意：
     * 1.任我印的盒子 与 喷墨打印机需要连接同一个局域网内
     * 2。任我印鉴权问题，RenWoYinPrinter 里面已经集成 sdk app_key app_secret HOST，如果后期需要改动 可以初始化sdk的时候指定
     */
    const val PRINTER_RENWOYIN = "EP-3251"
}