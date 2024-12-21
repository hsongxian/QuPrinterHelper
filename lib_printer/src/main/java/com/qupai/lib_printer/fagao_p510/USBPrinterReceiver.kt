package com.qupai.lib_printer.fagao_p510

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.os.Parcelable

class USBPrinterReceiver : BroadcastReceiver() {
    companion object {
        private val ACTION_USB_PERMISSION = "com.idp.androidsmarttest.USB_PERMISSION"
        private val instance: USBPrinterReceiver? = null

    }
    private var usbPerm: PendingIntent? = null

    override fun onReceive(context: Context?, intent: Intent?) {
        val action = intent!!.action
        if (UsbManager.ACTION_USB_DEVICE_ATTACHED == action) {
            val usbDevice =
                intent.getParcelableExtra<Parcelable>(UsbManager.EXTRA_DEVICE) as UsbDevice?
            RequestPermissionIfIdpPrinter(context!!, usbDevice!!)
        } else if (UsbManager.ACTION_USB_DEVICE_DETACHED == action) {
            val usbDevice =
                intent.getParcelableExtra<Parcelable>(UsbManager.EXTRA_DEVICE) as UsbDevice?
            HandleDetachIfIdpPrinter(context, usbDevice!!)
        } else if (USBPrinterReceiver.ACTION_USB_PERMISSION == action) {
            val usbDevice =
                intent.getParcelableExtra<Parcelable>(UsbManager.EXTRA_DEVICE) as UsbDevice?
            if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                usbDevice?.let { SetupIdpPrinter(context!!, it) }
            }
        }
    }


    fun RegisterIntentFilterForPermission(context: Context) {
        // https://developer.android.com/reference/android/app/PendingIntent#FLAG_MUTABLE
        //   Starting with Build.VERSION_CODES.S, it will be required to explicitly specify the mutability of PendingIntents
        //   It is strongly recommended to use FLAG_IMMUTABLE when creating a PendingIntent.
        //   [Hugh.Chang] FLAG_MUTABLE is required for EXTRA_PERMISSION_GRANTED.
        usbPerm = PendingIntent.getBroadcast(
            context, 0, Intent(USBPrinterReceiver.ACTION_USB_PERMISSION), PendingIntent.FLAG_MUTABLE
        )
        val intentFilterPermission = IntentFilter(USBPrinterReceiver.ACTION_USB_PERMISSION)
        context.registerReceiver(this, intentFilterPermission)
    }

    fun FindPrinter(context: Context) {
        val usbManager = context.getSystemService(Context.USB_SERVICE) as UsbManager
        val deviceList = usbManager.deviceList
        for (usbDevice in deviceList.values) {
            RequestPermissionIfIdpPrinter(context, usbDevice)
        }
    }

    fun RegisterIntentFilterForAttach(context: Context) {
        val intentFilterUSB = IntentFilter()
        intentFilterUSB.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED)
        intentFilterUSB.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED)
        context.registerReceiver(this, intentFilterUSB)
    }

    fun RequestPermissionIfIdpPrinter(context: Context, usbDevice: UsbDevice) {
        // 0x0016 for old models, IDP bought 0x3515 at 2023.03
        if (usbDevice.vendorId == 0x0016 || usbDevice.vendorId == 0x3515) {
            val usbManager = context.getSystemService(Context.USB_SERVICE) as UsbManager
            if (usbPerm != null) {
                usbManager.requestPermission(usbDevice, usbPerm)
            }
        }
    }

    fun SetupIdpPrinter(context: Context, usbDevice: UsbDevice) {
        val intf = usbDevice.getInterface(0)
        val usbManager = context.getSystemService(Context.USB_SERVICE) as UsbManager
        val usbDeviceConnection = usbManager.openDevice(usbDevice)
        usbDeviceConnection.claimInterface(intf, true)

        PrintFaGaoP510.LibUsbInit(context, usbDevice, usbDeviceConnection.fileDescriptor)
    }

    fun HandleDetachIfIdpPrinter(context: Context?, usbDevice: UsbDevice) {
        if (usbDevice.vendorId == 0x0016) {
            PrintFaGaoP510.LibUsbFini()
        }
    }
}