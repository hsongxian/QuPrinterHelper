package com.qupai.lib_printer

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import com.blankj.utilcode.util.LogUtils
import com.qupai.lib_printer.type.PrinterType


object PrinterDeviceHelper {
    private var usbManager: UsbManager? = null
    private val deviceList: MutableMap<String, UsbDevice> = HashMap()
    private const val ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION"
    var list = ArrayList<String>()

    /**
     * 识别主板连接的打印机
     */
    fun checkPrinterDevices(context: Context) :ArrayList<String>{
        deviceList.clear()
        list.clear()
        if (context.packageManager.hasSystemFeature(PackageManager.FEATURE_USB_HOST)) {
            usbManager = context.getSystemService(Context.USB_SERVICE) as UsbManager

            // List currently attached devices
            for (device in usbManager!!.deviceList.values) {
                if (usbManager!!.hasPermission(device)) {
                    deviceList[device.deviceName] = device
                } else {
                    val mPermissionIntent = PendingIntent.getBroadcast(
                        context, 0, Intent(
                            ACTION_USB_PERMISSION
                        ), PendingIntent.FLAG_MUTABLE
                    )
                    usbManager!!.requestPermission(device, mPermissionIntent)
                }
            }

            val iterator: Iterator<Map.Entry<String, UsbDevice>> = deviceList.entries.iterator()
            var sb = StringBuffer()
            sb.append("识别的usb设备:\n")
            while (iterator.hasNext()) {
                val entry = iterator.next()
                val manufacturerName = entry.value.manufacturerName
                val serialNumber = entry.value.serialNumber
                val productId = entry.value.productId
//                LogUtils.e(entry.value.toString())
                if(!manufacturerName.isNullOrEmpty()){
                    sb.append("manufacturerName:$manufacturerName productId:$productId serialNumber:$serialNumber\n")
                    if(manufacturerName.contains("CITIZEN")){   //西铁城 productId = 5
                        list.add(PrinterType.PRINTER_CITIZEN_CY)
                    }else if(manufacturerName.contains("Nippon Printing")){ //DNP
                        if(!serialNumber.isNullOrEmpty()){
                            when (productId) {
                                37377 -> {    //410打印机 productId = 37377
                                    list.add(PrinterType.PRINTER_DNP_QW410)
                                }
                                35585 -> { //620打印机 productId = 35585
                                    list.add(PrinterType.PRINTER_DNP_DS620)
                                }
                                5 -> { //rx1打印机 productId = 5
                                    list.add(PrinterType.PRINTER_DNP_DSRX1)
                                }
                                else -> { //默认rx1
                                    LogUtils.e("识别到DNP打印机，但是产品ID 未定义 默认未rx1",entry.value.toString())
                                    list.add(PrinterType.PRINTER_DNP_DSRX1)
                                }
                            }
                        }else{

                            list.add(PrinterType.PRINTER_DNP_DSRX1)
                        }
                    }else if(manufacturerName.contains("JoySpace")){
                        list.add(PrinterType.PRINTER_HITI_U826)
                    }else if(manufacturerName.contains("HiTi")){
                        list.add(PrinterType.PRINTER_HITI_525L)
                    }else{
                        //不符合业务打印机类型定义
                    }
                }
            }
            LogUtils.e(sb.toString())
        } else {
            LogUtils.e("USB Host not supported on this device.")
        }
        return list
    }
}
