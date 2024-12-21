package com.wingyo.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.viewbinding.ViewBinding
import com.blankj.utilcode.util.ToastUtils
import java.lang.reflect.ParameterizedType

/**
 * DESC：
 * Created on 2021/4/12
 */
open class BaseActivity<VB : ViewBinding> : AppCompatActivity() {
    protected lateinit var bdi: VB

    override fun onCreate(savedInstanceState: Bundle?) {
        fullScree()
        super.onCreate(savedInstanceState)
        setContentView(inflectRootView())
        init(savedInstanceState)
    }





    private fun inflectRootView(): View {
        var rootView: View? = null
        // 使用反射获取泛型类型参数
        val clz1 = analysisClassInfo(this)
        if (clz1 != ViewBinding::class.java && ViewBinding::class.java.isAssignableFrom(clz1)) {
            try {
                // 通过反射获取对应的inflate方法
                val method = clz1.getDeclaredMethod("inflate", LayoutInflater::class.java)
                method.isAccessible = true
                bdi = method.invoke(null, layoutInflater) as VB
                if (bdi != null) {
                    rootView = bdi!!.root
                }
            } catch (e: Exception) {
                //e.printStackTrace();
            }
        }
        if (rootView == null) {
            throw RuntimeException(
                "RootView can not be null !"
            )
        }
        return rootView
    }

    fun analysisClassInfo(`object`: Any): Class<*> {
        val genType = `object`.javaClass.genericSuperclass
        // ParameterizedType参数化类型，即泛型
        val pType = genType as ParameterizedType
        // getActualTypeArguments获取参数化类型的数组，泛型可能有多个
        val params = pType.actualTypeArguments
        val type0 = params[0]
        return type0 as Class<*>
    }


    //endregion
    private fun fullScree() {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        // 设置为全屏模式
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
    }


    open fun init(savedInstanceState: Bundle?) {}


    fun startActivity(c: Class<*>?) {
        val intent = Intent(this, c)
        startAnActivity(intent)
    }

    fun startActivity(c: Class<*>?, isAnimate: Boolean) {
        val intent = Intent(this, c)
        startAnActivity(intent, isAnimate)
    }

    @JvmOverloads
    fun startActivity(c: Class<*>?, bundle: Bundle?, isAnimate: Boolean = true) {
        val intent = Intent(this, c)
        intent.putExtras(bundle!!)
        startAnActivity(intent, isAnimate)
    }

    private fun startAnActivity(intent: Intent, isAnimate: Boolean = true) {
        startActivity(intent)
    }


    override fun onPause() {
        super.onPause()
        ToastUtils.cancel()
    }

}
