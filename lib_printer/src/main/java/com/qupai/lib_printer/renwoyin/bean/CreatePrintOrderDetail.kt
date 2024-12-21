package com.qupai.lib_printer.renwoyin.bean
import com.qupai.lib_printer.renwoyin.const.FileType
import com.qupai.lib_printer.renwoyin.const.PrintColorType

class CreatePrintOrderDetail {
    var file_format_id = FileType.JPG //文件格式
    var print_mode_id = PrintColorType.COLOR //打印模式 1彩色 2灰色
    var media_type_id = 1 //媒体类型 1普通纸 2高光相纸
    var print_type_id = 110002 //打印类型枚举 110002 A4 , 110001 4R ，120001 PDF , 150002 复印
    var copy_count = 1 //打印份数
    var borderless_type_id = 1 //边框类型枚举 1无边框 2有边框
    var print_file_url = "" //打印文件 Url 必须是网络可访问的文件链接
    var print_size_id = 2 //打印尺寸 ID 1=4R   2=A4    3=A3    4=8R
    var print_page_number = "" //页码数组 打印类型为PDF时需填（可空）,使用英文逗号,隔开，如 1,2,3,4,5
    var outer_code = "" //外部编码 调用方平台的打印单明细编码，请确保外部编码对平台唯一，接口不验证是否唯一。
    var sides_type_id = 0 //页面类型 0单面 1长边双面 2短边双面
    var print_scaling_id = 4 //打印缩放，仅部分驱动生效 1原始 2缩放到合适 3伸展到合适 4调整页面大小
    var print_orientation_id = 2 //打印方向，仅部分驱动生效 1横向 2纵向 3自动判断
}