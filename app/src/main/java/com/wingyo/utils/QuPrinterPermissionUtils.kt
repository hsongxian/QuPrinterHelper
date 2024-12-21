package com.wingyo.utils

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.blankj.utilcode.util.LogUtils

class QuPrinterPermissionUtils(val activity: AppCompatActivity) {
    private val ACTION_USB_PERMISSION = "com.wingyo.ui.USB_PERMISSION"
    private lateinit var usbManager: UsbManager
    private lateinit var permissionIntent: PendingIntent

    private var onPermissionListener: ((Boolean, MutableMap<UsbDevice, Boolean>) -> Unit)? = null

    var allUSBPermissionsAllow: Boolean = false


    private val permissionList = mutableMapOf<UsbDevice, Boolean>()

    fun register() {

        activity.apply {

            lifecycle.addObserver(object : LifecycleEventObserver {
                override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
                    if (Lifecycle.Event.ON_DESTROY == event) {
                        // 注销广播接收器
                        unregisterReceiver(usbReceiver)
                        onPermissionListener = null
                    }
                }
            })

            usbManager = getSystemService(Context.USB_SERVICE) as UsbManager
            permissionIntent = PendingIntent.getBroadcast(this, 0, Intent(ACTION_USB_PERMISSION),
                PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE)

            // 注册广播接收器以处理USB权限请求结果
            registerReceiver(usbReceiver, IntentFilter(ACTION_USB_PERMISSION))

        }
    }

    fun setOnRequestUsbPermissionListener(listener: ((Boolean, MutableMap<UsbDevice, Boolean>) -> Unit)) {
        onPermissionListener = listener
    }

    fun requestUsbPermission() {
        permissionList.clear()

        // 获取连接的所有USB设备
        val deviceList: HashMap<String, UsbDevice>? = usbManager.deviceList
        LogUtils.d("deviceList.size: ${deviceList?.size}")
        if (!deviceList.isNullOrEmpty()) {
            var allHasPermission = true
            // 遍历设备列表，选择您要操作的设备
            for ((deviceName, usbDevice) in deviceList) {
                // 在此处根据您的需求选择特定的USB设备
                if (isDesiredDevice(usbDevice)) {
                    val hasPermission = usbManager.hasPermission(usbDevice)
                    LogUtils.d("hasPermission:$hasPermission , id:${usbDevice.deviceId} , deviceName:${usbDevice.deviceName} , productId:${usbDevice.productId}, productName:${usbDevice.productName}")
                    if (hasPermission) {
                        permissionList.put(usbDevice, true)
                    } else {
                        allHasPermission = false
                        permissionList.put(usbDevice, false)
                        // 调用方法处理选定的USB设备
                        handleUsbDevice(usbDevice)
                    }
                }

            }

            if (allHasPermission) {
                onPermissionListenerCall(allHasPermission)
            }

        } else {
            onPermissionListenerCall(false)
        }
    }


    // 根据您的需求判断是否是所需的USB设备
    private fun isDesiredDevice(usbDevice: UsbDevice): Boolean {
        // 在此处添加逻辑以判断是否是您需要的USB设备
        // 例如，检查设备的厂商ID、产品ID等
//        LogUtils.d("id:${usbDevice.deviceId} , deviceName:${usbDevice.deviceName} , productId:${usbDevice.productId}, productName:${usbDevice.productName}")
        return true // 如果是所需设备，返回 true
    }

    // 处理所选的USB设备
    private fun handleUsbDevice(usbDevice: UsbDevice) {
        // 在此处添加处理选定USB设备的代码
        requestUsbPermission(usbDevice)
    }


    // USB权限请求的广播接收器
    private val usbReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val action = intent?.action
            if (ACTION_USB_PERMISSION == action) {
                synchronized(this) {
                    val usbDevice = intent.getParcelableExtra<UsbDevice>(UsbManager.EXTRA_DEVICE)
                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        if (usbDevice != null) {
                            // 用户授予了USB权限，可以在这里处理USB设备
//                            Toast.makeText(context, "USB设备已连接", Toast.LENGTH_SHORT).show()
                            LogUtils.d("USB设备已连接 id:${usbDevice.deviceId} , deviceName:${usbDevice.deviceName} , productId:${usbDevice.productId}, productName:${usbDevice.productName}")

                            // USB设备处理代码...
                            permissionList.put(usbDevice, true)

                            var allPermission = true
                            permissionList.forEach {
                                LogUtils.d("permissionList item:${it.key.deviceName} , ${it.value}")
                                if (!it.value) {
                                    allPermission = false
                                }
                            }
                            LogUtils.d("permissionList allPermission:${allPermission}")
                            if (allPermission) {
                                onPermissionListenerCall(true)
                            }
                        }
                    } else {
                        // 用户拒绝了USB权限
//                        Toast.makeText(context, "用户拒绝了USB权限", Toast.LENGTH_SHORT).show()
                        onPermissionListenerCall(false)
                        LogUtils.d("用户拒绝了USB权限 id:${usbDevice?.deviceId} , deviceName:${usbDevice?.deviceName} , productId:${usbDevice?.productId}, productName:${usbDevice?.productName}")
                    }
                }
            }
        }
    }

    private fun onPermissionListenerCall(allHasPermission: Boolean) {
        allUSBPermissionsAllow = allHasPermission
        LogUtils.d("allUSBPermissionsAllow:$allUSBPermissionsAllow")
        onPermissionListener?.invoke(allUSBPermissionsAllow, permissionList)
    }

    // 在需要访问USB设备的地方调用此方法
    private fun requestUsbPermission(usbDevice: UsbDevice) {
        LogUtils.d("id:${usbDevice.deviceId} , deviceName:${usbDevice.deviceName} , productId:${usbDevice.productId}, productName:${usbDevice.productName}")
        usbManager.requestPermission(usbDevice, permissionIntent)
    }
}