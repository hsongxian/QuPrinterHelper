package com.wingyo.ui

import android.annotation.SuppressLint
import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.widget.Toolbar.LayoutParams
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.ThreadUtils
import com.blankj.utilcode.util.ToastUtils
import com.hjq.permissions.OnPermissionCallback
import com.hjq.permissions.Permission
import com.hjq.permissions.XXPermissions
import com.luck.picture.lib.basic.PictureSelector
import com.luck.picture.lib.config.SelectMimeType
import com.luck.picture.lib.config.SelectModeConfig
import com.luck.picture.lib.entity.LocalMedia
import com.luck.picture.lib.interfaces.OnResultCallbackListener
import com.qupai.lib_base.exfun.click
import com.qupai.lib_base.utils.GlideEngine
import com.qupai.lib_printer.PrinterConfig
import com.qupai.lib_printer.QuPrinter
import com.qupai.lib_printer.dnp_dsrx1.PrintOptions.EResolution
import com.qupai.lib_printer.entity.EventPrintResult
import com.qupai.lib_printer.entity.EventSinglePrintResult
import com.qupai.lib_printer.entity.PrintBean
import com.qupai.lib_printer.response.InitResult
import com.qupai.lib_printer.type.PrinterType
import com.qupai.lib_printer.type.WinBoxPrintMode
import com.wingyo.app.BApp
import com.wingyo.printer.R
import com.wingyo.printer.databinding.ActivityMainBinding
import com.wingyo.utils.QuPrinterPermissionUtils
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class MainActivity : BaseActivity<ActivityMainBinding>() {

    private var isHaveUsbPermission = false
    private var cutNum = -1
    private var printNum = 1

    private val quPrinterPermissionUtils: QuPrinterPermissionUtils by lazy {
        QuPrinterPermissionUtils(
            this@MainActivity
        )
    }

    override fun init(savedInstanceState: Bundle?) {
        EventBus.getDefault().register(this)
        //        val parentFile = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).toString() + File.separator + Utils.getApp().packageName
//        FileCleaner.cleanFiles(parentFile,"",".bmp")

        initPrinter()
        manageExSD()
        requestUsbPermission()
        initClick()
    }

    private fun initClick() {
        bdi.selectPhoto.click {
            doPrintPhoto()
        }
        bdi.btAdd.click {
            printNum++
            bdi.tvPrintNum.text = printNum.toString()
        }
        bdi.btMinus.click {
            if (printNum <= 1) {
                return@click
            }
            printNum--
            bdi.tvPrintNum.text = printNum.toString()
        }
        bdi.btGetRemain.click {
            bdi.btGetRemain.text = "相纸余量 " +
                    "\nDNP620：${QuPrinter.getMediaCount(PrinterType.PRINTER_DNP_DS620)}" +
                    "\nDNPrx1：${QuPrinter.getMediaCount(PrinterType.PRINTER_DNP_DSRX1)}"+
                    "\nDNP410：${QuPrinter.getMediaCount(PrinterType.PRINTER_DNP_QW410)}"+
                    "\n西铁城：${QuPrinter.getMediaCount(PrinterType.PRINTER_CITIZEN_CY)}"+
                    "\n呈妍525L：${QuPrinter.getMediaCount(PrinterType.PRINTER_HITI_525L)}"+
                    "\nJoySpace-U826：${QuPrinter.getMediaCount(PrinterType.PRINTER_HITI_U826)}"
        }
        bdi.getSerialNum.click {
            bdi.getSerialNum.text = "打印机序列号 " +
                    "\nDNP620：${QuPrinter.getPrintSerial(PrinterType.PRINTER_DNP_DS620)}" +
                    "\nDNPrx1：${QuPrinter.getPrintSerial(PrinterType.PRINTER_DNP_DSRX1)}"+
                    "\nDNP410：${QuPrinter.getPrintSerial(PrinterType.PRINTER_DNP_QW410)}"+
                    "\n西铁城：${QuPrinter.getPrintSerial(PrinterType.PRINTER_CITIZEN_CY)}"+
                    "\n呈妍525L：${QuPrinter.getPrintSerial(PrinterType.PRINTER_HITI_525L)}"+
                    "\nJoySpace-U826：${QuPrinter.getPrintSerial(PrinterType.PRINTER_HITI_U826)}"
        }
        bdi.dnprx1Print.click {
            if (printLists.size == 0) return@click
            var list = arrayListOf(
                PrintBean(
                    printLists[0].printPhotoPath,
                    1, printLists[0].isCut, printLists[0].cutNum
                )
            )
            QuPrinter.doPrint(PrinterType.PRINTER_DNP_DSRX1,printLists)
        }
        bdi.dnp620Print.click {
            if (printLists.size == 0) return@click
            var list = arrayListOf(
                PrintBean(
                    printLists[0].printPhotoPath,
                    printLists[0].printNum, printLists[0].isCut, printLists[0].cutNum
                )
            )
            QuPrinter.doPrint(PrinterType.PRINTER_DNP_DS620, list)
        }
        bdi.dnp410Print.click {
            if (printLists.size == 0) return@click
            QuPrinter.doPrint(PrinterType.PRINTER_DNP_QW410, printLists)
        }
        bdi.citizenPrint.click {
            if (printLists.size == 0) return@click
            QuPrinter.doPrint(PrinterType.PRINTER_CITIZEN_CY,printLists)
        }
        bdi.joySpaceU826Print.click {
            if (printLists.size == 0) return@click
            QuPrinter.doPrint(PrinterType.PRINTER_HITI_U826, printLists)
        }
        bdi.chengYan525LPrint.click {
            if (printLists.size == 0) return@click
            QuPrinter.doPrint(PrinterType.PRINTER_HITI_525L,printLists)
        }
        bdi.faGaoPrint.click {

        }
        bdi.renWoYinPrint.click {

        }
        bdi.winBoxPrint.click {
            //        //打印参数
            var printBean = PrintBean().apply {
                printPhotoPath="/sdcard/DCIM/com.qupai.dolaphotobooth/1730795595338_100.JPG"
//            printPhotoPath="/sdcard/DCIM/com.qupai.dolaphotobooth/q.png"
                printNum=2
                isCut=true
                winBoxPrintMode= WinBoxPrintMode.MODE_NORMAL //MODE_PANORAMIC=全景照片打印  MODE_NORMAL=普通打印
            }
            QuPrinter.doPrint(PrinterType.PRINTER_WIN_BOX,arrayListOf(printBean))
        }

        bdi.rgCut.setOnCheckedChangeListener { group, checkedId ->
            when (checkedId) {
                R.id.rbCut0 -> {
                    cutNum = 0
                }

                R.id.rbCut1 -> {
                    cutNum = 1
                }

                R.id.rbCut2 -> {
                    cutNum = 2
                }
            }
        }
        bdi.rgOverCoat.setOnCheckedChangeListener { group, checkedId ->
            when (checkedId) {
                R.id.rbGlossy -> {
                    //光面
                    QuPrinter.setOverCoatGlossy()
                }

                R.id.rbMatte -> {
                    //磨砂
                    QuPrinter.setOverCoatMatte()
                }
            }
        }
    }


    private fun initPrinter() {
        //打印库初始化配置，如果是sub连接打印机 可以不传配置参数
        val config = PrinterConfig().apply {
            winBoxHost = "http://192.168.0.200:5000"     //winbox ip 选填
//            printerList = arrayListOf(PrinterType.PRINTER_DNP_DSRX1)  //手动指定打印机列表 用于 winbox 任我印 选填
            mixColorsValue = "RT5606"    //dnp颜色 选填
            dnpEResolution = EResolution.RESO300    //dnp分辨率 选填
            isUseConfigPrinter=0    //是否强制使用配置的打印机，0是读取usb返回 1是只使用printerList 配置的列表
            renwoyin_app_key = ""    //任我印key 选填
            renwoyin_app_secret = ""    //任我印secret 选填
            renwoyin_host =""    //任我印host 选填
        }
        //初始化方法 识别USB +PrinterConfig手动配置打印机类型
        QuPrinter.autoInit(BApp.appContext,config,object:InitResult{
            override fun onInitResult(initSate: Int, printerList: String) {
                //initState 1成功 0失败
                //printerList 成功返回打印机列表 失败返回空
                LogUtils.e("onInitResult：initSate= $initSate printer=$printerList")
                if(initSate==1){
                    if(printerList.contains(PrinterType.PRINTER_WIN_BOX)){
                        QuPrinter.getPrinterStatus {

                        }
                        //winBox 返回相纸余量 使用使用监听返回
                        QuPrinter.getMediaCount(PrinterType.PRINTER_WIN_BOX,object:(Int)->Unit{
                            override fun invoke(mediaCount:Int) {
                            }
                        })
                        //打印结果统一EventBus 打印
                    }else{
                        var serialNum = QuPrinter.getPrintSerial()
                        LogUtils.e("初始化读取打印机序列号：printer=$printerList  -- serialNum=$serialNum")
                    }
                }else{

                }
            }
        })
    }

    private fun requestUsbPermission() {
        quPrinterPermissionUtils.let {
            it.register()
            ThreadUtils.runOnUiThreadDelayed({
                it.requestUsbPermission()
            }, 2000)
        }
        if (quPrinterPermissionUtils.allUSBPermissionsAllow) {
            isHaveUsbPermission = true
        } else {
            quPrinterPermissionUtils.requestUsbPermission()
            //第一次无法获取usb权限，这里暂时做重启处理
            //            ThreadUtils.runOnUiThreadDelayed({ AppUtils.relaunchApp(true) }, 1000)
            quPrinterPermissionUtils.setOnRequestUsbPermissionListener { allAllow, permissionList ->
                LogUtils.d("allAllow:$allAllow  permissionList.size:${permissionList.size}")
                permissionList.forEach { (k, v) ->
                    LogUtils.d("allow:$v, deviceName:${k.deviceName}")
                    if (!allAllow) {
                        quPrinterPermissionUtils.requestUsbPermission()
                    }
                }
                if (quPrinterPermissionUtils.allUSBPermissionsAllow) {

                }
            }
        }
    }

    private fun manageExSD() {
//        requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE),100)
        XXPermissions.with(this)
            .permission(Permission.MANAGE_EXTERNAL_STORAGE)
            .request(object : OnPermissionCallback {
                override fun onGranted(permissions: MutableList<String>, allGranted: Boolean) {
                    LogUtils.i("allGranted:$allGranted")
                    LogUtils.i("permissions:$permissions")
                }

                override fun onDenied(permissions: MutableList<String>, doNotAskAgain: Boolean) {
                    LogUtils.i("doNotAskAgain:$doNotAskAgain")
                }
            })
    }


    @SuppressLint("SetTextI18n")
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(ev: EventPrintResult) {
        LogUtils.e(ev.toString())
        ToastUtils.showLong(( ""+ev.msg))
    }
    @SuppressLint("SetTextI18n")
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(ev: EventSinglePrintResult) {
        LogUtils.e(ev.toString())
        ToastUtils.showLong("单次任务返回：" +ev.msg)
    }

    /**
     * Do print photo
     * 选择本地照片，打印
     */
    val printLists = ArrayList<PrintBean>()
    private fun doPrintPhoto() {
        PictureSelector.create(this).openGallery(SelectMimeType.ofImage())
            .setImageEngine(GlideEngine.createGlideEngine())
            .setSelectionMode(SelectModeConfig.MULTIPLE)
            .forResult(object : OnResultCallbackListener<LocalMedia> {
                override fun onResult(result: ArrayList<LocalMedia>) {
                    findViewById<ImageView>(R.id.photo)
                    printLists.clear()
                    findViewById<LinearLayout>(R.id.photoLlt).removeAllViews()
                    for (localMedia in result) {
                        LogUtils.i(localMedia.realPath)
                        ImageView(this@MainActivity).also {
                            val layoutParams = LayoutParams(
                                LinearLayout.LayoutParams.WRAP_CONTENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                            )
                            layoutParams.bottomMargin = 50
                            it.layoutParams = layoutParams
                            it.adjustViewBounds = true
                            it.setImageBitmap(BitmapFactory.decodeFile(localMedia.realPath))
                            findViewById<LinearLayout>(R.id.photoLlt).addView(it)
                        }
                        printLists.add(PrintBean(localMedia.realPath, printNum, false, cutNum))
                    }
                }

                override fun onCancel() {

                }

            })
    }
//    private fun doPrintPhoto() {
//        PictureSelector.create(this).openGallery(SelectMimeType.ofImage()).setImageEngine(GlideEngine.createGlideEngine())
//            .setSelectionMode(SelectModeConfig.MULTIPLE)
//            .forResult(object : OnResultCallbackListener<LocalMedia> {
//                override fun onResult(result: ArrayList<LocalMedia>) {
//                    val printLists = ArrayList<PrintBean>()
//                    for (localMedia in result) {
//                        LogUtils.i(localMedia.realPath)
//                        printLists.add(PrintBean(localMedia.realPath, printNum, false, cutNum))
//                    }
//                    QuPrinter.doPrint(printLists, this@MainActivity, isLooper = true)
//                }
//
//                override fun onCancel() {
//
//                }
//
//            })
//    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }


}