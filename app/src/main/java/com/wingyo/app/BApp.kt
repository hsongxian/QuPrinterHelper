package com.wingyo.app

import android.app.Application
import com.qupai.lib_base.app.BaseApplication

open class BApp : BaseApplication() {
    companion object {
        lateinit var appContext:Application
    }

    override fun onCreate() {
        super.onCreate()
        appContext = this
        //base url
        initUtilsAndNet(true)
    }
}