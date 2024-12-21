package com.qupai.lib_printer.entity

import com.qupai.lib_printer.type.WinBoxPrintMode

/**
 * Print bean
 *
 * cutNum和isCut冲突时，采用切刀数cutNum参数
 * 所以，不需要使用切2刀方法时，还可以用isCut参数，也可以使用cutNum代替isCut方法，isCut方法预计将在之后版本更新后弃用，到时再通知
 *
 * @property printPhotoPath 图片地址
 * @property printNum 打印数量
 * @property isCut 是否切割（只有切1刀功能）
 * @property winBoxPrintMode 打印模式 WinBoxPrintMode.MODE_PANORAMIC=全景打印  WinBoxPrintMode.MODE_NORMAL= 普通打印
 * @property cutNum 切刀数（0为不切，1为切1刀为两份，2为切2刀成三份，其他参数不起作用），默认为-1，不起作用。目前只有HITI-U826适配此功能
 * @property backPrintPhotoPath 背面图片地址。目前只有 FAGAO-P510 适配此功能
 * @property printerType 打印机类型 用于多打印机同时操作情况
 * @constructor Create empty Print bean 创建打印实例
 */
class PrintBean(
    var printPhotoPath: String="",
    var printNum: Int=1,
    var isCut: Boolean=false,
    var cutNum: Int = -1,
    var winBoxPrintMode: String = WinBoxPrintMode.MODE_PANORAMIC,
    var backPrintPhotoPath:String = "",
    var printStatus:Int = 0, //0未打印 1打印中 2打印失败-故障 3打印完成
)
