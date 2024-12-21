//package com.wingyo.ui
//
//import android.os.Bundle
//import android.view.WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN
//import android.widget.Button
//import android.widget.EditText
//import androidx.appcompat.app.AppCompatActivity
//import com.blankj.utilcode.util.LogUtils
//import com.blankj.utilcode.util.ToastUtils
//import com.hoho.android.usbserial.IM10ResultListener
//import com.hoho.android.usbserial.MPPayment
////import com.cl.log.XLog
////import com.kongqw.serialportlibrary.Driver
////import com.kongqw.serialportlibrary.SerialUtils
////import com.kongqw.serialportlibrary.enumerate.SerialPortEnum
////import com.kongqw.serialportlibrary.enumerate.SerialStatus
////import com.kongqw.serialportlibrary.listener.SerialPortDirectorListens
//import com.wingyo.printer.R
//
//
//class IM10TestActivity : AppCompatActivity(), IM10ResultListener {
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        // 设置软键盘输入模式为隐藏
//        window.setSoftInputMode(SOFT_INPUT_STATE_HIDDEN)
//        setContentView(R.layout.activity_im10_test)
//        MPPayment.init(this, true)
//        MPPayment.connect(this, this)
//        findViewById<Button>(R.id.btLogin).setOnClickListener {
////            M10Payment.send(CMD_LOGIN)
//            MPPayment.login()
//        }
//        findViewById<Button>(R.id.btSale).setOnClickListener {
//            val sum =
//                findViewById<EditText>(R.id.editSum).text.toString().trim().toDoubleOrNull() ?: 0.1
//            MPPayment.sale(sum, MPPayment.PAYMENT_CARD)
//        }
//        findViewById<Button>(R.id.btSale2).setOnClickListener {
//            val sum =
//                findViewById<EditText>(R.id.editSum).text.toString().trim().toDoubleOrNull() ?: 0.1
//            MPPayment.sale(sum, MPPayment.PAYMENT_QRCODE)
//        }
//        findViewById<Button>(R.id.btLogoff).setOnClickListener {
//            MPPayment.logoff()
//        }
//        findViewById<Button>(R.id.btGetTotal).setOnClickListener {
//            MPPayment.getTotal()
//        }
//
//    }
//
//    override fun onIM10ResultListener(
//        status: Int,
//        cmdString: String,
//        retCode: String?,
//        retMsg: String?
//    ) {
//        LogUtils.i("status:$status  retCode:$retCode  retMsg:$retMsg")
//        ToastUtils.showLong("status:$status")
//
//    }
//}