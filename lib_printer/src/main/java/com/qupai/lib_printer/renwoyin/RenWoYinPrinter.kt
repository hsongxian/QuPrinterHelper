
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.SPUtils
import com.blankj.utilcode.util.ThreadUtils
import com.blankj.utilcode.util.ToastUtils
import com.drake.net.Post
import com.drake.net.utils.scopeNet
import com.google.gson.reflect.TypeToken
import com.qupai.lib_printer.renwoyin.bean.CreateOrderParam
import com.qupai.lib_printer.renwoyin.bean.CreatePrintOrderDetail
import com.qupai.lib_printer.renwoyin.bean.SalesNetwork
import com.qupai.lib_printer.renwoyin.listener.CreatePrinterOrderListener
import com.qupai.lib_printer.renwoyin.listener.GetSalesNetworkListListener
import com.qupai.lib_printer.renwoyin.listener.InitTokenListener
import com.qupai.util.GsonUtils
import com.qupai.util.MD5Util
import java.util.UUID

/**
 * 网络共享打印机
 * app_key：29f4ac6095f3fd58
 * app_secret：bb744a5e54057c201d5675b998bc5e57
 * 测试地址：http://sandbox.open.renwoyin.cn
 */
object RenWoYinPrinter {

    private var ACCESS_TOKEN: String? = ""
    val dev_app_key = "29f4ac6095f3fd58"
    val dev_app_secret = "bb744a5e54057c201d5675b998bc5e57"
    val dev_HOST = "http://sandbox.open.renwoyin.cn"

    var app_key = "65bed833938fb477"
    var app_secret = "5578eac4979545e1d67753284bede461"
    var HOST = "http://open.renwoyin.cn"

    val ACCESS_TOKEN_SP ="ACCESS_TOKEN_SP"
    val ACCESS_TOKEN_LAST_GET_TIME_SP ="ACCESS_TOKEN_LAST_GET_TIME_SP"

    /**
     * 初始化 接口参数
     * 1。
     */
    fun init(listener: InitTokenListener){
        init(app_key, app_secret, HOST,listener)
    }
    fun init(app_key:String, app_secret:String,host:String,listener: InitTokenListener){
        if(!app_key.isNullOrEmpty())this.app_key = app_key
        if(!app_secret.isNullOrEmpty())this.app_secret = app_secret
        if(!host.isNullOrEmpty())this.HOST = host
        //读缓存存在 使用缓存
        ACCESS_TOKEN = SPUtils.getInstance().getString(ACCESS_TOKEN_SP)
        var time = SPUtils.getInstance().getLong(ACCESS_TOKEN_LAST_GET_TIME_SP)
        var vTime = System.currentTimeMillis()/1000-time
        LogUtils.e("token已缓存时间：${7200-vTime}")
        var isNeedUpdateToken = false
        if(7200-vTime < 200){
            isNeedUpdateToken=true
            LogUtils.e("token已缓存时间有效期低于200s 立即更新token")
        }
        if(ACCESS_TOKEN.isNullOrEmpty()||isNeedUpdateToken){
            getAccessToken(listener)
        }else{
            LogUtils.e("使用缓存Token：$ACCESS_TOKEN")
            listener.onResult(true)
        }
    }

    /**
     * /open_app/access_token
     */
    private fun getAccessToken(listener: InitTokenListener){
        runCatching {
            //获取新的token
            scopeNet {
                val result = Post<String>(HOST + "/open_app/access_token"){
                    param("app_id", app_key)
                    val nonceStr = getNonceStr()
                    param("nonce_str", nonceStr)
                    val timestamp = System.currentTimeMillis()/1000
                    param("timestamp", timestamp.toInt())
                    param("sign", getSign(nonceStr,timestamp.toInt()))
                }.await()
                LogUtils.e("result",result)
                val result_code = GsonUtils.getIntFromJSON(result, "result_code")
                val result_message = GsonUtils.getStringFromJSON(result, "result_message")
                if (result_code == 0) {
                    val access_token = GsonUtils.getStringFromJSON(result, "access_token")
                    SPUtils.getInstance().put(ACCESS_TOKEN_SP,access_token)
                    LogUtils.e("获取Token成功：${access_token}")
                    listener.onResult(true)
                    SPUtils.getInstance().put(ACCESS_TOKEN_LAST_GET_TIME_SP,System.currentTimeMillis()/1000)
                    ACCESS_TOKEN = access_token
                } else {
                    LogUtils.e("获取Token失败：${result_message}")
                    listener.onResult(false)
                }
            }
        }.onFailure {
            LogUtils.e("获取Token异常：",it)
            listener.onResult(false)
        }

    }

    private fun getNonceStr(): String {
        return UUID.randomUUID().toString()+(System.currentTimeMillis()/1000)
    }

    private fun getSign(nonceStr:String,timestamp:Int):String{
        val stringA = "app_id=$app_key&app_secret=$app_secret&nonce_str=$nonceStr&timestamp=$timestamp"
        return MD5Util.getMD5(stringA)
    }

    /**获取网络打印机列表
     * /partner/sales_network/list
     * */
    fun getPrinterList(listener: GetSalesNetworkListListener){
        runCatching {
            scopeNet {
                val result = Post<String>(HOST + "/partner/sales_network/list?access_token=$ACCESS_TOKEN"){
                    param("longitude",0)
                    param("latitude", 0)
                    param("page_index", 1)
                    param("page_size", 10)
                }.await()
                LogUtils.e("result",result)
                val result_code = GsonUtils.getIntFromJSON(result, "result_code")
                val result_message = GsonUtils.getStringFromJSON(result, "result_message")
                if (result_code == 0) {
                    val sales_network_list = GsonUtils.getStringFromJSON(result, "sales_network_list")
                    val objects = GsonUtils.jsonToBeanList<SalesNetwork>(sales_network_list,object : TypeToken<ArrayList<SalesNetwork?>?>() {}.type)
                    listener.onResult(result_code,result_message,objects)
                } else {
                    LogUtils.e("获取打印机列表失败：${result_message}")
                    listener.onResult(result_code,result_message,ArrayList())
                }
            }
        }.onFailure {
            LogUtils.e("获取打印机列表异常：",it)
            listener.onResult(1,"异常",ArrayList())
        }

    }

    /**创建网络打印订单
     * /business/print_order/create_print_order
     * */
    var rePrintCount = 0
    fun createPrinterOrder(fileUrl:String,printNum:Int=1,terminal_code:String,listener: CreatePrinterOrderListener){
        runCatching {
            scopeNet {
                val result = Post<String>(HOST + "/business/print_order/create_print_order?access_token=$ACCESS_TOKEN"){
                    var params = CreateOrderParam().apply {
                        this.terminal_code = terminal_code
                        var order = CreatePrintOrderDetail().apply {
                            this.print_file_url = fileUrl
                            this.copy_count = printNum
                        }
                        this.print_order_detail_list.add(order)
                    }
                    json(GsonUtils.serializedToJson(params))
                }.await()
                LogUtils.e("result",result)
                val result_code = GsonUtils.getIntFromJSON(result, "result_code")
                val result_message = GsonUtils.getStringFromJSON(result, "result_message")
                if (result_code == 0) {
                    rePrintCount = 0
                    val print_order_id = GsonUtils.getStringFromJSON(result, "print_order_id")
                    listener.onResult(result_code,result_message,print_order_id)
                } else {
                    rePrintCount++
                    if(rePrintCount<5){
                        ToastUtils.showLong("打印任务执行中，请稍后...\n$result_message")
                        LogUtils.e("创建打印订单失败：${result_message},5s后 重新读取token 重试次数 $rePrintCount")
                        ThreadUtils.runOnUiThreadDelayed({
                            if(result_message.contains("access_token")){
                                getAccessToken(object : InitTokenListener {
                                    override fun onResult(isSuccess: Boolean) {
                                        createPrinterOrder(fileUrl,printNum,terminal_code,listener)
                                    }
                                })
                            }else{
                                createPrinterOrder(fileUrl,printNum,terminal_code,listener)
                            }
                        },5000)
                    }else{
                        rePrintCount = 0
                        listener.onResult(result_code,result_message,"")
                    }
                }
            }
        }.onFailure {
            LogUtils.e("创建打印订单异常：",it)
            listener.onResult(1,"异常","")
        }

    }

    /**发送打印任务
     * /business/print_order/send_print_job
     * */
    fun sendPrinterJob(){

    }

    /**获取打印订单信息
     * /business/print_order/get_print_order
     * */
    fun getPrinterOrderDetail(){

    }

}